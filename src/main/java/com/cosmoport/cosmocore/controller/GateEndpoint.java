package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.controller.helper.TranslationHelper;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.model.GateEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.GateRepository;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gates")
public class GateEndpoint {

    private final GateRepository gateRepository;
    private final LocaleRepository localeRepository;
    private final TranslationRepository translationRepository;
    private final ApplicationEventPublisher eventBus;

    public GateEndpoint(GateRepository gateRepository,
                        LocaleRepository localeRepository,
                        TranslationRepository translationRepository,
                        ApplicationEventPublisher eventBus) {
        this.gateRepository = gateRepository;
        this.localeRepository = localeRepository;
        this.translationRepository = translationRepository;
        this.eventBus = eventBus;
    }

    @Transactional
    @PostMapping
    @Operation(summary = "Создать новые ворота")
    public ResultDto create(@RequestBody Object name) {
        final GateEntity tempGate = gateRepository.save(new GateEntity("new_gate"));
        tempGate.setCode("gate_" + tempGate.getId());
        final GateEntity newGate = gateRepository.save(tempGate);

        translationRepository.saveAll(
                TranslationHelper.createTranslationForCodeAndDefaultText(localeRepository, newGate.getCode(), name.toString())
        );

        return ResultDto.ok();
    }

    @GetMapping
    @Operation(summary = "Получить все ворота со всеми переводами")
    public List<GateDto> getAll(
            @RequestParam(value = "isActive", required = false) Boolean isActive
    ) {
        return gateRepository.findAll().stream()
                .filter(entity -> isActive == null || entity.isDisabled() != isActive)
                .map(gateEntity -> new GateDto(
                                gateEntity.getId(),
                                gateEntity.getCode(),
                                gateEntity.isDisabled(),
                                TranslationHelper.getTranslationsByCode(translationRepository, gateEntity.getCode())
                        )
                )
                .toList();
    }

    @GetMapping("/locale/{localeId}")
    @Operation(summary = "Получить все ворота с текстом для указанной локали")
    public List<GateDtoWithText> getAllWithText(@PathVariable int localeId,
                                                @RequestParam(value = "isActive", required = false) Boolean isActive) {
        return gateRepository.findAll().stream()
                .filter(entity -> isActive == null || entity.isDisabled() != isActive)
                .map(gateEntity -> {
                    final TranslationEntity translation = translationRepository.findByLocaleIdAndCode(localeId, gateEntity.getCode())
                            .orElseThrow(() -> new IllegalStateException("No translation for gate code " + gateEntity.getCode() + " and locale " + localeId));
                    return new GateDtoWithText(
                            gateEntity.getId(),
                            translation.getId(),
                            gateEntity.getCode(),
                            translation.getText(),
                            gateEntity.isDisabled()
                    );
                })
                .toList();
    }

    @PostMapping("/{translationId}")
    @Operation(summary = "Обновить название ворот по id перевода")
    public ResultDto updateText(@PathVariable int translationId, @RequestBody Object text) {
        translationRepository.findById(translationId)
                .ifPresentOrElse(
                        translationEntity -> {
                            translationEntity.setText(text.toString());
                            translationRepository.save(translationEntity);
                            eventBus.publishEvent(new ReloadMessage(this));
                        },
                        () -> {
                            throw new IllegalStateException("No translation with id " + translationId);
                        }
                );
        return ResultDto.ok();
    }

    @Transactional
    @PostMapping("/code/{gateId}")
    @Operation(summary = "Обновить кодовое обозначение ворот")
    public ResultDto updateCode(@PathVariable int gateId, @RequestBody Object text) {
        gateRepository.findById(gateId)
                .ifPresentOrElse(
                        gateEntity -> {
                            final List<TranslationEntity> translations =
                                    translationRepository.findAllByCode(gateEntity.getCode());
                            translations.forEach(translationEntity -> translationEntity.setCode(text.toString()));
                            gateEntity.setCode(text.toString());

                            gateRepository.save(gateEntity);
                            translationRepository.saveAll(translations);
                        },
                        () -> {
                            throw new IllegalStateException("No gate with id " + gateId);
                        }
                );

        eventBus.publishEvent(new ReloadMessage(this));
        return ResultDto.ok();
    }

    @Transactional
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить ворота по id")
    public ResultDto delete(@PathVariable int id) {
        gateRepository.findById(id).ifPresentOrElse(gateEntity -> gateEntity.setDisabled(true), () -> {
            throw new IllegalArgumentException("Not found");
        });
        eventBus.publishEvent(new ReloadMessage(this));
        return ResultDto.ok();
    }

    public record GateDto(long id, String code, boolean isDisabled, List<TranslationDto> translations) {
    }

    public record GateDtoWithText(long id, int translationId, String code, String text, boolean isDisabled) {
    }
}
