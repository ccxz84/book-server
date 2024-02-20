package com.ssu.commerce.book.grpc;

import com.ssu.commerce.book.annotation.DistributedLock;
import com.ssu.commerce.book.dto.mapper.UpdateBookStateDtoMapper;
import com.ssu.commerce.book.dto.param.UpdateBookStateRequestDto;
import com.ssu.commerce.book.exception.BookStateConflictException;
import com.ssu.commerce.book.model.Book;
import com.ssu.commerce.book.persistence.BookRepository;
import com.ssu.commerce.core.error.NotFoundException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;
import com.ssu.commerce.book.constant.code.BookState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcUpdateBookStateService extends UpdateBookStateGrpc.UpdateBookStateImplBase {
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void updateBookState(
            UpdateBookStateRequest request,
            StreamObserver<UpdateBookStateResponse> responseObserver
    ) {
        /*
         *   TODO RentalBookRequest 의 token 검증
         */

        List<UpdateBookStateRequestDto> updateBookStateRequestDto = request.getUpdateBookStateDtosList()
                .stream()
                .map(UpdateBookStateDtoMapper.INSTANCE::map)
                .collect(Collectors.toList());

        updateBookState(updateBookStateRequestDto, responseObserver);

    }

    public void updateBookState(
            @DistributedLock List<UpdateBookStateRequestDto> requestDto,
            StreamObserver<UpdateBookStateResponse> responseObserver
    ) {
        Map<UUID, BookState> bookIdStateMap = requestDto.stream()
                .collect(Collectors.toMap(UpdateBookStateRequestDto::getId, UpdateBookStateRequestDto::getState));

        List<UUID> bookIds = new ArrayList<>(bookIdStateMap.keySet());
        List<Book> findBook = bookRepository.findAllById(bookIds);

        if (findBook.isEmpty()) {
            throw new NotFoundException(
                    String.format("book not found; book list size=%d", bookIds.size()),
                    "BOOK_003"
            );
        }

        for (Book book : findBook) {
            BookState newState = bookIdStateMap.get(book.getId());
            book.updateBookState(newState);
        }

        responseObserver.onNext(
                UpdateBookStateResponse.newBuilder().build()
        );
        responseObserver.onCompleted();
    }
}
