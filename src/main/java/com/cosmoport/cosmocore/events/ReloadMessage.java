package com.cosmoport.cosmocore.events;

import org.springframework.context.ApplicationEvent;

public class ReloadMessage extends ApplicationEvent {
    public static final String TOKEN = ":reload";

    public ReloadMessage(Object source) {
        super(source);
    }
}
