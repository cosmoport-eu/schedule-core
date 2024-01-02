package com.cosmoport.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventTypeCategoryDto extends Entity {
    private final long i18nEventTypeCategoryName;
    private final long parent;

    private final String color;

    @JsonCreator
    public EventTypeCategoryDto(@JsonProperty("id") long id,
                                @JsonProperty("i18n_event_type_category_name") long i18nEventTypeCategoryName,
                                @JsonProperty("parent") long parent,
                                @JsonProperty("color") String color) {
        this.id = id;
        this.i18nEventTypeCategoryName = i18nEventTypeCategoryName;
        this.parent = parent;
        this.color = color;
    }

    public long getI18nEventTypeCategoryName() {
        return i18nEventTypeCategoryName;
    }

    public long getParent() {
        return parent;
    }

    public String getColor() {
        return color;
    }
}
