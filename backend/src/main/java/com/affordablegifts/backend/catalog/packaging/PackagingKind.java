package com.affordablegifts.backend.catalog.packaging;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PackagingKind {
    BOX("box"),
    JAR("jar");

    private final String wire;

    PackagingKind(String wire) {
        this.wire = wire;
    }

    @JsonValue
    public String wire() {
        return wire;
    }

    public static PackagingKind fromWire(String wire) {
        for (PackagingKind kind : values()) {
            if (kind.wire.equals(wire)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown packaging kind: " + wire);
    }
}
