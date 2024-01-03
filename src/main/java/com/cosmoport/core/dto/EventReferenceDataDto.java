package com.cosmoport.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EventReferenceDataDto(List<EventTypeDto> types,
                                    @JsonProperty("type_categories") List<EventTypeCategoryDto> typeCategories,
                                    List<EventStatusDto> statuses,
                                    List<EventStateDto> states) {
}
