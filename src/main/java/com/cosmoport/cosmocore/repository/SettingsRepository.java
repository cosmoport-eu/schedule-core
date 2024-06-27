package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.SettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<SettingsEntity, Integer> {
    Optional<SettingsEntity> findByParam(String param);

    default String getValueOrThrow(String param) {
        return findByParam(param).orElseThrow().getValue();
    }
}
