package com.cosmoport.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class EventTypeDto extends Entity {
    private final long categoryId;
    private long i18nEventTypeName;
    private long i18nEventTypeDescription;
    private final int defaultDuration;
    private final int defaultRepeatInterval;
    private final double defaultCost;

    @JsonCreator
    public EventTypeDto(@JsonProperty("id") long id,
                        @JsonProperty("category_id") long categoryId,
                        @JsonProperty("i18n_event_type_name") long i18nEventTypeName,
                        @JsonProperty("i18n_event_type_description") long i18nEventTypeDescription,
                        @JsonProperty("default_duration") int defaultDuration,
                        @JsonProperty("default_repeat_interval") int defaultRepeatInterval,
                        @JsonProperty("default_cost") double defaultCost) {
        this.id = id;
        this.categoryId = categoryId;
        this.i18nEventTypeName = i18nEventTypeName;
        this.i18nEventTypeDescription = i18nEventTypeDescription;
        this.defaultDuration = defaultDuration;
        this.defaultRepeatInterval = defaultRepeatInterval;
        this.defaultCost = defaultCost;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public long getI18nEventTypeName() {
        return i18nEventTypeName;
    }

    public long getI18nEventTypeDescription() {
        return i18nEventTypeDescription;
    }

    public int getDefaultDuration() {
        return defaultDuration;
    }

    public int getDefaultRepeatInterval() {
        return defaultRepeatInterval;
    }

    public double getDefaultCost() {
        return defaultCost;
    }

    public void setI18nEventTypeName(long i18nEventTypeName) {
        this.i18nEventTypeName = i18nEventTypeName;
    }

    public void setI18nEventTypeDescription(long i18nEventTypeDescription) {
        this.i18nEventTypeDescription = i18nEventTypeDescription;
    }
}
