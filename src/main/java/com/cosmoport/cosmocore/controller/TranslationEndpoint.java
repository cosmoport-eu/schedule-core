package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.events.ReloadMessage;
import com.cosmoport.cosmocore.events.TimeoutUpdateMessage;
import com.cosmoport.cosmocore.model.LocaleEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/translations")
public class TranslationEndpoint {
    private final TranslationRepository translationRepository;
    private final LocaleRepository localeRepository;
    private final ApplicationEventPublisher eventBus;

    public TranslationEndpoint(TranslationRepository translationRepository,
                               LocaleRepository localeRepository,
                               ApplicationEventPublisher eventBus) {
        this.translationRepository = translationRepository;
        this.localeRepository = localeRepository;
        this.eventBus = eventBus;
    }

    @Deprecated
    @PostMapping("/locale")
    public LocaleDto createLocale(@RequestBody LocaleDto locale) {
        final LocaleEntity localeEntity = new LocaleEntity();
        localeEntity.setDefault(false);
        localeEntity.setCode(locale.code());
        localeEntity.setShow(true);
        localeEntity.setLocaleDescription(locale.localeDescription());
        final LocaleEntity newLocale = localeRepository.save(localeEntity);

        final List<TranslationEntity> neDefaultTranslations = translationRepository.findAllByLocaleId(1).stream()
                .map(translationEntity -> {
                    final TranslationEntity newTranslation = new TranslationEntity();
                    newTranslation.setLocaleId(newLocale.getId());
                    newTranslation.setText(translationEntity.getText());
                    return newTranslation;
                })
                .toList();

        translationRepository.saveAll(neDefaultTranslations);

        return new LocaleDto(newLocale.getId(), newLocale.getCode(), newLocale.isDefault(),
                newLocale.getLocaleDescription(), newLocale.isShow(), newLocale.getShowTime());
    }

    @PostMapping("/locale/show")
    public LocaleDto setLocaleShowData(@RequestBody LocaleDto locale) {
        return localeRepository.findById(locale.id())
                .map(localeEntity -> {
                    localeEntity.setShow(locale.show());
                    localeEntity.setShowTime(locale.showTime());
                    LocaleEntity newLocale = localeRepository.save(localeEntity);
                    eventBus.publishEvent(new TimeoutUpdateMessage(this));
                    return newLocale;
                })
                .map(localeEntity -> new LocaleDto(localeEntity.getId(), localeEntity.getCode(), localeEntity.isDefault(),
                        localeEntity.getLocaleDescription(), localeEntity.isShow(), localeEntity.getShowTime()))
                .orElseThrow();
    }


    @GetMapping("/locales/visible")
    public List<LocaleDto> getVisibleLocales() {
        return localeRepository.findAll().stream()
                .filter(LocaleEntity::isShow)
                .map(localeEntity -> new LocaleDto(localeEntity.getId(), localeEntity.getCode(), localeEntity.isDefault(),
                        localeEntity.getLocaleDescription(), true, localeEntity.getShowTime())).toList();
    }

    @GetMapping("/locales")
    public List<LocaleDto> getLocales() {
        return localeRepository.findAll().stream().map(localeEntity -> new LocaleDto(localeEntity.getId(), localeEntity.getCode(), localeEntity.isDefault(),
                localeEntity.getLocaleDescription(), localeEntity.isShow(), localeEntity.getShowTime())).toList();
    }

    @PostMapping("/update/{translationId}")
    public ResultDto updateTranslation(@PathVariable("translationId") int translationId,
                                       @RequestBody Object text) {
        return new ResultDto(translationRepository.findById(translationId).map(translationEntity -> {
            translationEntity.setText(text.toString());
            translationRepository.save(translationEntity);
            eventBus.publishEvent(new ReloadMessage(this));
            return true;
        }).orElse(false));
    }


    @GetMapping("/{locale}")
    @Operation(summary = "Get translations map (code to text) for locale (ru, en, etc)")
    public Map<String, String> getTranslationsMap(@PathVariable("locale") String locale) {
        final LocaleEntity localeEntity = localeRepository.findByCode(locale).orElseThrow();
        return translationRepository.findAllByLocaleId(localeEntity.getId()).stream()
                .collect(Collectors.toMap(TranslationEntity::getCode, TranslationEntity::getText));
    }

    @GetMapping
    @Operation(summary = "Получить все переводы для всех языков в формате {locale: {code: text}}")
    public Map<String, Map<String, String>> get() {
        final Map<Integer, LocaleEntity> localesByIdMap = localeRepository.findAll().stream()
                .collect(Collectors.toMap(LocaleEntity::getId, Function.identity()));

        final Map<String, Map<String, String>> map = new LinkedHashMap<>();
        for (final TranslationEntity translation : translationRepository.findAll()) {
            final String locale = localesByIdMap.get(translation.getLocaleId()).getCode();
            map.computeIfAbsent(locale, k -> new HashMap<>()).put(translation.getCode(), translation.getText());
        }
        return map;
    }

    @PostMapping
    @Operation(summary = "Создать перевод. Для каждого языка создается дубликат с таким же текстом")
    public ResultDto create(@RequestBody TranslationCreateRequestDto request) {
        final List<TranslationEntity> newTranslations = localeRepository.findAll().stream()
                .map(localeEntity -> {
                    final TranslationEntity translation = new TranslationEntity();
                    translation.setLocaleId(localeEntity.getId());
                    translation.setCode(request.code());
                    translation.setText(request.text());
                    return translation;
                }).toList();


        translationRepository.saveAll(newTranslations);
        eventBus.publishEvent(new ReloadMessage(this));
        return ResultDto.ok();
    }

    @GetMapping("/external")
    @Operation(summary = "Получить все переводы, не связанные с какими-либо сущностями")
    public List<TranslationDto> getExternal() {
        return translationRepository.findAll().stream()
                .filter(TranslationEntity::isExternal)
                .map(e -> new TranslationDto(e.getId(), e.getLocaleId(), e.getCode(), e.getText()))
                .toList();
    }

    @PostMapping("/external")
    @Operation(summary = "Обновить перевод, не связанные с какими-либо сущностями по его id")
    public ResultDto updateExternal(@RequestBody TranslationUpdateDto request) {
        translationRepository.findById(request.id).ifPresentOrElse(e -> {
                    e.setCode(request.code);
                    e.setText(request.text);
                    translationRepository.save(e);
                    eventBus.publishEvent(new ReloadMessage(this));
                },
                () -> {
                    throw new IllegalArgumentException();
                });
        return ResultDto.ok();
    }

    public record TranslationDto(int id, int localeId, String code, String text) {
    }

    public record TranslationUpdateDto(int id, String code, String text) {
    }

    public record TranslationCreateRequestDto(int localeId, String code, String text) {
    }


    public record LocaleDto(int id, String code, boolean isDefault, String localeDescription, boolean show,
                            int showTime) {
    }
}
