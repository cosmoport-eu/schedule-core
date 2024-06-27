package com.cosmoport.cosmocore.controller.dto;

public record ResultDto(boolean result) {
    public static ResultDto ok() {
        return new ResultDto(true);
    }
}
