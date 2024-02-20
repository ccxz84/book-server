package com.ssu.commerce.book.model;

import com.ssu.commerce.book.constant.code.BookState;
import com.ssu.commerce.book.dto.param.ChangeBookParamDto;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;


/*
         *  1. 도서 조회 API call (성엽)
         *   도서 상세 내용을 조회한다.
         *   -> request (bookId)
         *   -> response (bookId, 대출가능여부, 대여가능기간, 대여 장소(책 주인 위치), 대여비, 보증금, 배송 flag - 픽업 or 배송, 배송비 고정)
        * 아이디, 책이름 , 상세보기 -> (페이지정보 출판사 필요시, 돈정보) , 책상태, 이미지주소, 거래가능지역
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "writer")
    private String writer;

    @Column(name = "price")
    private Long price;

    @Column(name = "owner_id", columnDefinition = "BINARY(16)")
    private UUID ownerId;

    @Column(name = "publish_date")
    private LocalDateTime publishDate;

    @Column(name = "isbn")
    private String isbn;

    @Column(name = "max_borrow_day")
    private Long maxBorrowDay;

    @Column(name = "category_id", columnDefinition = "BINARY(16)")
    private UUID categoryId;

    @Column(name = "book_state")
    @Enumerated(EnumType.STRING)
    private BookState bookState;

    public Book update(ChangeBookParamDto paramDto) {
        title = paramDto.getTitle();
        content = paramDto.getContent();
        writer = paramDto.getWriter();
        price = paramDto.getPrice();
        publishDate = paramDto.getPublishDate();
        isbn = paramDto.getIsbn();
        maxBorrowDay = paramDto.getMaxBorrowDay();
        categoryId = paramDto.getCategoryId();
        return this;
    }
    public void updateBookState(BookState bookState) {
        this.bookState = bookState;
    }

    boolean isPossibleRentalState() {
        return bookState == BookState.REGISTERED || bookState == BookState.RETURN;
    }

    public boolean rollBack() {
        if (bookState == BookState.REGISTERED) {
            updateBookState(BookState.REGISTERED);
            return true;
        }
        return false;
    }
}
