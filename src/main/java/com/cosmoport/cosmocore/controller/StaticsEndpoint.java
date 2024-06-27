package com.cosmoport.cosmocore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class StaticsEndpoint {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> getFavicon() {
        return ResponseEntity.noContent().build();
    }
}
