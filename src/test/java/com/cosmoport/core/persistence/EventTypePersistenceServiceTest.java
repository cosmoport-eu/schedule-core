package com.cosmoport.core.persistence;

import com.cosmoport.core.dto.request.CreateEventSubTypeRequestDto;
import com.cosmoport.core.dto.request.CreateEventTypeRequestDto;
import com.cosmoport.core.persistence.exception.ValidationException;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class EventTypePersistenceServiceTest extends PersistenceTest {
    private EventTypePersistenceService service;
    private EventTypeCategoryPersistenceService eventTypeCategoryPersistenceService;
    private TranslationPersistenceService translationService;
    private I18nPersistenceService i18nService;

    @BeforeEach
    void createPersistenceService() {
        super.before();

        i18nService = new I18nPersistenceService(getLogger(), getDataSourceProvider());
        translationService = new TranslationPersistenceService(
                getDataSourceProvider(),
                i18nService,
                new LocalePersistenceService(getLogger(), getDataSourceProvider())
        );
        eventTypeCategoryPersistenceService = new EventTypeCategoryPersistenceService(getLogger(), getDataSourceProvider(), i18nService, translationService);
        final var timetablePersistenceService =
                new TimetablePersistenceService(getLogger(), getDataSourceProvider(),
                        new SettingsPersistenceService(getLogger(), getDataSourceProvider()));
        service = new EventTypePersistenceService(getLogger(), getDataSourceProvider(),
                i18nService, translationService, timetablePersistenceService, eventTypeCategoryPersistenceService);
    }

    @Test
    @Disabled("не работал еще до доработок по цветам")
    @DisplayName("Should be able to execute getAll()")
    void getAll() {
        Assertions.assertEquals(4, service.getAll().size());
    }

    @Nested
    @DisplayName("Should be able to validate input values")
    class ValidationTest {
        @Test
        @DisplayName("Validate duplicates for the same event names in one category")
        void validateSameName() {
            var event = new CreateEventTypeRequestDto(1, "Station lunch", "", Collections.emptyList(), 0, 0, 0);
            final Throwable exception = assertThrows(ValidationException.class, () -> service.save(event));
            assertEquals("Duplicate event: 'Station lunch' in the category 1", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Should be able to save/get/delete objects")
    class PersistenceTest {
        final int newCount = 5;
        final int localesCount = 3;
        final List<CreateEventSubTypeRequestDto> noSubtypes = Collections.emptyList();

        @Test
        @Disabled("не работал еще до доработок по цветам")
        @DisplayName("Should save new event type")
        void save() {
            final var store = service.save(
                    new CreateEventTypeRequestDto(0, "event_name", "event_description", noSubtypes, 0, 0, 0));
            assertAll("checks",
                    // created
                    () -> assertEquals(newCount, store.eventTypes().get(0).getId()),
                    // has i18ns
                    () -> assertTrue(i18nService.findByTag("event_type_name_" + newCount + "_1").isPresent()),
                    () -> assertTrue(i18nService.findByTag("event_type_description_" + newCount + "_1").isPresent()),
                    // has default translations
                    () -> assertEquals(localesCount, translationService.findAllByI18n(store.eventTypes().get(0).getI18nEventTypeName()).size()),
                    () -> assertEquals(localesCount, translationService.findAllByI18n(store.eventTypes().get(0).getI18nEventTypeDescription()).size())
            );
        }

        @Test
        @Disabled
        @DisplayName("The event type hierarchy should be properly saved and removed")
        void saveDeleteHierarchy() {
            final var subtypes = new ArrayList<CreateEventSubTypeRequestDto>();
            final var subType1 = new CreateEventSubTypeRequestDto("subtype1", "subtype1_description");
            final var subType2 = new CreateEventSubTypeRequestDto("subtype2", "subtype2_description");
            subtypes.add(subType1);
            subtypes.add(subType2);

            final int duration = 1;
            final int interval = 2;
            final double cost = 3.33;

            final var store = service.save(
                    new CreateEventTypeRequestDto(0, "event1", "description1", subtypes, duration, interval, cost));

            // we would have just one category
            assertEquals(1, store.eventTypeCategories().size());

            // query the results in the database
            var cat = eventTypeCategoryPersistenceService.findById(store.eventTypeCategories().get(0).getId()).orElseThrow();

            assertAll("",
                    // for the hierarchical type:
                    //   - no parent (null)
                    //   - name is `event1`
                    //   - description is ignored due to the following children:
                    //   - child type 1: subtype1/subtype1_description
                    //   - child type 2: subtype2/subtype2_description
                    //
                    // it should:
                    () -> assertEquals(0, cat.getParent(), "oof, should not have no parent categories in the persisted top category"),
                    () -> assertEquals("event1",
                            translationService.findAllByI18n(cat.getI18nEventTypeCategoryName()).get(0).getText(),
                            "oof, should have the top level category name persisted"),
                    () -> assertEquals(subtypes.size(), store.eventTypes().size(), "oof, should have the correct number of types persisted"),
                    () -> {
                        var i = 0;
                        for (var t : store.eventTypes()) {
                            assertTrue(t.getId() > 0, "oof, one of the event types wasn't persisted");
                            assertEquals(cat.getId(), t.getCategoryId(), "oof, the category for the event type is off");
                            var name = translationService.findAllByI18n(t.getI18nEventTypeName()).get(0);
                            var desc = translationService.findAllByI18n(t.getI18nEventTypeDescription()).get(0);

                            assertEquals(subtypes.get(i).name(), name.getText(), "oof, the name " + subtypes.get(i).name() + " was not persisted");
                            assertEquals(subtypes.get(i).description(), desc.getText(), "oof, the description " + subtypes.get(0).description() + " was not persisted");

                            var event = service.getById(t.getId()).orElseThrow();

                            assertAll("All default values should be saved/restored properly",
                                    () -> assertEquals(duration, event.getDefaultDuration(), "oof, the default duration is wrong"),
                                    () -> assertEquals(interval, event.getDefaultRepeatInterval(), "oof, the default interval is wrong"),
                                    () -> assertEquals(cost, event.getDefaultCost(), "oof, the default cost is wrong")
                            );

                            i++;
                        }
                    }
            );

            // delete
            var isDeleted = eventTypeCategoryPersistenceService.delete(cat.getId());

            assertAll("",
                    () -> assertTrue(isDeleted, "oof, the record was not deleted"),
                    () -> assertEquals(0,
                            translationService.findAllByI18n(cat.getI18nEventTypeCategoryName()).size(),
                            "oof, category i18n was not deleted"),
                    () -> {
                        var i = 0;
                        for (var t : store.eventTypes()) {
                            var deleted = service.delete(t.getId());
                            var name = translationService.findAllByI18n(t.getI18nEventTypeName());
                            var desc = translationService.findAllByI18n(t.getI18nEventTypeDescription());

                            assertTrue(deleted > 0, "oof, nothing was deleted");
                            assertEquals(0, name.size(), "oof, the name " + subtypes.get(i).name() + " was not deleted");
                            assertEquals(0, desc.size(), "oof, the description " + subtypes.get(0).description() + " was not deleted");

                            i++;
                        }
                    }
            );

        }

        @Test
        @DisplayName("Should be able to change the default cost value")
        void defaultCost() {
            final var store = service.save(
                    new CreateEventTypeRequestDto(0, "event_name", "event_description", noSubtypes, 0, 0, 33.33));
            assertAll("checks",
                    // created
                    () -> assertTrue(store.eventTypes().get(0).getId() > 0),
                    // has not default cost
                    () -> assertEquals(33.33, service.getById(store.eventTypes().get(0).getId()).orElseThrow().getDefaultCost())
            );
        }
    }
}
