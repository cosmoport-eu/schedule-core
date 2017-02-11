package com.cosmoport.core.persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

final class TranslationPersistenceServiceTest extends PersistenceTest {
    @Test
    @DisplayName("Should be able to execute getAll()")
    void getAll() {
        final TranslationPersistenceService service = new TranslationPersistenceService(getDataSourceProvider());

        Assertions.assertEquals(3, service.getAll().size());
    }
}