package com.cosmoport.cosmocore.controller.helper;

import com.cosmoport.cosmocore.controller.dto.TranslationDto;
import com.cosmoport.cosmocore.model.TranslationEntity;
import com.cosmoport.cosmocore.repository.LocaleRepository;
import com.cosmoport.cosmocore.repository.TranslationRepository;

import java.util.List;

public final class TranslationHelper {
    private TranslationHelper() {
    }

    public static List<TranslationDto> getTranslationsByCode(TranslationRepository translationRepository,
                                                             String code) {
        return translationRepository.findAllByCode(code).stream()
                .map(entity -> new TranslationDto(entity.getId(), entity.getLocaleId(), entity.getText()))
                .toList();
    }

    public static List<TranslationEntity> createTranslationForCodeAndDefaultText(
            LocaleRepository localeRepository,
            String code,
            String text
    ) {
        return localeRepository.findAll().stream()
                .map(localeEntity -> {
                    final TranslationEntity translation = new TranslationEntity();
                    translation.setLocaleId(localeEntity.getId());
                    translation.setCode(code);
                    translation.setText(text);
                    return translation;
                }).toList();
    }
}
