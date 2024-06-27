delete
from EVENT_TYPE_CATEGORY
where parent is not null;



create table EVENT_TYPE_dg_tmp
(
    id                      INTEGER                     not null
        primary key autoincrement,
    category_id             INTEGER,
    default_duration        INTEGER        default 1    not null,
    default_repeat_interval INTEGER        default 0    not null,
    default_cost            DECIMAL(10, 2) default 0.00 not null,
    i18n_name_code          varchar(255),
    i18n_desc_code          varchar(255),
    parent_id               integer
        constraint EVENT_TYPE_EVENT_TYPE_id_fk
            references EVENT_TYPE
            on update cascade on delete cascade
);

insert into EVENT_TYPE_dg_tmp(id, category_id, default_duration, default_repeat_interval, default_cost, i18n_name_code,
                              i18n_desc_code)
select id, category_id, default_duration, default_repeat_interval, default_cost, i18n_name_code, i18n_desc_code
from EVENT_TYPE;

drop table EVENT_TYPE;

alter table EVENT_TYPE_dg_tmp
    rename to EVENT_TYPE;



create table EVENT_TYPE_CATEGORY_dg_tmp
(
    id        INTEGER      not null
        primary key autoincrement,
    COLOR     text,
    i18n_code varchar(255) not null on conflict rollback
        constraint EVENT_TYPE_CATEGORY_pk
        unique
        on conflict rollback
);

insert into EVENT_TYPE_CATEGORY_dg_tmp(id, COLOR, i18n_code)
select id, COLOR, i18n_code
from EVENT_TYPE_CATEGORY;

drop table EVENT_TYPE_CATEGORY;

alter table EVENT_TYPE_CATEGORY_dg_tmp
    rename to EVENT_TYPE_CATEGORY;


delete
from EVENT_TYPE
where category_id not in (select id from EVENT_TYPE_CATEGORY)
