package com.cosmoport.core.persistence;

import com.cosmoport.core.dto.EventDtoWithColor;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TimeTableReadOnlyService extends PersistenceService<EventDtoWithColor>{
    private static final String DEFAULT_ORDER = " ORDER BY event_date, start_time";


    @Inject
    public TimeTableReadOnlyService(Logger logger, Provider<DataSource> ds) {
        super(logger, ds);
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
    public List<EventDtoWithColor> getAllFromDates(final String date, final String date2) {
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
                DEFAULT_ORDER;

        return getAllByParams(sql, params.toArray());
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
    public List<EventDtoWithColor> getAllWithFilter(final String date, final Long gateId) {
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

        final StringBuilder sql = new StringBuilder("SELECT t.*, etc.COLOR FROM TIMETABLE t " +
                "left join EVENT_TYPE et on t.event_type_id = et.id " +
                "left join EVENT_TYPE_CATEGORY etc on et.category_id = etc.id ");
        if (hasParams) {
            sql.append(" WHERE ");
            sql.append(hasDate && hasGate ? "event_date = ? AND gate_id = ?" :
                    hasDate ? "event_date = ?" : "gate_id = ?");
        }
        sql.append(DEFAULT_ORDER);
        getLogger().debug("sql={}", sql);

        return getAllByParams(sql.toString(), params.toArray());
    }

    @Override
    protected EventDtoWithColor map(ResultSet rs) throws SQLException {
        return new EventDtoWithColor(
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
