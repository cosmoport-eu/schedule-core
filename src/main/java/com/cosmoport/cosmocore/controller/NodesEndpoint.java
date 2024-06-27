package com.cosmoport.cosmocore.controller;

import com.cosmoport.cosmocore.service.NodesHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/nodes")
public class NodesEndpoint {

    private final NodesHolder nodesHolder;

    public NodesEndpoint(NodesHolder nodesHolder) {
        this.nodesHolder = nodesHolder;
    }

    @GetMapping
    public Map<String, Byte> get() {
        Map<String, Byte> nodes = new HashMap<>();
        nodes.put("timetables", (byte) nodesHolder.getTables());
        nodes.put("gates", (byte) nodesHolder.getGates());

        return nodes;
    }
}
