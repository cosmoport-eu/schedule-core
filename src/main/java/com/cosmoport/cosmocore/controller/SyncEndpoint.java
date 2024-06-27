package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.Constants;
import com.cosmoport.cosmocore.controller.dto.EventDtoRequest;
import com.cosmoport.cosmocore.controller.dto.EventDtoResponse;
import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.error.ApiAuthError;
import com.cosmoport.cosmocore.controller.helper.BindingHelper;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.model.FacilityEntity;
import com.cosmoport.cosmocore.model.MaterialEntity;
import com.cosmoport.cosmocore.model.TimetableEntity;
import com.cosmoport.cosmocore.repository.FacilityRepository;
import com.cosmoport.cosmocore.repository.MaterialRepository;
import com.cosmoport.cosmocore.repository.SettingsRepository;
import com.cosmoport.cosmocore.repository.TimeTableRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sync")
public class SyncEndpoint {

    private final ApplicationEventPublisher eventBus;
    private final MaterialRepository materialRepository;
    private final FacilityRepository facilityRepository;
    private final SettingsRepository settingsRepository;
    private final TimeTableRepository timeTableRepository;

    @Transactional
    @PostMapping("/add/event")
    public EventDtoResponse create(@RequestBody SyncAddEventDto syncAddEvent) {
        auth(syncAddEvent);
        final EventDtoRequest event = syncAddEvent.event();

        final TimetableEntity timetableEntity = timeTableRepository.findById(event.id()).orElse(
                new TimetableEntity(event.id(),
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
                )
        );

        timetableEntity.setEventDate(event.eventDate());
        timetableEntity.setEventTypeId(event.eventTypeId());
        timetableEntity.setEventStateId(event.eventStateId());
        timetableEntity.setEventStatusId(event.eventStatusId());
        timetableEntity.setGateId(event.gateId());
        timetableEntity.setGate2Id(event.gate2Id());
        timetableEntity.setStartTime(event.startTime());
        timetableEntity.setDurationTime(event.durationTime());
        timetableEntity.setRepeatInterval(event.repeatInterval());
        timetableEntity.setCost(event.cost());
        timetableEntity.setPeopleLimit(event.peopleLimit());
        timetableEntity.setContestants(event.contestants());
        if(event.description() != null) {
            timetableEntity.setDescription(event.description());
        }

        if (event.materialIds() != null) {
            BindingHelper.updateAttributes(
                    materialRepository.findAllById(event.materialIds()),
                    timetableEntity,
                    TimetableEntity::getMaterials,
                    MaterialEntity::getEvents);
        }

        if (event.facilityIds() != null) {
            BindingHelper.updateAttributes(
                    facilityRepository.findAllById(event.facilityIds()),
                    timetableEntity,
                    TimetableEntity::getFacilities,
                    FacilityEntity::getEvents);
        }

        TimetableEntity updatedEntity = timeTableRepository.save(timetableEntity);

        eventBus.publishEvent(new ReloadMessage(this));

        return new EventDtoResponse(
                updatedEntity.getId(),
                updatedEntity.getEventDate(),
                updatedEntity.getEventTypeId(),
                updatedEntity.getEventStateId(),
                updatedEntity.getEventStatusId(),
                updatedEntity.getGateId(),
                updatedEntity.getGate2Id(),
                updatedEntity.getStartTime(),
                updatedEntity.getDurationTime(),
                updatedEntity.getRepeatInterval(),
                updatedEntity.getCost(),
                updatedEntity.getPeopleLimit(),
                updatedEntity.getContestants(),
                updatedEntity.getDateAdded(),
                updatedEntity.getDescription(),
                updatedEntity.getMaterials().stream().map(MaterialEntity::getId).collect(Collectors.toSet()),
                updatedEntity.getFacilities().stream().map(FacilityEntity::getId).collect(Collectors.toSet())
        );
    }

    public SyncEndpoint(ApplicationEventPublisher eventBus,
                        MaterialRepository materialRepository,
                        FacilityRepository facilityRepository,
                        SettingsRepository settingsRepository,
                        TimeTableRepository timeTableRepository) {
        this.eventBus = eventBus;
        this.materialRepository = materialRepository;
        this.facilityRepository = facilityRepository;
        this.settingsRepository = settingsRepository;
        this.timeTableRepository = timeTableRepository;
    }

    @PostMapping("/tickets")
    public ResultDto syncTickets(@RequestBody SyncTicketsDto syncTickets) {
        auth(syncTickets);

        timeTableRepository.findById(syncTickets.eventId()).ifPresentOrElse(
                timeTableEntity -> {
                    timeTableEntity.setContestants(syncTickets.value());
                    timeTableRepository.save(timeTableEntity);
                },
                () -> {
                    throw new IllegalArgumentException("Event not found");
                }
        );

        return new ResultDto(true);
    }

    private void auth(HasAuthKey syncRequest) {
        final boolean isKeyOk = settingsRepository.findByParam(Constants.SYNC_SERVER_KEY).orElseThrow()
                .getValue().equals(syncRequest.key());

        if (!isKeyOk) {
            throw new ApiAuthError();
        }
    }


    public interface HasAuthKey {
        String key();
    }


    public record SyncTicketsDto(String key, int eventId, int value, String timestamp) implements HasAuthKey {
    }

    public record SyncAddEventDto(String key,
                                  EventDtoRequest event,
                                  String timestamp) implements HasAuthKey {
        @Override
        public String key() {
            return null;
        }
    }

}
