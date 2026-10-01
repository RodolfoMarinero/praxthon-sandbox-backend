package com.amisimecompila.speisandbox.operacion.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CodigoErrorPrx {
    PRX_001("PRX-001"),
    PRX_002("PRX-002"),
    PRX_003("PRX-003"),
    PRX_004("PRX-004"),
    PRX_005("PRX-005"),
    PRX_006("PRX-006"),
    PRX_007("PRX-007"),
    PRX_008("PRX-008"),
    PRX_009("PRX-009"),
    PRX_010("PRX-010"),
    PRX_011("PRX-011"),
    PRX_012("PRX-012"),
    PRX_013("PRX-013"),
    PRX_014("PRX-014"),
    PRX_015("PRX-015"),
    PRX_020("PRX-020"),
    PRX_021("PRX-021"),
    PRX_022("PRX-022"),
    PRX_023("PRX-023"),
    PRX_024("PRX-024"),
    PRX_030("PRX-030"),
    PRX_031("PRX-031");

    private final String value;

    CodigoErrorPrx(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
