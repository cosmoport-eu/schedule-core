package com.cosmoport.cosmocore.events;

import com.cosmoport.cosmocore.controller.dto.EventDtoRequest;
import org.springframework.context.ApplicationEvent;

public final class FireUpGateMessage extends ApplicationEvent {
    private static final char div = '|';
    private static final String token = ":fire_gate";
    private final EventDtoRequest event;
    private final String type;

    public FireUpGateMessage(Object source, EventDtoRequest event, String type) {
        super(source);
        this.event = event;
        this.type = type;
    }

    @Override
    public String toString() {
        return token + div + this.event.gateId() + div + this.event.gate2Id() +
                div + this.event.id() + div + this.type;
    }
}
