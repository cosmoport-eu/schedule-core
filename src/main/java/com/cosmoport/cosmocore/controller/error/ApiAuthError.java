package com.cosmoport.cosmocore.controller.error;

import java.io.Serial;

public class ApiAuthError extends RuntimeException implements ApiError {
    @Serial
    private static final long serialVersionUID = -2149236424230204297L;

    @Override
    public ApiErrorDto getError() {
        return new ApiErrorDto("e-5", "Unauthorized access.");
    }

    @Override
    public int getHttpStatus() {
        return 401;
    }
}
