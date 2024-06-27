alter table TIMETABLE add column description varchar(2048);

create table TIMETABLE__MATERIAL
(
    id           INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    timetable_id INTEGER      NOT NULL,
    material_id   INTEGER      NOT NULL,
    FOREIGN KEY (timetable_id) REFERENCES TIMETABLE (id) on delete cascade,
    FOREIGN KEY (material_id) REFERENCES MATERIAL (id) on delete cascade
);

create table TIMETABLE__FACILITY
(
    id           INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    timetable_id INTEGER      NOT NULL,
    facility_id   INTEGER      NOT NULL,
    FOREIGN KEY (timetable_id) REFERENCES TIMETABLE (id) on delete cascade,
    FOREIGN KEY (facility_id) REFERENCES FACILITY (id) on delete cascade
);
