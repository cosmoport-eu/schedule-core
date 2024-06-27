package com.cosmoport.cosmocore.controller;


import com.cosmoport.cosmocore.Constants;
import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.repository.SettingsRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/settings")
public class SettingsEndpoint {

    private final Set<String> protectedSettings = Set.of(Constants.PASSWORD, Constants.SYNC_SERVER_KEY);

    private final SettingsRepository settingsRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SettingsEndpoint(SettingsRepository settingsRepository,
                            ApplicationEventPublisher applicationEventPublisher) {
        this.settingsRepository = settingsRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @GetMapping
    public List<SettingsDto> getSettings() {
        return settingsRepository.findAll().stream()
                .filter(settings -> !protectedSettings.contains(settings.getParam()))
                .map(settings -> new SettingsDto(settings.getId(), settings.getParam(), settings.getValue()))
                .toList();
    }

    @PostMapping("/update/{id}")
    public ResultDto updateSetting(@PathVariable("id") int id, @RequestBody TextValueUpdateRequestDto requestDto) {
        settingsRepository.findById(id).ifPresent(settings -> {
            settings.setValue(requestDto.text());
            settingsRepository.save(settings);
        });

        applicationEventPublisher.publishEvent(new ReloadMessage(this));

        return new ResultDto(true);
    }

    public record SettingsDto(long id, String param, String value) {
    }

    public record TextValueUpdateRequestDto(String text) {
    }

}
