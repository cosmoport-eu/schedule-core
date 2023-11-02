package com.cosmoport.core.persistence.exception;

import java.io.Serial;

public final class ValidationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 2909626414579344513L;

    public ValidationException(final String message) {
        super(message);
    }
}
