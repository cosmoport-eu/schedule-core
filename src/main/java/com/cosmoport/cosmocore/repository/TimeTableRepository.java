package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.TimetableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TimeTableRepository extends JpaRepository<TimetableEntity, Integer> {

    @Query(value = """
                SELECT MAX(time) as time
                FROM (SELECT start_time AS time
                      FROM TIMETABLE
                      WHERE gate_id = :gateId
                        AND event_date = :eventDate
                      UNION
                      SELECT (start_time + duration_time) AS time
                      FROM TIMETABLE
                      WHERE gate2_id = :gateId
                        AND event_date = :eventDate)
            """, nativeQuery = true)
    Integer getLastTimeForGate(int gateId, String eventDate);

    @Query(value = """
                SELECT *
                FROM TIMETABLE
                WHERE event_date = :eventDate
                  AND (gate_id = :gateId OR gate2_id = :gate2Id)
                  AND start_time > :startTime
                  AND event_status_id != :statusId
                  AND id != :id
                ORDER BY event_date, start_time
                LIMIT 1
            """, nativeQuery = true)
    Optional<TimetableEntity> findNextEventForGate(String eventDate, int gateId, int gate2Id, int startTime, int statusId, int id);
    List<TimetableEntity> findAllByEventDate(String eventDate);

    List<TimetableEntity> findAllByEventDateIsBetween(String eventDateFrom, String eventDateTo);
}
