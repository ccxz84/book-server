package com.ssu.commerce.book.exception;

import com.ssu.commerce.core.exception.SsuCommerceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

public class DistributedLockException extends SsuCommerceException {
    public DistributedLockException(@Nullable String errorCode, @NotNull String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }
}
