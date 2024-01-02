package com.cosmoport.core.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record CreateEventTypeRequestDto(long categoryId,
                                        String name,
                                        String description,
                                        List<CreateEventSubTypeRequestDto> subtypes,
                                        int defaultDuration,
                                        int defaultRepeatInterval,
                                        double defaultCost) {
    @JsonCreator
    public CreateEventTypeRequestDto(@JsonProperty("category_id") long categoryId,
                                     @JsonProperty("name") String name,
                                     @JsonProperty("description") String description,
                                     @JsonProperty("subtypes") List<CreateEventSubTypeRequestDto> subtypes,
                                     @JsonProperty("default_duration") int defaultDuration,
                                     @JsonProperty("default_repeat_interval") int defaultRepeatInterval,
                                     @JsonProperty("default_cost") double defaultCost) {
        this.categoryId = categoryId;
        // trim
        this.name = (name != null) ? name.trim() : null;
        this.description = description;
        this.subtypes = subtypes;
        this.defaultDuration = defaultDuration;
        this.defaultRepeatInterval = defaultRepeatInterval;
        this.defaultCost = defaultCost;
    }
}
