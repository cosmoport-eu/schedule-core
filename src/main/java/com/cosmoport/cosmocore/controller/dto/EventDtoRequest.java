package com.cosmoport.cosmocore.controller.dto;

import java.util.Set;

public record EventDtoRequest(
        int id,
        String eventDate,
        int eventTypeId,
        int eventStateId,
        int eventStatusId,
        int gateId,
        int gate2Id,
        int startTime,
        int durationTime,
        int repeatInterval,
        double cost,
        int peopleLimit,
        int contestants,
        String dateAdded,
        String description,
        Set<Integer> materialIds,
        Set<Integer> facilityIds
) {
}
