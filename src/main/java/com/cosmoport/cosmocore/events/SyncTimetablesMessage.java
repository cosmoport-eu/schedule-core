package com.cosmoport.cosmocore.events;

import org.springframework.context.ApplicationEvent;

public class SyncTimetablesMessage extends ApplicationEvent {
    public static final String TOKEN = ":sync_timetables";
    public SyncTimetablesMessage(Object source) {
        super(source);
    }
}
