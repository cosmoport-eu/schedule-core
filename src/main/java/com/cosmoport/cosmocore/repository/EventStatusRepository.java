package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.EventStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventStatusRepository extends JpaRepository<EventStatusEntity, Integer> {
}
