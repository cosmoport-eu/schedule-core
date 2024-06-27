package com.cosmoport.cosmocore.controller.dto;

import java.util.Set;

public record EventDtoResponse(int id, String eventDate, int eventTypeId, int eventStateId,
                               int eventStatusId, Integer gateId, Integer gate2Id, int startTime, int durationTime,
                               int repeatInterval, double cost, int peopleLimit, int contestants, String dateAdded,
                               String description, Set<Integer> materialIds, Set<Integer> facilityIds
) {
}
