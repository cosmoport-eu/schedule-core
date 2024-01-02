package com.cosmoport.core.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateEventTypeCategoryRequestDto(String name, String color) {
    @JsonCreator
    public CreateEventTypeCategoryRequestDto(@JsonProperty("name") String name,
                                             @JsonProperty("color") String color) {
        this.name = name;
        this.color = color;
    }
}
