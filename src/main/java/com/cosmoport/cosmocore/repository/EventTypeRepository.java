package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.EventTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EventTypeRepository extends JpaRepository<EventTypeEntity, Integer> {
    @Query(value = """
            SELECT et.*
            FROM EVENT_TYPE et
                     LEFT JOIN TRANSLATION t0 ON et.i18n_event_type_name = t0.i18n_id AND t0.locale_id = 1
                     LEFT JOIN EVENT_TYPE_CATEGORY etc ON etc.id = et.category_id
                     LEFT JOIN TRANSLATION t1 ON t1.i18n_id = etc.i18n_event_type_category_name AND t1.locale_id = 1
            WHERE (et.category_id = :categoryId OR etc.parent = :categoryId)
              AND (t0.tr_text = :name OR t1.tr_text = :name)
            LIMIT 1
            """, nativeQuery = true)
    Optional<EventTypeEntity> findAnyDuplicate(int categoryId, String name);

    boolean existsByNameCode(String nameCode);
    boolean existsByDescCode(String descCode);
}
