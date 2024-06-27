drop trigger if exists EVENT_TYPE_AD;
drop trigger if exists EVENT_TYPE_CATEGORY_AD;

alter table event_state add column i18n_code varchar(255);
update event_state set i18n_code = (select tag from i18n where id = event_state.i18n_state)
where exists (select * from i18n where i18n.id = event_state.i18n_state);

alter table event_status add column i18n_code varchar(255);
update event_status set i18n_code = (select tag from i18n where id = event_status.i18n_status)
where exists (select * from i18n where i18n.id = event_status.i18n_status);


alter table event_type add column i18n_name_code varchar(255);
alter table event_type add column i18n_desc_code varchar(255);
update event_type set i18n_name_code = (select tag from i18n where id = event_type.i18n_event_type_name)
where exists (select * from i18n where i18n.id = event_type.i18n_event_type_name);
update event_type set i18n_desc_code = (select tag from i18n where id = event_type.i18n_event_type_description)
where exists (select * from i18n where i18n.id = event_type.i18n_event_type_description);

alter table event_type_category add column i18n_code varchar(255);
update event_type_category set i18n_code = (select tag from i18n where id = event_type_category.i18n_event_type_category_name)
where exists (select * from i18n where i18n.id = event_type_category.i18n_event_type_category_name);

alter table translation add column code varchar(255);
update translation set code = (select tag from i18n where id = translation.i18n_id)
where exists (select * from i18n where i18n.id = translation.i18n_id);


create table TRANSLATION_dg_tmp
(
    id        INTEGER         not null
        primary key autoincrement,
    locale_id INTEGER         not null
        references LOCALE
            on update cascade on delete cascade,
    tr_text   TEXT default '' not null,
    code      varchar(255)    not null,
    is_external boolean default false not null,
    constraint TRANSLATION_CODE_LOCALE unique (code, locale_id)
);

insert into TRANSLATION_dg_tmp(id, locale_id, tr_text, code)
select id, locale_id, tr_text, code
from TRANSLATION;

drop table TRANSLATION;

alter table TRANSLATION_dg_tmp
    rename to TRANSLATION;

update TRANSLATION
set is_external = true
where code in (select tag from I18N where external = 1);
