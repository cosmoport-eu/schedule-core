package com.cosmoport.cosmocore.events;

import org.springframework.context.ApplicationEvent;

public class TimeoutUpdateMessage extends ApplicationEvent {
    public static final String TOKEN = ":timeout_update";

    public TimeoutUpdateMessage(Object source) {
        super(source);
    }
}
