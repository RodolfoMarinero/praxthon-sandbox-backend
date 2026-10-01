package com.amisimecompila.speisandbox.catalogo.domain;

public record Institucion(
        String codigo,
        String nombre,
        boolean permiteEmision,
        boolean disponible
) {}
