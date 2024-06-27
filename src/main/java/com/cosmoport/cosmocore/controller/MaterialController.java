package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.ResultDto;
import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.controller.helper.TranslationHelper;
import com.cosmoport.cosmocore.model.MaterialEntity;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.MaterialRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/material")
public class MaterialController {
    private static final String TEMP_CODE = "NEW_MATERIAL_CODE";
    private static final String CODE_PREFIX = "material_";

    private final MaterialRepository materialRepository;
    private final LocaleRepository localeRepository;
    private final TranslationRepository translationRepository;

    public MaterialController(MaterialRepository materialRepository,
                              LocaleRepository localeRepository,
                              TranslationRepository translationRepository) {
        this.materialRepository = materialRepository;
        this.localeRepository = localeRepository;
        this.translationRepository = translationRepository;
    }

    @Transactional
    @PostMapping
    public ResultDto create(@RequestBody Object name) {
        final MaterialEntity entity = new MaterialEntity();
        entity.setCode(TEMP_CODE);
        MaterialEntity savedEntity = materialRepository.save(entity);
        savedEntity.setCode(CODE_PREFIX + savedEntity.getId());
        materialRepository.save(savedEntity);

        translationRepository.saveAll(
                TranslationHelper.createTranslationForCodeAndDefaultText(localeRepository, savedEntity.getCode(), name.toString())
        );

        return ResultDto.ok();
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResultDto delete(@PathVariable("id") int id) {
        materialRepository.findById(id).ifPresentOrElse(materialEntity -> materialEntity.setDisabled(true), () -> {
            throw new IllegalArgumentException("Material not found");
        });
        return ResultDto.ok();
    }

    @GetMapping
    public List<MaterialDto> getAll(@RequestParam("localeId") int localeId,
                                    @RequestParam(value = "isActive", required = false) Boolean isActive) {
        return materialRepository.findAll().stream()
                .filter(entity -> isActive == null || entity.isDisabled() != isActive)
                .map(entity -> {
                    final TranslationEntity translation =
                            translationRepository.findByLocaleIdAndCode(localeId, entity.getCode()).orElseThrow();
                    return new MaterialDto(entity.getId(), entity.getCode(), translation.getText(), entity.isDisabled());
                }).toList();
    }

    @GetMapping("/all")
    public List<MaterialTranslationsDto> getAllWithTranslations(
            @RequestParam(value = "isActive", required = false) Boolean isActive
    ) {
        return materialRepository.findAll().stream()
                .filter(entity -> isActive == null || entity.isDisabled() != isActive)
                .map(entity ->
                        new MaterialTranslationsDto(entity.getId(), entity.getCode(), entity.isDisabled(),
                                TranslationHelper.getTranslationsByCode(translationRepository, entity.getCode()))).toList();
    }

    @Transactional
    @PostMapping("/{id}")
    @Operation(summary = "Update i18n code")
    public ResultDto update(@PathVariable("id") int id, @RequestBody Object materialCode) {
        materialRepository.findById(id).ifPresentOrElse(materialEntity -> {
            final List<TranslationEntity> translations = translationRepository.findAllByCode(materialEntity.getCode());
            translations.forEach(translation -> translation.setCode(materialCode.toString()));
            translationRepository.saveAll(translations);

            materialEntity.setCode(materialCode.toString());
            materialRepository.save(materialEntity);
        }, () -> {
            throw new IllegalArgumentException("Entity not found");
        });
        return ResultDto.ok();
    }

    public record MaterialDto(int id, String code, String name, boolean isDisabled) {
    }

    public record MaterialTranslationsDto(int id, String code, boolean isDisabled, List<TranslationDto> translations) {
    }
}
