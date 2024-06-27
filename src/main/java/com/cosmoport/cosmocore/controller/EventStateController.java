package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.dto.TextUpdateDto;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.EventStateRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/state")
public class EventStateController {

    private final ApplicationEventPublisher eventBus;
    private final EventStateRepository eventStateRepository;
    private final TranslationRepository translationRepository;

    public EventStateController(ApplicationEventPublisher eventBus,
                                EventStateRepository eventStateRepository,
                                TranslationRepository translationRepository
    ) {
        this.eventBus = eventBus;
        this.eventStateRepository = eventStateRepository;
        this.translationRepository = translationRepository;
    }


    @PostMapping("/updateText")
    public ResultDto updateText(@RequestBody TextUpdateDto textUpdateDto) {
        eventStateRepository.findById(textUpdateDto.id())
                .ifPresentOrElse(eventStateEntity -> {
                    final TranslationEntity translation = translationRepository.findByLocaleIdAndCode(textUpdateDto.localeId(), eventStateEntity.getCode())
                            .orElseThrow(() -> new RuntimeException("Translation not found"));
                    translation.setText(textUpdateDto.text());
                    translationRepository.save(translation);
                    eventBus.publishEvent(new ReloadMessage(this));
                }, () -> {
                    throw new IllegalArgumentException("Event state not found");
                });
        return ResultDto.ok();
    }

    @Transactional
    @PostMapping("/updateCode/{id}")
    public ResultDto updateCode(@PathVariable int id, @RequestBody Object newCode) {
        eventStateRepository.findById(id)
                .ifPresentOrElse(eventStateEntity -> {
                    final List<TranslationEntity> translations =
                            translationRepository.findAllByCode(eventStateEntity.getCode());
                    translations.forEach(translation -> translation.setCode(newCode.toString()));

                    eventStateEntity.setCode(newCode.toString());
                    eventStateRepository.save(eventStateEntity);
                    translationRepository.saveAll(translations);
                }, () -> {
                    throw new IllegalArgumentException("Event state not found");
                });
        return ResultDto.ok();
    }
}
