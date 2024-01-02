package com.cosmoport.core.api;

import com.cosmoport.core.dto.EventDto;
import com.cosmoport.core.dto.EventDtoWithColor;
import com.cosmoport.core.dto.ResultDto;
import com.cosmoport.core.dto.TimeSuggestionDto;
import com.cosmoport.core.dto.request.TicketsUpdateRequestDto;
import com.cosmoport.core.event.message.ReloadMessage;
import com.cosmoport.core.event.message.SyncTimetablesMessage;
import com.cosmoport.core.persistence.SettingsPersistenceService;
import com.cosmoport.core.persistence.TimeTableReadOnlyService;
import com.cosmoport.core.persistence.TimetablePersistenceService;
import com.cosmoport.core.persistence.exception.ValidationException;
import com.cosmoport.core.service.SuggestionService;
import com.cosmoport.core.sync.RemoteSync;
import com.cosmoport.core.sync.Types;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Path("/timetable")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@GZIP
public final class TimetableEndpoint {
    private final TimetablePersistenceService service;
    private final TimeTableReadOnlyService roService;
    private final SettingsPersistenceService settings;
    private final SuggestionService suggestionService;
    private final EventBus eventBus;

    @Inject
    public TimetableEndpoint(TimetablePersistenceService service,
                             TimeTableReadOnlyService roService,
                             SettingsPersistenceService settings,
                             SuggestionService suggestionService,
                             EventBus eventBus) {
        this.service = service;
        this.roService = roService;
        this.settings = settings;
        this.suggestionService = suggestionService;
        this.eventBus = eventBus;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/")
    public List<EventDtoWithColor> get(@QueryParam("date") String date, @QueryParam("date2") String date2,
                                       @QueryParam("gate") long gateId) {
        return date2 != null && (!Objects.equals(date2, "")) ?
                roService.getAllFromDates(date, date2) :
                roService.getAllWithFilter(date, gateId);
    }

    @GET
    @Path("/all")
    public List<EventDto> getAll(
            @QueryParam("page") int page,
            @QueryParam("count") int count,
            @QueryParam("date") String date) {
        final List<EventDto> events = service.getAllPage(page, count);
        // We assume that this method will be called on every timetable app opening
        eventBus.post(new SyncTimetablesMessage());

        return events;
    }

    /**
     * Gets the event with {@code id} and one event after that for same gate.
     *
     * @param id long An id of event.
     * @return Two events.
     */
    @GET
    @Path("/byIdAndOneAfter")
    public List<EventDto> getEvents(@QueryParam("id") long id) {
        return service.getCustomByIdForGate(id);
    }

    @POST
    @Path("/")
    public EventDto create(final EventDto event) {
        final EventDto newEvent = service.save(event);
        eventBus.post(new ReloadMessage());
        new RemoteSync(settings.getSyncServerParams()).process(Types.CREATE, newEvent);


        return newEvent;
    }

    @POST
    @Path("/update/event")
    public EventDto update(final EventDto event) {
        final EventDto newEvent = service.save(event);
        eventBus.post(new ReloadMessage());
        new RemoteSync(settings.getSyncServerParams()).process(Types.UPDATE, newEvent);

        return newEvent;
    }

    /**
     * Updates the number of sold tickets. Same as {@link SyncEndpoint::updateTickets}.
     *
     * @param request An object containing an events' id and its new tickets count value.
     * @return A result object.
     * @throws RuntimeException In case of any errors.
     */
    @POST
    @Path("/tickets")
    public ResultDto updateTickets(final TicketsUpdateRequestDto request) throws RuntimeException {
        service.updateTicketsForced(request.id(), request.tickets(), request.forceOpen());

        return new ResultDto(true);
    }

    @DELETE
    @Path("/{id}")
    public String delete(@PathParam("id") final long id) {
        final String deleted = "{\"deleted\": " + service.delete(id) + '}';
        eventBus.post(new ReloadMessage());
        new RemoteSync(settings.getSyncServerParams()).process(Types.DELETE, new EventDto(id));

        return deleted;
    }

    // Suggestions
    @GET
    @Path("/suggest/next")
    public TimeSuggestionDto getSuggestion(@QueryParam("gate") long gateId,
                                           @QueryParam("date") final String date) throws RuntimeException {
        if (!(gateId > 0)) {
            throw new ValidationException("Set the gate number.");
        }

        final String date_ = date != null && !date.isEmpty() ? date :
                new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        return suggestionService.suggest(gateId, date_);
    }
}
