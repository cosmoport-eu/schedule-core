create table FACILITY
(
    id        INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    i18n_code VARCHAR(255) NOT NULL UNIQUE
);

create table MATERIAL
(
    id        INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    i18n_code VARCHAR(255) NOT NULL UNIQUE
);

create table EVENT_TYPE__MATERIAL
(
    id           INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    event_type_id INTEGER      NOT NULL,
    material_id   INTEGER      NOT NULL,
    FOREIGN KEY (event_type_id) REFERENCES EVENT_TYPE (id) on delete cascade,
    FOREIGN KEY (material_id) REFERENCES MATERIAL (id) on delete cascade
);

create table EVENT_TYPE__FACILITY
(
    id           INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    event_type_id INTEGER      NOT NULL,
    facility_id   INTEGER      NOT NULL,
    FOREIGN KEY (event_type_id) REFERENCES EVENT_TYPE (id) on delete cascade,
    FOREIGN KEY (facility_id) REFERENCES FACILITY (id) on delete cascade
);