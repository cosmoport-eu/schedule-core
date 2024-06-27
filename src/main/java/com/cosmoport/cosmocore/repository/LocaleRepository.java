package com.cosmoport.cosmocore.repository;

import com.cosmoport.cosmocore.model.LocaleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocaleRepository extends JpaRepository<LocaleEntity, Integer> {
    Optional<LocaleEntity> findByCode(String code);
}
