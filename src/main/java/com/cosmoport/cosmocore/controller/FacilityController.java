package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.controller.helper.TranslationHelper;
import com.cosmoport.cosmocore.model.FacilityEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.FacilityRepository;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/facility")
public class FacilityController {
    private static final String TEMP_CODE = "NEW_FACILITY_CODE";
    private static final String CODE_PREFIX = "facility_";

    private final FacilityRepository facilityRepository;
    private final LocaleRepository localeRepository;
    private final TranslationRepository translationRepository;

    public FacilityController(FacilityRepository facilityRepository,
                              LocaleRepository localeRepository,
                              TranslationRepository translationRepository) {
        this.facilityRepository = facilityRepository;
        this.localeRepository = localeRepository;
        this.translationRepository = translationRepository;
    }

    @PostMapping
    public ResultDto create(@RequestBody Object name) {
        final FacilityEntity entity = new FacilityEntity();
        entity.setCode(TEMP_CODE);
        FacilityEntity savedEntity = facilityRepository.save(entity);
        savedEntity.setCode(CODE_PREFIX + savedEntity.getId());
        facilityRepository.save(savedEntity);

        translationRepository.saveAll(
                TranslationHelper.createTranslationForCodeAndDefaultText(localeRepository, savedEntity.getCode(), name.toString())
        );

        return ResultDto.ok();
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResultDto delete(@PathVariable("id") int id) {
        facilityRepository.findById(id).ifPresentOrElse(facilityEntity -> facilityEntity.setDisabled(true), () -> {
            throw new IllegalArgumentException("Facility not found");
        });
        return ResultDto.ok();
    }

    @GetMapping
    public List<FacilityDto> getAll(@RequestParam("localeId") int localeId,
                                    @RequestParam(value = "isActive", required = false) Boolean isActive) {
        return facilityRepository.findAll().stream()
                .filter(entity -> isActive == null || entity.isDisabled() != isActive)
                .map(entity -> {
                    final TranslationEntity translation =
                            translationRepository.findByLocaleIdAndCode(localeId, entity.getCode()).orElseThrow();
                    return new FacilityDto(entity.getId(), entity.getCode(), translation.getText(), entity.isDisabled());
                }).toList();
    }

    @GetMapping("/all")
    public List<FacilityDtoWithTranslations> getAllWithTranslations(
            @RequestParam(value = "isActive", required = false) Boolean isActive
    ) {
        return facilityRepository.findAll().stream()
                .filter(entity -> isActive == null || entity.isDisabled() != isActive)
                .map(entity ->
                        new FacilityDtoWithTranslations(entity.getId(), entity.getCode(), entity.isDisabled(),
                                TranslationHelper.getTranslationsByCode(translationRepository, entity.getCode()))).toList();
    }

    @Transactional
    @PostMapping("/{id}")
    @Operation(summary = "Update facility i18n code")
    public ResultDto update(@PathVariable("id") int id, @RequestBody Object facilityCode) {
        facilityRepository.findById(id).ifPresentOrElse(facilityEntity -> {
            final List<TranslationEntity> translations = translationRepository.findAllByCode(facilityEntity.getCode());
            translations.forEach(translation -> translation.setCode(facilityCode.toString()));
            translationRepository.saveAll(translations);

            facilityEntity.setCode(facilityCode.toString());
            facilityRepository.save(facilityEntity);
        }, () -> {
            throw new IllegalArgumentException("Facility not found");
        });
        return ResultDto.ok();
    }

    public record FacilityDto(int id, String code, String name, boolean isDisabled) {
    }

    public record FacilityDtoWithTranslations(int id, String code, boolean isDisabled, List<TranslationDto> translations) {
    }
}
