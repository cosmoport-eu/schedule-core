package com.cosmoport.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EventDtoWithColor extends EventDto{
    private final String eventColor;

    @JsonCreator
    public EventDtoWithColor(
            @JsonProperty("id") long id,
            @JsonProperty("event_date") String eventDate,
            @JsonProperty("event_type_id") long eventTypeId,
            @JsonProperty("event_color") String eventColor,
            @JsonProperty("event_state_id") long eventStateId,
            @JsonProperty("event_status_id") long eventStatusId,
            @JsonProperty("event_destination_id") long eventDestinationId,
            @JsonProperty("gate_id") long gateId,
            @JsonProperty("gate2_id") long gate2Id,
            @JsonProperty("start_time") long startTime,
            @JsonProperty("duration_time") long durationTime,
            @JsonProperty("repeat_interval") long repeatInterval,
            @JsonProperty("cost") double cost,
            @JsonProperty("people_limit") long peopleLimit,
            @JsonProperty("contestants") long contestants,
            @JsonProperty("date_added") String dateAdded
    ) {
        super(id, eventDate, eventTypeId, eventStateId, eventStatusId, eventDestinationId, gateId, gate2Id, startTime, durationTime, repeatInterval, cost, peopleLimit, contestants, dateAdded);
        this.eventColor = eventColor;
    }

    public EventDtoWithColor(long id) {
        super(id);
        this.eventColor = null;
    }

    public String getEventColor() {
        return eventColor;
    }
}
