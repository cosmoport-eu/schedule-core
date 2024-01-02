package com.cosmoport.core.persistence;

import com.cosmoport.core.dto.*;
import com.cosmoport.core.dto.request.CreateEventTypeRequestDto;
import com.cosmoport.core.persistence.exception.UniqueConstraintException;
import com.cosmoport.core.persistence.exception.ValidationException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Event type entity database service.
 *
 * @since 0.1.0
 */
public final class EventTypePersistenceService extends PersistenceService<EventTypeDto> {
    private final I18nPersistenceService i18nPersistenceService;
    private final TranslationPersistenceService translationPersistenceService;
    private final TimetablePersistenceService timetablePersistenceService;
    private final EventTypeCategoryPersistenceService eventTypeCategoryPersistenceService;

    @Inject
    EventTypePersistenceService(Logger logger, Provider<DataSource> ds,
                                I18nPersistenceService i18nPersistenceService,
                                TranslationPersistenceService translationPersistenceService,
                                TimetablePersistenceService timetablePersistenceService,
                                EventTypeCategoryPersistenceService eventTypeCategoryPersistenceService) {
        super(logger, ds);
        this.i18nPersistenceService = i18nPersistenceService;
        this.translationPersistenceService = translationPersistenceService;
        this.timetablePersistenceService = timetablePersistenceService;
        this.eventTypeCategoryPersistenceService = eventTypeCategoryPersistenceService;
    }

    @Override
    protected EventTypeDto map(final ResultSet rs) throws SQLException {
        return new EventTypeDto(
                rs.getLong("id"),
                rs.getLong("category_id"),
                rs.getLong("i18n_event_type_name"),
                rs.getLong("i18n_event_type_description"),
                rs.getInt("default_duration"),
                rs.getInt("default_repeat_interval"),
                rs.getDouble("default_cost")
        );
    }

    public List<EventTypeDto> getAll() {
        return getAll("SELECT * FROM EVENT_TYPE");
    }

    public Optional<EventTypeDto> getById(final long id) {
        return findById("SELECT * FROM EVENT_TYPE WHERE id = ?", id);
    }

    /**
     * Validates an event type object.
     * <p>
     * There's shouldn't be two events types with:
     * - the same name in one category/subcategory
     * </p>
     *
     * @param cat  The category id
     * @param name The name of the category
     * @throws ValidationException In case of violation of a constraint.
     * @since 0.1.0
     */
    private void validate(long cat, String name) throws ValidationException {
        // TODO Be aware of the default translation, mate

        final List<EventTypeDto> duplicates = getAllByParams(
                "SELECT et.* FROM EVENT_TYPE et " +
                        "LEFT JOIN TRANSLATION t0 ON et.i18n_event_type_name = t0.i18n_id AND t0.locale_id = 1 " +
                        "LEFT JOIN EVENT_TYPE_CATEGORY etc ON etc.id = et.category_id " +
                        "LEFT JOIN TRANSLATION t1 ON t1.i18n_id = etc.i18n_event_type_category_name AND t1.locale_id = 1 " +
                        "WHERE (et.category_id = ? OR etc.parent = ?) AND (t0.tr_text = ? OR t1.tr_text = ?) " +
                        "LIMIT 1",
                cat, cat, name, name);

        if (!duplicates.isEmpty()) {
            throw new ValidationException("Duplicate event: '" + name + "' in the category " + cat);
        }
    }

    /**
     * Saves new event type record.
     *
     * @param eventType The record to save.
     * @return A list of created event types and event categories.
     * @throws UniqueConstraintException In case of unique constraint violation during save.
     * @throws ValidationException       In case of validation failure before save.
     * @throws RuntimeException          In case of any other exception during save.
     * @since 0.1.0
     */
    public EventTypeSaveResultDto save(final CreateEventTypeRequestDto eventType) throws RuntimeException {
        validate(eventType.categoryId(), eventType.name());

        final var eTypes = new ArrayList<EventTypeDto>();
        final var eCatTypes = new ArrayList<EventTypeCategoryDto>();

        Connection conn = null;
        try {
            conn = getConnection();
            // disable auto-commit because everything will be in one transaction
            conn.setAutoCommit(false);

            // get an uid for i18n tag name auto-generation
            String uid = shortUUID();

            // here we build type hierarchy
            if (eventType.subtypes() != null && !eventType.subtypes().isEmpty()) {
                final var eventTypeI18nSub =
                        i18nPersistenceService.save(I18nDto.Short("event_type_subcategory_" + uid), conn);
                translationPersistenceService.saveWithDefaultTranslation(eventTypeI18nSub.getId(), eventType.name(), conn);

                final String color = eventTypeCategoryPersistenceService.findById(eventType.categoryId()).orElseThrow()
                        .getColor();
                // save the name as a new category
                final var eventSubcategory = eventTypeCategoryPersistenceService.save(
                        new EventTypeCategoryDto(0L, eventTypeI18nSub.getId(), eventType.categoryId(), color), conn);

                eCatTypes.add(eventSubcategory);

                // link the category to a list of event types
                var i = 0;
                for (var subtype : eventType.subtypes()) {
                    var newEventType = saveEventType(uid, i + 1,
                            new EventTypeDto(0L, eventSubcategory.getId(), 0L, 0L,
                                    eventType.defaultDuration(), eventType.defaultRepeatInterval(), eventType.defaultCost()
                            ),
                            subtype.name(), subtype.description(), conn);
                    eTypes.add(newEventType);
                    i++;
                }
                // here we add just one event of a category
            } else {
                var newEventType = saveEventType(
                        uid,
                        1,
                        new EventTypeDto(0L, eventType.categoryId(), 0L, 0L,
                                eventType.defaultDuration(), eventType.defaultRepeatInterval(), eventType.defaultCost()
                        ),
                        eventType.name(), eventType.description(), conn);
                eTypes.add(newEventType);
            }

            conn.commit();
        } catch (SQLException sqlexception) {
            rollback(conn);
            throwConstrainViolation(sqlexception);
            throwServerApiException(sqlexception);
        } catch (Exception e) {
            rollback(conn);
            throwServerApiException(e);
        } finally {
            close(conn);
        }

        return new EventTypeSaveResultDto(eCatTypes, eTypes);
    }

    private EventTypeDto saveEventType(String uid, int i, EventTypeDto ev, String name, String description, Connection conn) throws Exception {
        // Actually, it's possible with only one SQL query
        // Make new i18n records
        final var eventTypeI18nName = i18nPersistenceService.save(I18nDto.Short("event_type_name_" + uid + "_" + i), conn);
        final var eventTypeI18nDescription = i18nPersistenceService.save(I18nDto.Short("event_type_description_" + uid + "_" + i), conn);

        // Link it with an event type record
        ev.setI18nEventTypeName(eventTypeI18nName.getId());
        ev.setI18nEventTypeDescription(eventTypeI18nDescription.getId());

        final var sql = "INSERT INTO EVENT_TYPE (category_id, i18n_event_type_name, " +
                "i18n_event_type_description, default_duration, default_repeat_interval, default_cost) " +
                " VALUES (?, ?, ?, ?, ?, ?)";

        try (var statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (ev.getCategoryId() != 0) {
                statement.setLong(1, ev.getCategoryId());
            } else {
                statement.setNull(1, Types.BIGINT);
            }
            statement.setLong(2, ev.getI18nEventTypeName());
            statement.setLong(3, ev.getI18nEventTypeDescription());
            statement.setLong(4, ev.getDefaultDuration());
            statement.setLong(5, ev.getDefaultRepeatInterval());
            statement.setDouble(6, ev.getDefaultCost());

            if (statement.executeUpdate() < 0) {
                throw new Exception();
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    ev.setId(generatedKeys.getLong(1));
                } else {
                    throw new Exception();
                }
            }
        }

        // add the translation for the default locale and copy it over to all other existing locales
        translationPersistenceService.saveWithDefaultTranslation(ev.getI18nEventTypeName(), name, conn);
        translationPersistenceService.saveWithDefaultTranslation(ev.getI18nEventTypeDescription(), description, conn);

        return ev;
    }

    public int delete(final long id) throws RuntimeException {
        Connection conn = null;
        PreparedStatement statement = null;
        int deleted = 0;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            if (timetablePersistenceService.findById(
                    "SELECT * FROM TIMETABLE WHERE event_type_id = ?", id).isPresent()) {
                throw new Exception("No delete. Some events were linked with this type.");
            }

            // Deletes the main record
            statement = conn.prepareStatement("DELETE FROM EVENT_TYPE WHERE id = ?");
            statement.setLong(1, id);

            final int deleted2 = statement.executeUpdate();
            int deleted3 = 0;

            if (deleted2 + deleted3 < 0) {
                throw new Exception();
            }

            deleted = deleted2 + deleted3;

            conn.commit();
        } catch (SQLException sqlexception) {
            rollback(conn);
            throwConstrainViolation(sqlexception);
            throwServerApiException(sqlexception);
        } catch (Exception e) {
            rollback(conn);
            throwServerApiException(e);
        } finally {
            close(statement, conn);
        }

        return deleted;
    }

    private String shortUUID() {
        return Long.toString(ByteBuffer.wrap(UUID.randomUUID().toString().getBytes()).getLong(), Character.MAX_RADIX);
    }
}
