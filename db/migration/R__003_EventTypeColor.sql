alter table EVENT_TYPE_CATEGORY add column COLOR text;
update EVENT_TYPE_CATEGORY set COLOR = '#f44336' where ID = 1;
update EVENT_TYPE_CATEGORY set COLOR = '#9c27b0' where ID = 2;
update EVENT_TYPE_CATEGORY set COLOR = '#2196f3' where ID = 3;
update EVENT_TYPE_CATEGORY set COLOR = '#009688' where ID = 4;
update EVENT_TYPE_CATEGORY set COLOR = '#41A6f3' where COLOR is null;
