package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.EventStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventStateRepository extends JpaRepository<EventStateEntity, Integer> {
}
