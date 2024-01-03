delete from TRANSLATION where i18n_id in (select i18n_event_destination_name from EVENT_DESTINATION);
delete from I18N where id in (select i18n_event_destination_name from EVENT_DESTINATION);
delete from TRANSLATION where i18n_id = 3;
delete from I18N where id = 3;
update TIMETABLE set event_destination_id = null where event_destination_id is not null;
drop table EVENT_DESTINATION;
