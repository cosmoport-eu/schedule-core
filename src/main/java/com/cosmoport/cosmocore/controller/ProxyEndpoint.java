package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.controller.dto.EventDtoRequest;
import com.cosmoport.cosmocore.events.FireUpGateMessage;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/proxy")
public class ProxyEndpoint {

    private final ApplicationEventPublisher eventBus;

    public ProxyEndpoint(ApplicationEventPublisher eventBus) {
        this.eventBus = eventBus;
    }

    @PostMapping
    public String reactOn(@RequestBody ProxyRequestDto request) {
        if (request.name().equals("fire_gate")) {
            eventBus.publishEvent(new FireUpGateMessage(this, request.event(), request.type()));
        }

        return "{\"result\": \"success\"}";
    }

    public record ProxyRequestDto(String name, EventDtoRequest event, String type) {
    }

}


