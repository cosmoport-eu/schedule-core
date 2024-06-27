package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.EventDtoRequest;
import com.cosmoport.cosmocore.controller.dto.EventDtoResponse;
import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.error.ValidationException;
import com.cosmoport.cosmocore.controller.helper.BindingHelper;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.events.SyncTimetablesMessage;
import com.cosmoport.cosmocore.model.*;
import com.cosmoport.cosmocore.repository.*;
import com.cosmoport.cosmocore.service.RemoteSync;
import com.cosmoport.cosmocore.service.Types;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/timetable")
public class TimetableEndpoint {
    private final ApplicationEventPublisher eventBus;
    private final RemoteSync remoteSync;
    private final TimeTableRepository timeTableRepository;
    private final EventTypeRepository eventTypeRepository;
    private final MaterialRepository materialRepository;
    private final FacilityRepository facilityRepository;
    private final EventTypeCategoryRepository eventTypeCategoryRepository;

    public TimetableEndpoint(ApplicationEventPublisher eventBus,
                             RemoteSync remoteSync,
                             TimeTableRepository timeTableRepository,
                             EventTypeRepository eventTypeRepository,
                             MaterialRepository materialRepository,
                             FacilityRepository facilityRepository,
                             EventTypeCategoryRepository eventTypeCategoryRepository) {
        this.eventBus = eventBus;
        this.remoteSync = remoteSync;
        this.timeTableRepository = timeTableRepository;
        this.eventTypeRepository = eventTypeRepository;
        this.materialRepository = materialRepository;
        this.facilityRepository = facilityRepository;
        this.eventTypeCategoryRepository = eventTypeCategoryRepository;
    }

    @GetMapping("/all")
    @Transactional
    public List<EventDtoResponse> getAll(
            @RequestParam int page,
            @RequestParam int count) {
        final List<EventDtoResponse> events =
                timeTableRepository.findAllByEventDate(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                        .stream()
                        .skip((page - 1L) * count)
                        .limit(count)
                        .map(this::convertToDto)
                        .toList();

        // We assume that this method will be called on every timetable app opening
        eventBus.publishEvent(new SyncTimetablesMessage(this));
        return events;
    }

    /**
     * Gets the event with {@code id} and one event after that for same gate.
     *
     * @param id long An id of event.
     * @return Two events.
     */
    @GetMapping("/byIdAndOneAfter")
    @Transactional
    public List<EventDtoResponse> getEvents(@RequestParam("id") int id) {
        final Optional<TimetableEntity> mainEvent = timeTableRepository.findById(id);
        if (mainEvent.isEmpty()) {
            return Collections.emptyList();
        }

        final TimetableEntity main = mainEvent.get();
        return timeTableRepository.findNextEventForGate(main.getEventDate(), main.getGateId(), main.getGate2Id(), main.getStartTime(), main.getEventStatusId(), main.getId())
                .map(next -> Arrays.asList(convertToDto(main), convertToDto(next)))
                .orElse(Collections.singletonList(convertToDto(main)));
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable("id") final int id) {
        timeTableRepository.deleteById(id);
        eventBus.publishEvent(new ReloadMessage(this));
        remoteSync.process(Types.DELETE, new RemoteSync.EventIdDto(id));
        return "{\"deleted\": true}";
    }

    /**
     * Updates the number of sold tickets. Same as {@link SyncEndpoint::updateTickets}.
     *
     * @param request An object containing an events' id and its new tickets count value.
     * @return A result object.
     * @throws RuntimeException In case of any errors.
     */
    @PostMapping("/tickets")
    public ResultDto updateTickets(@RequestBody TicketsUpdateRequestDto request) {
        final Boolean result = timeTableRepository.findById(request.id()).map(
                        timetableEntity -> {
                            timetableEntity.setContestants(request.tickets());
                            timetableEntity.setEventStateId(1);
                            timeTableRepository.save(timetableEntity);
                            return true;
                        })
                .orElse(false);
        return new ResultDto(result);
    }

    @GetMapping("/suggest/next")
    public TimeSuggestionDto getSuggestion(@RequestParam("gateId") int gateId,
                                           @RequestParam("date") final String date) {
        if (gateId <= 0) {
            throw new ValidationException("Set the gate number.");
        }

        final String dateString = date != null && !date.isEmpty() ? date :
                new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        return new TimeSuggestionDto(timeTableRepository.getLastTimeForGate(gateId, dateString));
    }

    @Transactional
    @PostMapping("/update/event")
    public EventDtoResponse update(@RequestBody EventDtoRequest event) {
        final TimetableEntity toUpdate = timeTableRepository.findById(event.id()).orElseThrow();

        toUpdate.setEventDate(event.eventDate());
        toUpdate.setEventTypeId(event.eventTypeId());
        toUpdate.setEventStateId(event.eventStateId());
        toUpdate.setEventStatusId(event.eventStatusId());
        toUpdate.setGateId(event.gateId());
        toUpdate.setGate2Id(event.gate2Id());
        toUpdate.setStartTime(event.startTime());
        toUpdate.setDurationTime(event.durationTime());
        toUpdate.setRepeatInterval(event.repeatInterval());
        toUpdate.setCost(event.cost());
        toUpdate.setPeopleLimit(event.peopleLimit());
        toUpdate.setContestants(event.contestants());
        toUpdate.setDateAdded(event.dateAdded());
        if (event.description() != null) {
            toUpdate.setDescription(event.description());
        }

        if (event.materialIds() != null) {
            BindingHelper.updateAttributes(
                    materialRepository.findAllById(event.materialIds()),
                    toUpdate,
                    TimetableEntity::getMaterials,
                    MaterialEntity::getEvents);
        }

        if (event.facilityIds() != null) {
            BindingHelper.updateAttributes(
                    facilityRepository.findAllById(event.facilityIds()),
                    toUpdate,
                    TimetableEntity::getFacilities,
                    FacilityEntity::getEvents);
        }

        EventDtoResponse newEvent = convertToDto(timeTableRepository.save(toUpdate));

        eventBus.publishEvent(new ReloadMessage(this));
        remoteSync.process(Types.UPDATE, new RemoteSync.EventIdDto(event.id()));

        return newEvent;
    }


    @PostMapping
    @Transactional
    public EventDtoResponse create(@RequestBody final EventDtoRequest event) {
        final List<MaterialEntity> materials = event.materialIds() == null ? Collections.emptyList() :
                materialRepository.findAllById(event.materialIds());
        final List<FacilityEntity> facilities = event.materialIds() == null ? Collections.emptyList() :
                facilityRepository.findAllById(event.materialIds());

        final TimetableEntity timetableEntity = timeTableRepository.save(new TimetableEntity(
                event.id(),
                event.eventDate(),
                event.eventTypeId(),
                event.eventStatusId(),
                event.eventStateId(),
                event.gateId(),
                event.gate2Id(),
                event.startTime(),
                event.durationTime(),
                event.repeatInterval(),
                event.cost(),
                event.peopleLimit(),
                event.contestants(),
                event.dateAdded(),
                event.description(),
                new HashSet<>(),
                new HashSet<>()
        ));

        materials.forEach(entity -> entity.getEvents().add(timetableEntity));
        facilities.forEach(entity -> entity.getEvents().add(timetableEntity));

        timetableEntity.getFacilities().addAll(facilities);
        timetableEntity.getMaterials().addAll(materials);

        timeTableRepository.save(timetableEntity);

        eventBus.publishEvent(new ReloadMessage(this));
        remoteSync.process(Types.CREATE, new RemoteSync.EventIdDto(timetableEntity.getId()));
        return new EventDtoResponse(
                timetableEntity.getId(),
                timetableEntity.getEventDate(),
                timetableEntity.getEventTypeId(),
                timetableEntity.getEventStateId(),
                timetableEntity.getEventStatusId(),
                timetableEntity.getGateId(),
                timetableEntity.getGate2Id(),
                timetableEntity.getStartTime(),
                timetableEntity.getDurationTime(),
                timetableEntity.getRepeatInterval(),
                timetableEntity.getCost(),
                timetableEntity.getPeopleLimit(),
                timetableEntity.getContestants(),
                timetableEntity.getDateAdded(),
                timetableEntity.getDescription(),
                timetableEntity.getMaterials().stream().map(MaterialEntity::getId).collect(Collectors.toSet()),
                timetableEntity.getFacilities().stream().map(FacilityEntity::getId).collect(Collectors.toSet())
        );
    }

    @GetMapping
    @Transactional
    public List<EventDtoWithColor> get(String date,
                                       String date2,
                                       @RequestParam(required = false) Integer gateId) {
        final Map<Integer, String> categoryTypeToColorMap = eventTypeCategoryRepository.findAll().stream()
                .collect(Collectors.toMap(EventTypeCategoryEntity::getId, EventTypeCategoryEntity::getColor));
        final Map<Integer, String> categoryIdToColorMap = eventTypeRepository.findAll().stream()
                .collect(Collectors.toMap(EventTypeEntity::getId, ete -> categoryTypeToColorMap.get(ete.getCategoryId())));

        return timeTableRepository.findAllByEventDateIsBetween(date, date2).stream()
                .filter(event -> gateId == null || gateId.equals(event.getGateId()))
                .sorted(Comparator.comparing(TimetableEntity::getEventDate).thenComparing(TimetableEntity::getGateId))
                .map(event -> new EventDtoWithColor(
                        event.getId(),
                        event.getEventDate(),
                        event.getEventTypeId(),
                        categoryIdToColorMap.get(event.getEventTypeId()),
                        event.getEventStateId(),
                        event.getEventStatusId(),
                        event.getGateId(),
                        event.getGate2Id(),
                        event.getStartTime(),
                        event.getDurationTime(),
                        event.getRepeatInterval(),
                        event.getCost(),
                        event.getPeopleLimit(),
                        event.getContestants(),
                        event.getDateAdded(),
                        event.getDescription(),
                        event.getMaterials().stream().map(MaterialEntity::getId).collect(Collectors.toSet()),
                        event.getFacilities().stream().map(FacilityEntity::getId).collect(Collectors.toSet())
                ))
                .toList();
    }


    public record EventDtoWithColor(int id, String eventDate, int eventTypeId, String eventColor, int eventStateId,
                                    int eventStatusId, int gateId, int gate2Id, int startTime, int durationTime,
                                    int repeatInterval,
                                    double cost, int peopleLimit, int contestants, String dateAdded, String description,
                                    Set<Integer> materialIds, Set<Integer> facilityIds
    ) {
    }


    private EventDtoResponse convertToDto(TimetableEntity timetableEntity) {
        return new EventDtoResponse(
                timetableEntity.getId(),
                timetableEntity.getEventDate(),
                timetableEntity.getEventTypeId(),
                timetableEntity.getEventStateId(),
                timetableEntity.getEventStatusId(),
                timetableEntity.getGateId(),
                timetableEntity.getGate2Id(),
                timetableEntity.getStartTime(),
                timetableEntity.getDurationTime(),
                timetableEntity.getRepeatInterval(),
                timetableEntity.getCost(),
                timetableEntity.getPeopleLimit(),
                timetableEntity.getContestants(),
                timetableEntity.getDateAdded(),
                timetableEntity.getDescription(),
                timetableEntity.getMaterials().stream().map(MaterialEntity::getId).collect(Collectors.toSet()),
                timetableEntity.getFacilities().stream().map(FacilityEntity::getId).collect(Collectors.toSet())
        );
    }

    public record TicketsUpdateRequestDto(int id, int tickets, boolean forceOpen) {
    }

    public record TimeSuggestionDto(int time) {
    }

}
