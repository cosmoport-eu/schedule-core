create table EVENT_TYPE_dg_tmp
(
    id                      INTEGER                     not null
        primary key autoincrement,
    category_id             INTEGER
        references EVENT_TYPE_CATEGORY
            on update cascade on delete cascade,
    default_duration        INTEGER        default 1    not null,
    default_repeat_interval INTEGER        default 0    not null,
    default_cost            DECIMAL(10, 2) default 0.00 not null,
    i18n_name_code          varchar(255),
    i18n_desc_code          varchar(255)
);

insert into EVENT_TYPE_dg_tmp(id, category_id, default_duration, default_repeat_interval, default_cost, i18n_name_code,
                              i18n_desc_code)
select id, category_id, default_duration, default_repeat_interval, default_cost, i18n_name_code, i18n_desc_code
from EVENT_TYPE;

drop table EVENT_TYPE;

alter table EVENT_TYPE_dg_tmp
    rename to EVENT_TYPE;

