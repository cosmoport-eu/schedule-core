package com.cosmoport.core.api;

import com.cosmoport.core.dto.*;
import com.cosmoport.core.dto.request.CreateEventTypeCategoryRequestDto;
import com.cosmoport.core.dto.request.CreateEventTypeRequestDto;
import com.cosmoport.core.event.message.ReloadMessage;
import com.cosmoport.core.persistence.*;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/t_events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@GZIP
public final class TimeEventsEndpoint {
    private final EventTypePersistenceService eventTypePersistenceService;
    private final EventTypeCategoryPersistenceService eventTypeCategoryPersistenceService;
    private final EventStatusPersistenceService eventStatusPersistenceService;
    private final EventStatePersistenceService eventStatePersistenceService;
    private final EventBus eventBus;

    @Inject
    public TimeEventsEndpoint(EventTypePersistenceService eventTypePersistenceService,
                              EventTypeCategoryPersistenceService eventTypeCategoryPersistenceService,
                              EventStatusPersistenceService eventStatusPersistenceService,
                              EventStatePersistenceService eventStatePersistenceService,
                              EventBus eventBus) {
        this.eventTypePersistenceService = eventTypePersistenceService;
        this.eventTypeCategoryPersistenceService = eventTypeCategoryPersistenceService;
        this.eventStatusPersistenceService = eventStatusPersistenceService;
        this.eventStatePersistenceService = eventStatePersistenceService;
        this.eventBus = eventBus;
    }

    @GET
    @Path("/reference_data")
    public EventReferenceDataDto getEventReferenceData() {
        return new EventReferenceDataDto(
                eventTypePersistenceService.getAll(),
                eventTypeCategoryPersistenceService.getAll(),
                eventStatusPersistenceService.getAll(),
                eventStatePersistenceService.getAll());
    }

    @GET
    @Path("/types")
    public List<EventTypeDto> getEventTypes() {
        return eventTypePersistenceService.getAll();
    }

    @POST
    @Path("/types")
    public EventTypeSaveResultDto createEventType(final CreateEventTypeRequestDto eventType) {
        return eventTypePersistenceService.save(eventType);
    }

    @DELETE
    @Path("/types/{id}")
    public String delete(@PathParam("id") final long id) {
        final String result = "{\"deleted\": " + eventTypePersistenceService.delete(id) + '}';
        eventBus.post(new ReloadMessage());
        return result;
    }

    @POST
    @Path("/types/categories")
    public EventTypeCategoryDto createEventTypeCategory(final CreateEventTypeCategoryRequestDto cat) {
        return eventTypeCategoryPersistenceService.create(cat);
    }

    @GET
    @Path("/statuses")
    public List<EventStatusDto> getEventStatuses() {
        return eventStatusPersistenceService.getAll();
    }

    @GET
    @Path("/states")
    public List<EventStateDto> getEventStates() {
        return eventStatePersistenceService.getAll();
    }
}
