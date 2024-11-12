package com.ssu.commerce.book.model;

import com.ssu.commerce.book.constant.code.BookState;
import com.ssu.commerce.book.dto.param.ChangeBookParamDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "book")
public class Book {

    @Id
    @Type(type = "uuid-char")
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "book_id", columnDefinition = "CHAR(36)")
    private UUID bookId;

    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(100) CHARACTER SET UTF8")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "VARCHAR(200) CHARACTER SET UTF8")
    private String content;

    @Column(name = "writer", nullable = false, columnDefinition = "VARCHAR(50) CHARACTER SET UTF8")
    private String writer;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "share_price", nullable = false)
    private Long sharePrice;

    @Column(name = "comment", columnDefinition = "VARCHAR(500) CHARACTER SET UTF8")
    private String comment;

    @Column(name = "start_borrow_day", nullable = false)
    private LocalDateTime startBorrowDay;

    @Column(name = "end_borrow_day", nullable = false)
    private LocalDateTime endBorrowDay;

    @Type(type = "uuid-char")
    @Column(name = "owner_id", columnDefinition = "CHAR(36)")
    private UUID ownerId;

    @Column(name = "publish_date", nullable = false)
    private LocalDateTime publishDate;

    @Column(name = "isbn", nullable = false, columnDefinition = "VARCHAR(50) CHARACTER SET UTF8")
    private String isbn;

    @Type(type = "uuid-char")
    @Column(name = "category_id", columnDefinition = "CHAR(36)", nullable = false)
    private UUID categoryId;

    @Column(name = "book_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookState bookState;

    public Book update(ChangeBookParamDto paramDto) {
        title = paramDto.getTitle();
        content = paramDto.getContent();
        writer = paramDto.getWriter();
        price = paramDto.getPrice();
        sharePrice = paramDto.getSharePrice();
        comment = paramDto.getComment();
        startBorrowDay = paramDto.getStartBorrowDay();
        endBorrowDay = paramDto.getEndBorrowDay();
        ownerId = paramDto.getOwnerId();
        publishDate = paramDto.getPublishDate();
        isbn = paramDto.getIsbn();
        categoryId = paramDto.getCategoryId();
        return this;
    }
    public void updateBookState(BookState bookState) {
        if (!isWithinBorrowDateRange()) {
            this.bookState = BookState.DISSHAREABLE; // 어떤 상태 값이 들어와도 대여 가능 날짜 범위 벗어나면 대여 불가 상태가 됨.
        } else {
            this.bookState = bookState;
        }
    }

    public boolean canUpdateToState(BookState desiredState) {
        if (desiredState == BookState.SHARING) {
            return !(this.bookState == BookState.SHARING || this.bookState == BookState.DISSHAREABLE);
        } else if (desiredState == BookState.SHARABLE) {
            return (this.bookState != BookState.DISSHAREABLE);
        }
        return false;
    }

    private boolean isWithinBorrowDateRange() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(startBorrowDay) && !now.isAfter(endBorrowDay);
    }

    public boolean rental() {
        if(isPossibleRentalState()) {
            updateBookState(BookState.DISSHAREABLE);
            return true;
        }

        return false;
    }

    boolean isPossibleRentalState() {
        return bookState == BookState.SHARABLE;
    }
}
