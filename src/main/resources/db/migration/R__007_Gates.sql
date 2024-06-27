alter table GATE
    add i18n_code TEXT default '' not null;
update GATE
set i18n_code = 'gate_' || id
where i18n_code = '';

create table GATE_dg_tmp
(
    id        INTEGER         not null
        primary key autoincrement,
    i18n_code TEXT default '' not null
);

insert into GATE_dg_tmp(id, i18n_code)
select id, i18n_code
from GATE;

drop table GATE;

alter table GATE_dg_tmp
    rename to GATE;

create unique index GATE_i18n_code_uindex
    on GATE (i18n_code);


INSERT INTO TRANSLATION (locale_id, tr_text, code, is_external)
SELECT 1 AS locale_id, G.i18n_code AS tr_text, G.i18n_code, 0 AS is_external FROM GATE G
UNION ALL
SELECT 2, G.i18n_code, G.i18n_code, 0 FROM GATE G
UNION ALL
SELECT 3, G.i18n_code, G.i18n_code, 0 FROM GATE G;

