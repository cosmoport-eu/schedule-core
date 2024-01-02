package com.cosmoport.core.persistence;

import com.cosmoport.core.dto.EventTypeCategoryDto;
import com.cosmoport.core.dto.I18nDto;
import com.cosmoport.core.dto.request.CreateEventTypeCategoryRequestDto;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

/**
 * Service dedicated to event type categories.
 *
 * @since 0.2.0
 */
public class EventTypeCategoryPersistenceService extends PersistenceService<EventTypeCategoryDto> {
    private final I18nPersistenceService i18nPersistenceService;
    private final TranslationPersistenceService translationPersistenceService;

    @Inject
    protected EventTypeCategoryPersistenceService(Logger logger, Provider<DataSource> ds,
                                                  I18nPersistenceService i18nPersistenceService,
                                                  TranslationPersistenceService translationPersistenceService) {
        super(logger, ds);
        this.i18nPersistenceService = i18nPersistenceService;
        this.translationPersistenceService = translationPersistenceService;
    }

    @Override
    protected EventTypeCategoryDto map(ResultSet rs) throws SQLException {
        return new EventTypeCategoryDto(
                rs.getLong("id"),
                rs.getLong("i18n_event_type_category_name"),
                rs.getLong("parent"),
                rs.getString("color"));
    }

    public Optional<EventTypeCategoryDto> findById(long id) {
        return findById("SELECT * FROM EVENT_TYPE_CATEGORY WHERE id = ?", id);
    }

    public List<EventTypeCategoryDto> getAll() {
        return getAll("SELECT * FROM EVENT_TYPE_CATEGORY");
    }

    private Optional<EventTypeCategoryDto> findDuplicateName(String name) {
        return findByParam("SELECT e.* " +
                "FROM EVENT_TYPE_CATEGORY e " +
                "LEFT JOIN TRANSLATION t on t.i18n_id = e.i18n_event_type_category_name " +
                "WHERE parent IS null AND t.locale_id = 1 AND t.tr_text = ?", name);
    }

    public EventTypeCategoryDto create(CreateEventTypeCategoryRequestDto cat) throws RuntimeException {
        Connection conn = null;
        EventTypeCategoryDto newEventTypeCategory = null;

        // don't allow same names for the parent categories
        final var dup = findDuplicateName(cat.name());
        dup.ifPresent(eventTypeCategoryDto -> throwServerApiException(new Exception("Duplicate name for top categories (" + eventTypeCategoryDto.getId() + ")")));

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // get the max id for i18n tag name auto-generation
            long maxId = this.getMaxId() + 1;

            final var eventTypeI18nSub =
                    i18nPersistenceService.save(I18nDto.Short("event_type_category_" + maxId), conn);
            translationPersistenceService.saveWithDefaultTranslation(eventTypeI18nSub.getId(), cat.name(), conn);

            // save the name as a new category
            newEventTypeCategory = save(new EventTypeCategoryDto(0L, eventTypeI18nSub.getId(), 0, cat.color()), conn);

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

        return newEventTypeCategory;
    }

    public EventTypeCategoryDto save(EventTypeCategoryDto etc, final Connection extConn) throws RuntimeException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = extConn != null ? extConn : getConnection();

            statement = conn.prepareStatement(
                    "INSERT INTO EVENT_TYPE_CATEGORY (i18n_event_type_category_name, parent, color) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, etc.getI18nEventTypeCategoryName());
            if (etc.getParent() > 0) {
                statement.setLong(2, etc.getParent());
            } else {
                statement.setNull(2, Types.BIGINT);
            }
            statement.setString(3, etc.getColor() == null ? "#41A6f3" : etc.getColor());

            if (statement.executeUpdate() < 0) {
                throw new Exception();
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    etc.setId(generatedKeys.getLong(1));
                } else {
                    throw new Exception();
                }
            }
        } catch (SQLException sqlexception) {
            throwConstrainViolation(sqlexception);
            throwServerApiException(sqlexception);
        } catch (Exception e) {
            throwServerApiException(e);
        } finally {
            close(statement);
            if (extConn == null) {
                close(conn);
            }
        }

        return etc;
    }

    public boolean delete(final long id) throws RuntimeException {
        return deleteByIdWithParams("DELETE FROM EVENT_TYPE_CATEGORY WHERE id = ?", id) == 1;
    }

    private long getMaxId() {
        long result = 0;

        Connection conn = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();

            statement = conn.createStatement();
            rs = statement.executeQuery("SELECT MAX(id) FROM EVENT_TYPE_CATEGORY");

            if (rs.next()) {
                result = rs.getLong(1);
            }
        } catch (Exception e) {
            throwServerApiException(e);
        } finally {
            close(rs, statement, conn);
        }

        return result;
    }
}
