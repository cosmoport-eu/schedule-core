package com.cosmoport.cosmocore.service;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Component
public final class NodesHolder {
    private final AtomicInteger tables = new AtomicInteger(0);
    private final AtomicInteger gates = new AtomicInteger(0);

    public int getTables() {
        return tables.get();
    }

    public int getGates() {
        return gates.get();
    }

    public void incTables() {
        this.tables.incrementAndGet();
    }

    public void incGates() {
        this.gates.incrementAndGet();
    }

    public void decTables() {
        this.tables.decrementAndGet();
    }

    public void decGates() {
        this.gates.decrementAndGet();
    }
}
