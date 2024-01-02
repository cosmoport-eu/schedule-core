package com.cosmoport.core.persistence.module;

import com.cosmoport.core.db.DatasourceServiceProvider;
import com.cosmoport.core.persistence.*;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import javax.sql.DataSource;

public final class PersistenceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(DataSource.class).toProvider(DatasourceServiceProvider.class).in(Scopes.SINGLETON);

        bind(TimetablePersistenceService.class);
        bind(TimeTableReadOnlyService.class);
        bind(EventTypeCategoryPersistenceService.class);
        bind(EventTypePersistenceService.class);
        bind(EventStatusPersistenceService.class);
        bind(EventStatePersistenceService.class);
        bind(EventDestinationPersistenceService.class);
        bind(GatePersistenceService.class);
        bind(LocalePersistenceService.class);
        bind(TranslationPersistenceService.class);
        bind(I18nPersistenceService.class);
        bind(SettingsPersistenceService.class);
    }
}
