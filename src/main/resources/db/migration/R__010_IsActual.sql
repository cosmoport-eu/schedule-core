alter table EVENT_TYPE_CATEGORY
    add is_disabled boolean default false not null;

alter table EVENT_TYPE
    add is_disabled boolean default false not null;

alter table EVENT_STATUS
    add is_disabled boolean default false not null;

alter table EVENT_STATE
    add is_disabled boolean default false not null;

alter table GATE
    add is_disabled boolean default false not null;

alter table FACILITY
    add is_disabled boolean default false not null;

alter table MATERIAL
    add is_disabled boolean default false not null;