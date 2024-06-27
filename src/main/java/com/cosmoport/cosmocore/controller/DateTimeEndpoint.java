package com.cosmoport.cosmocore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/time")
public class DateTimeEndpoint {
    @GetMapping
    public DateTimeDto getDateTime() {
        return new DateTimeDto(Instant.now().getEpochSecond());
    }

    public record DateTimeDto(long timestamp) {
    }
}
