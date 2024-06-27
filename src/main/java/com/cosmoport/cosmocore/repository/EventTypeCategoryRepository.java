package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.EventTypeCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventTypeCategoryRepository extends JpaRepository<EventTypeCategoryEntity, Integer> {
    boolean existsByCode(String code);
}
