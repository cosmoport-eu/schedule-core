package com.cosmoport.cosmocore.service;

public enum Types {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete");

    private final String value;

    Types(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
