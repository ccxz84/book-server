package com.ssu.commerce.book.grpc;

import com.ssu.commerce.book.annotation.DistributedLock;
import com.ssu.commerce.book.constant.code.BookState;
import com.ssu.commerce.book.dto.param.UpdateBookStateRequestDto;
import com.ssu.commerce.book.exception.BookStateConflictException;
import com.ssu.commerce.book.model.Book;
import com.ssu.commerce.book.persistence.BookRepository;
import com.ssu.commerce.core.error.NotFoundException;
import com.ssu.commerce.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/*
    도서 대여 신청
    Book Id 리스트를 받아서, 도서가 '대여 가능' 상태인지 확인 후, 대여 가능하다면 '대여 중'으로 변경함.
    '대여 불가능', '대여 중' 상태인 경우 에러를 반환함.

    도서 반납 신청
    Book Id 리스트를 받아서, 도서를 '대여 가능' 상태로 변경.
    '대여 불가능' 상태인 도서의 경우 '대여 가능'으로 변경하지 않음.

 */

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcUpdateBookStateService extends UpdateBookStateGrpc.UpdateBookStateImplBase {
    private final BookRepository bookRepository;

    @Override
    public void rentalBook(RentalBookRequest request, StreamObserver<RentalBookResponse> responseObserver) {
        List<UpdateBookStateRequestDto> rentalBookRequestDto = request.getIdList()
                .stream().map(id -> UpdateBookStateRequestDto.builder().bookId(UUID.fromString(id)).build())
                .collect(Collectors.toList());

        try {
            if (updateRentalBookstateIfValid(rentalBookRequestDto)) {
                RentalBookResponse response = RentalBookResponse.newBuilder()
                        .setMessage("Books rented successfully.")
                        .build();
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            RentalBookResponse response = RentalBookResponse.newBuilder()
                    .setMessage("Books not found.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            throw e;
        } catch (BookStateConflictException e) {
            RentalBookResponse response = RentalBookResponse.newBuilder()
                    .setMessage("Books could not be rented due to state conflicts.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            throw e;
        }
    }

    @Override
    public void returnBook(ReturnBookRequest request, StreamObserver<ReturnBookResponse> responseObserver) {
        List<UpdateBookStateRequestDto> returnBookRequestDto = request.getIdList()
                .stream().map(id -> UpdateBookStateRequestDto.builder().bookId(UUID.fromString(id)).build())
                .collect(Collectors.toList());


        try {
            // 대여 가능한 상태로 변경 가능한지 확인
            if (updateReturnBookstateIfValid(returnBookRequestDto)) {
                ReturnBookResponse response = ReturnBookResponse.newBuilder()
                        .setMessage("Books rented successfully.")
                        .build();
                responseObserver.onNext(response);
            }
            responseObserver.onCompleted();
        } catch (NotFoundException e) {
            ReturnBookResponse response = ReturnBookResponse.newBuilder()
                    .setMessage("Books not found.")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            throw e;
        }
    }

    private boolean updateReturnBookstateIfValid(
            @DistributedLock List<UpdateBookStateRequestDto> requestDto
    ) {
        List<UUID> bookIds = requestDto.stream().map(UpdateBookStateRequestDto::getBookId).collect(Collectors.toList());
        List<Book> booksToCheck = bookRepository.findAllById(bookIds);

        if (booksToCheck.isEmpty()) {
            throw new NotFoundException(
                    String.format("book not found; book list size=%d", bookIds.size()),
                    "BOOK_003"
            );
        }

        booksToCheck.forEach(book -> {
            if (book.canUpdateToState(BookState.SHARABLE)) { // '대여 가능' 상태로 변경가능 하면 업데이트
                book.updateBookState(BookState.SHARING);
                bookRepository.save(book);
            }
        });

        return true;
    }

    private boolean updateRentalBookstateIfValid(
            @DistributedLock List<UpdateBookStateRequestDto> requestDto
    ) {
        List<UUID> bookIds = requestDto.stream().map(UpdateBookStateRequestDto::getBookId).collect(Collectors.toList());
        List<Book> booksToCheck = bookRepository.findAllById(bookIds);

        if (booksToCheck.isEmpty()) {
            throw new NotFoundException(
                    String.format("book not found; book list size=%d", bookIds.size()),
                    "BOOK_003"
            );
        }

        boolean allUpdatePossible = booksToCheck.stream()
                .allMatch(book -> book.canUpdateToState(BookState.SHARING));

        if (!allUpdatePossible) {
            throw new BookStateConflictException("BOOK_004", "One or more books cannot be updated due to state conflicts.");
        }

        booksToCheck.forEach(book -> {
            book.updateBookState(BookState.SHARING);  // 가정: setBookState 메소드는 책의 상태를 업데이트
            bookRepository.save(book);  // 변경된 상태를 저장
        });

        return true;
    }
}
