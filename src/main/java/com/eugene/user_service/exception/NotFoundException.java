package com.eugene.user_service.exception;

import java.io.Serial;

public class NotFoundException
        extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = 3L;

    public NotFoundException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}