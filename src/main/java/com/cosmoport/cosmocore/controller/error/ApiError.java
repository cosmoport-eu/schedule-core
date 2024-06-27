package com.cosmoport.cosmocore.controller.error;

public interface ApiError {
    ApiErrorDto getError();

    int getHttpStatus();
}
