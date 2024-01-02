package com.cosmoport.core.persistence;

import com.cosmoport.core.dto.request.CreateEventTypeCategoryRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTypeCategoryPersistenceServiceTest extends PersistenceTest {
    private EventTypeCategoryPersistenceService service;
    private TranslationPersistenceService translationPersistenceService;

    @BeforeEach
    void createPersistenceService() {
        super.before();
        var i18nPersistenceService = new I18nPersistenceService(getLogger(), getDataSourceProvider());
        translationPersistenceService = new TranslationPersistenceService(
                getDataSourceProvider(),
                i18nPersistenceService,
                new LocalePersistenceService(getLogger(), getDataSourceProvider())
        );

        service = new EventTypeCategoryPersistenceService(
                getLogger(), getDataSourceProvider(), i18nPersistenceService, translationPersistenceService);
    }

    @Test
    @Disabled("не работал еще до доработок по цветам")
    @DisplayName("Should be able to execute getAll()")
    void getAll() {
        var result = service.getAll();

        assertAll(
                () -> assertEquals(3, result.size()),
                () -> {
                    for (var i : result) {
                        assertTrue(i.getI18nEventTypeCategoryName() > 0);
                    }
                }
        );
    }

    @Test
    @DisplayName("Should be able to create new event type categories")
    void create() {
        final var rq = new CreateEventTypeCategoryRequestDto("test", "#000000");

        final var result = service.create(rq);
        final var trs = translationPersistenceService.findAllByI18n(result.getI18nEventTypeCategoryName());

        assertAll(
                () -> assertTrue(result.getId() > 0),
                () -> assertEquals(rq.name(), trs.get(0).getText()),
                // same names are not allowed
                () -> assertThrows(RuntimeException.class, () -> service.create(rq))
        );
    }
}
