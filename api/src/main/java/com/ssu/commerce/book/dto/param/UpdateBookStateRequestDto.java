package com.ssu.commerce.book.dto.param;

import com.ssu.commerce.book.constant.code.BookState;
import com.ssu.commerce.book.lock.AbstractLockableObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UpdateBookStateRequestDto extends AbstractLockableObject {
    private UUID id;

    private BookState state;
}
