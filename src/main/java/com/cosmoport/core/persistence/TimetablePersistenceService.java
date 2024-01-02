package com.cosmoport.core.persistence;

import com.cosmoport.core.dto.EventDto;
import com.cosmoport.core.persistence.constant.EventState;
import com.cosmoport.core.persistence.constant.EventStatus;
import com.cosmoport.core.persistence.exception.UniqueConstraintException;
import com.cosmoport.core.persistence.exception.ValidationException;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * The Timetable (events) database entity service.
 *
 * @since 0.1.0
 */
public class TimetablePersistenceService extends PersistenceService<EventDto> {
    private final SettingsPersistenceService settingsPersistenceService;
    private static final String defaultOrder = " ORDER BY event_date, start_time";

    @Inject
    public TimetablePersistenceService(
            Logger logger,
            Provider<DataSource> ds,
            SettingsPersistenceService settingsPersistenceService) {
        super(logger, ds);

        this.settingsPersistenceService = settingsPersistenceService;
    }

    /**
     * Fetches all events from the table without any conditions.
     *
     * @return A collection of {@code TimetableDto} objects or an empty list.
     * @throws RuntimeException In case of any exception during fetch procedure.
     * @since 0.1.0
     */
    public List<EventDto> getAll() throws RuntimeException {
        return getAll("SELECT * FROM TIMETABLE" + defaultOrder);
    }

    /**
     * Fetches all events from the table.
     * Uses hardcoded params, oh well.
     *
     * @param date   null or the date string to filter with formatted in yyyy-mm-dd supposedly.
     * @param gateId null or the id number to filter by a gate id.
     * @return A collection of {@code TimetableDto} objects or an empty list.
     * @throws RuntimeException In case of any exception during fetch procedure.
     * @since 0.1.0
     */
    public List<EventDto> getAllWithFilter(final String date, final Long gateId) {
        getLogger().debug("date={}, gateId={}", date, gateId);

        final boolean hasDate = date != null && !date.isEmpty();
        final boolean hasGate = gateId != null && gateId != 0;
        final boolean hasParams = hasDate || hasGate;

        final List<Object> params = new ArrayList<>();
        if (hasDate) {
            params.add(date);
        }
        if (hasGate) {
            params.add(gateId);
        }

        final StringBuilder sql = new StringBuilder("SELECT * FROM TIMETABLE");
        if (hasParams) {
            sql.append(" WHERE ");
            sql.append(hasDate && hasGate ? "event_date = ? AND gate_id = ?" :
                    hasDate ? "event_date = ?" : "gate_id = ?");
        }
        sql.append(defaultOrder);
        getLogger().debug("sql={}", sql);

        return getAllByParams(sql.toString(), params.toArray());
    }

    /**
     * Fetches all the events from the database by a date period.
     *
     * @param date  null or the start date in yyyy-mm-dd format.
     * @param date2 null or the end date in yyyy-mm-dd format.
     * @return A collection of {@code TimetableDto} objects or an empty list.
     * @throws RuntimeException In case of any exception during fetch procedure.
     * @since 0.1.3
     */
    public List<EventDto> getAllFromDates(final String date, final String date2) {
        final boolean hasDate1 = date != null && !date.isEmpty();
        final boolean hasDate2 = date2 != null && !date2.isEmpty();

        final List<Object> params = new ArrayList<>();
        if (hasDate1) {
            params.add(date);
        }
        if (hasDate2) {
            params.add(date2);
        }

        final String sql = "SELECT t.*, etc.COLOR FROM TIMETABLE t " +
                "left join EVENT_TYPE et on t.event_type_id = et.id " +
                "left join EVENT_TYPE_CATEGORY etc on et.category_id = etc.id WHERE " +
                (hasDate1 && hasDate2 ? "event_date BETWEEN ? AND ? " :
                        hasDate2 ? "event_date <= ? " :
                                hasDate1 ? "event_date >= ? " : "") +
                defaultOrder;

        return getAllByParams(sql, params.toArray());
    }

    /**
     * Fetches all events from the table with simple page * count params.
     *
     * @return A collection of {@code TimetableDto} objects or an empty list.
     * @throws RuntimeException In case of any exception during the fetch procedure.
     * @since 0.1.2
     */
    private List<EventDto> getAllPage(final int page, final int count, final String date) throws RuntimeException {
        //noinspection UnnecessaryBoxing
        return getAllByParams("SELECT * FROM TIMETABLE WHERE event_date = ? " + defaultOrder + " LIMIT ?, ?",
                date, Integer.valueOf((page - 1) * count), Integer.valueOf(count));
    }

    public List<EventDto> getAllPage(final int page, final int count) throws RuntimeException {
      return getAllPage(page, count, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    /**
     * Fetches an event by its {@code id} and one following.
     * This needs for the Gate app only.
     * <p>
     * TODO One query
     * </p>
     *
     * @param id The {@code id} of the event.
     * @return A list of one or two events.
     * @throws RuntimeException In case of any exception during the fetch procedure.
     * @since 0.1.2
     */
    public List<EventDto> getCustomByIdForGate(final long id) throws RuntimeException {
        List<EventDto> result = new ArrayList<>();

        // Get the main event
        final Optional<EventDto> mainEvent = findById("SELECT * FROM TIMETABLE WHERE id = ?", id);
        // If exists then select the next one
        if (mainEvent.isPresent()) {
            final EventDto main = mainEvent.get();
            //noinspection UnnecessaryBoxing
            final List<EventDto> nextEvent = getAllByParams(
                    "SELECT * FROM TIMETABLE WHERE event_date = ? AND (gate_id = ? OR gate2_id = ?) AND start_time > ? " +
                            "AND event_status_id <> ? AND id <> ? " +
                            defaultOrder + " LIMIT 1",
                    main.getEventDate(),
                    Long.valueOf(main.getGateId()),
                    Long.valueOf(main.getGate2Id()),
                    Long.valueOf(main.getStartTime()),
                    Integer.valueOf(EventStatus.CANCELED.value()),
                    Long.valueOf(main.getId()));
            // Compile the result
            result.add(main);
            if (!nextEvent.isEmpty()) {
                result.add(nextEvent.get(0));
            }
        }

        return result;
    }


    /**
     * Validates times of an event object.
     * <p>
     * There's shouldn't be two events for one gate with:
     * - the same start time
     * - the same end time (which is sum of the start time and the duration)
     * - overlapping pre-boarding and pre-return periods (value in settings)
     * </p>
     *
     * @param event The event to validate.
     * @throws ValidationException In case of violation of a constraint.
     * @since 0.1.0
     */
    private void validateTime(final EventDto event) throws ValidationException {
        final long startTime = event.getStartTime();
        final long endTime = event.getStartTime() + event.getDurationTime();

        final Object[] params = {
                event.getId(), event.getEventDate(),
                event.getGateId(), event.getGate2Id(), event.getGateId(), event.getGate2Id(),
                startTime, endTime, startTime, endTime
        };

        final List<EventDto> overlapping = getAllByParams(
                "SELECT DISTINCT * FROM TIMETABLE WHERE id <> ? AND event_date = ? AND " +
                        "(" +
                        "(gate_id IN (?, ?) OR gate2_id IN (?, ?)) AND " +
                        "(start_time IN (?, ?) OR start_time + duration_time IN (?, ?))" +
                        ")",
                params);

        if (!overlapping.isEmpty()) {
            String divider = "";
            StringBuilder message = new StringBuilder();
            message.append("Overlapping events: ");

            for (final EventDto event0 : overlapping) {
                message
                        .append(divider)
                        .append("id: ")
                        .append(event0.getId())
                        .append(" [gate: ")
                        .append(event0.getGateId())
                        .append("→")
                        .append(event0.getGate2Id())
                        .append("] start: ")
                        .append(minutesToHm(event0.getStartTime()))
                        .append(", end: ")
                        .append(minutesToHm(event0.getStartTime() + event0.getDurationTime()));
                divider = " | ";
            }

            throw new ValidationException(message.toString());
        }
    }

    public int getLastTimeForGate(final long gateId, final String date) throws RuntimeException {
        int result = -1;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();

            statement = conn.prepareStatement("SELECT MAX(time) as time FROM " +
                    "(SELECT start_time AS time FROM TIMETABLE WHERE gate_id = ? AND event_date = ?" +
                    "UNION " +
                    "SELECT (start_time + duration_time) AS time FROM TIMETABLE " +
                    "WHERE gate2_id = ? AND event_date = ?)");
            statement.setLong(1, gateId);
            statement.setString(2, date);
            statement.setLong(3, gateId);
            statement.setString(4, date);

            rs = statement.executeQuery();

            if (rs.next()) {
                final int value = rs.getInt("time");
                result = rs.wasNull() ? -1 : value;
            }
        } catch (Exception e) {
            throwServerApiException(e);
        } finally {
            close(rs, statement, conn);
        }

        return result;
    }

    /**
     * Validates pre-event periods of an event object.
     * <p>
     * There's shouldn't be two events for one gate with:
     * - overlapping pre-boarding and pre-return periods (the value in settings)
     * </p>
     *
     * @param event The event to validate.
     * @throws ValidationException In case of violation of a constraint.
     * @since 0.1.3
     */
    private void validatePeriod(final EventDto event) throws ValidationException {
        final long startTime = event.getStartTime();
        final long endTime = event.getStartTime() + event.getDurationTime();
        final int period = settingsPersistenceService.getPreEventPeriod();

        final Object[] params = {
                event.getId(), event.getEventDate(),
                event.getGateId(), event.getGate2Id(), event.getGateId(), event.getGate2Id(),
                startTime - period, startTime, endTime - period, endTime
        };

        final List<EventDto> overlapping = getAllByParams(
                "SELECT DISTINCT * FROM TIMETABLE WHERE id <> ? AND event_date = ? AND " +
                        "(" +
                        "(gate_id IN (?, ?) OR gate2_id IN (?, ?)) AND " +
                        "(start_time > ? AND start_time < ? OR start_time + duration_time > ? AND start_time + duration_time < ?)" +
                        ")",
                params);

        if (!overlapping.isEmpty()) {
            String divider = "";
            StringBuilder message = new StringBuilder();
            message.append("Overlapping pre-periods of events: ");

            for (final EventDto event0 : overlapping) {
                message
                        .append(divider)
                        .append("id: ")
                        .append(event0.getId())
                        .append(" [gate: ")
                        .append(event0.getGateId())
                        .append("→")
                        .append(event0.getGate2Id())
                        .append("] start: ")
                        .append(minutesToHm(event0.getStartTime()))
                        .append(" - ")
                        .append(period)
                        .append(", end: ")
                        .append(minutesToHm(event0.getStartTime() + event0.getDurationTime()))
                        .append(" - ")
                        .append(period);
                divider = " | ";
            }

            throw new ValidationException(message.toString());
        }
    }

    private String minutesToHm(final long minutes) {
        return LocalTime.MIN.plus(Duration.ofMinutes(minutes)).toString();
    }

    /**
     * Saves new event record.
     *
     * @param record The record to save.
     * @return A new record with id.
     * @throws UniqueConstraintException In case of unique constraint violation during save.
     * @throws ValidationException       In case of validation failure before save.
     * @throws RuntimeException          In case of any other exception during save.
     * @since 0.1.0
     */
    public EventDto save(final EventDto record) throws RuntimeException {
        validateTime(record);
        validatePeriod(record);

        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();

            // Is it new for insert or an old one for the update
            statement = record.getId() > 0 ?
                    conn.prepareStatement(
                            "UPDATE TIMETABLE SET event_date = ?, event_type_id = ?, " +
                                    "event_state_id = ?, event_status_id = ?, event_destination_id = ?,  " +
                                    "gate_id = ?, gate2_id = ?, start_time = ?, duration_time = ?, " +
                                    "repeat_interval = ?, cost = ?, people_limit = ?, contestants = ? " +
                                    "WHERE id = ?"
                    ) : conn.prepareStatement(
                    "INSERT INTO TIMETABLE (event_date, event_type_id, event_state_id, " +
                            "event_status_id, event_destination_id, gate_id, gate2_id, " +
                            "start_time, duration_time, repeat_interval, cost, people_limit, contestants) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, record.getEventDate());
            statement.setLong(2, record.getEventTypeId());
            statement.setLong(3, record.getEventStateId());
            statement.setLong(4, record.getEventStatusId());
            statement.setLong(5, record.getEventDestinationId());
            statement.setLong(6, record.getGateId());
            statement.setLong(7, record.getGate2Id());
            statement.setLong(8, record.getStartTime());
            statement.setLong(9, record.getDurationTime());
            statement.setLong(10, record.getRepeatInterval());
            statement.setDouble(11, record.getCost());
            statement.setLong(12, record.getPeopleLimit());
            statement.setLong(13, record.getContestants());
            if (record.getId() > 0) {
                statement.setLong(14, record.getId());
            }

            if (statement.executeUpdate() < 0) {
                throw new Exception();
            }

            if (record.getId() == 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setId(generatedKeys.getLong(1));
                    } else {
                        throw new Exception();
                    }
                }
            }
        } catch (SQLException sqlexception) {
            throwConstrainViolation(sqlexception);
            // if not than
            throwServerApiException(sqlexception);
        } catch (Exception e) {
            throwServerApiException(e);
        } finally {
            close(statement, conn);
        }

        return record;
    }

    /**
     * Updates sold tickets in the database (for convenience).
     *
     * @param eventId An {@code id} of the record to update.
     * @param value   A new value of tickets.
     * @param force   Force to reopen event.
     * @throws RuntimeException in case of any errors.
     * @since 0.1.3
     */
    public void updateTicketsForced(final long eventId, final int value, final boolean force) throws RuntimeException {
        // Validation for tickets
        // ...

        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();

            statement = conn.prepareStatement(
                    force ? "UPDATE TIMETABLE SET contestants = ?, event_state_id = ? WHERE id = ?" :
                            "UPDATE TIMETABLE SET contestants = ? WHERE id = ?"
            );
            statement.setInt(1, value);
            if (force) {
                statement.setLong(2, EventState.OPENED.value());
                statement.setLong(3, eventId);
            } else {
                statement.setLong(2, eventId);
            }

            if (statement.executeUpdate() < 0) {
                throw new Exception("Weren't updated.");
            }
        } catch (SQLException sqlexception) {
            throwConstrainViolation(sqlexception);
            // if not than
            throwServerApiException(sqlexception);
        } catch (Exception e) {
            throwServerApiException(e);
        } finally {
            close(statement, conn);
        }
    }

    public void updateTickets(final long eventId, final int value) throws RuntimeException {
        updateTicketsForced(eventId, value, false);
    }

    /**
     * Deletes an event.
     *
     * @param id The id number of the event to delete.
     * @return bool Whether or not record has been deleted.
     * @throws RuntimeException In case of an exception during delete.
     * @since 0.1.0
     */
    public boolean delete(final long id) {
        return deleteByIdWithParams("DELETE FROM TIMETABLE WHERE id = ?", id) == 1;
    }

    @Override
    protected EventDto map(ResultSet rs) throws SQLException {
        return new EventDto(
                rs.getLong("id"),
                rs.getString("event_date"),
                rs.getLong("event_type_id"),
                rs.getString("color"),
                rs.getLong("event_state_id"),
                rs.getLong("event_status_id"),
                rs.getLong("event_destination_id"),
                rs.getLong("gate_id"),
                rs.getLong("gate2_id"),
                rs.getLong("start_time"),
                rs.getLong("duration_time"),
                rs.getLong("repeat_interval"),
                rs.getDouble("cost"),
                rs.getLong("people_limit"),
                rs.getLong("contestants"),
                rs.getString("date_added")
        );
    }
}
