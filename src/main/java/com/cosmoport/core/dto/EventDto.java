package com.cosmoport.core.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for an event.
 * It stores all references as ids on referenced objects.
 *
 * @since 0.1.0
 */
public class EventDto extends Entity {

    private final String eventDate;
    private final long eventTypeId;
    private final long eventStateId;
    private final long eventStatusId;
    private final long eventDestinationId;
    private final long gateId;
    private final long gate2Id;
    private final long startTime;
    private final long durationTime;
    private final long repeatInterval;
    private final double cost;
    private final long peopleLimit;
    private final long contestants;
    private final String dateAdded;

    @JsonCreator
    public EventDto(@JsonProperty("id") long id,
                    @JsonProperty("event_date") String eventDate,
                    @JsonProperty("event_type_id") long eventTypeId,
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
                    @JsonProperty("date_added") String dateAdded) {
        this.id = id;
        this.eventDate = eventDate;
        this.eventTypeId = eventTypeId;
        this.eventStatusId = eventStatusId;
        this.eventStateId = eventStateId;
        this.eventDestinationId = eventDestinationId;
        this.gateId = gateId;
        this.gate2Id = gate2Id;
        this.startTime = startTime;
        this.durationTime = durationTime;
        this.repeatInterval = repeatInterval;
        this.cost = cost;
        this.peopleLimit = peopleLimit;
        this.contestants = contestants;
        this.dateAdded = dateAdded;
    }

    public EventDto(long id) {
        this.id = id;
        eventStateId = 0;
        eventDate = null;
        eventTypeId = 0;
        eventStatusId = 0;
        eventDestinationId = 0;
        gateId = 0;
        gate2Id = 0;
        startTime = 0;
        durationTime = 0;
        repeatInterval = 0;
        cost = 0;
        peopleLimit = 0;
        contestants = 0;
        dateAdded = null;
    }

    public String getEventDate() {
        return eventDate;
    }

    public long getEventTypeId() {
        return eventTypeId;
    }

    public long getEventStatusId() {
        return eventStatusId;
    }

    public long getEventStateId() {
        return eventStateId;
    }

    public long getEventDestinationId() {
        return eventDestinationId;
    }

    public long getGateId() {
        return gateId;
    }

    public long getGate2Id() {
        return gate2Id;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDurationTime() {
        return durationTime;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public double getCost() {
        return cost;
    }

    public long getPeopleLimit() {
        return peopleLimit;
    }

    public long getContestants() {
        return contestants;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    @Override
    public String toString() {
        return "EventDto{" +
                "eventDate='" + eventDate + '\'' +
                ", eventTypeId=" + eventTypeId +
                ", eventStatusId=" + eventStatusId +
                ", eventStateId=" + eventStateId +
                ", eventDestinationId=" + eventDestinationId +
                ", gateId=" + gateId +
                ", gate2Id=" + gate2Id +
                ", startTime=" + startTime +
                ", durationTime=" + durationTime +
                ", repeatInterval=" + repeatInterval +
                ", cost=" + cost +
                ", peopleLimit=" + peopleLimit +
                ", contestants=" + contestants +
                ", dateAdded='" + dateAdded + '\'' +
                '}';
    }
}
