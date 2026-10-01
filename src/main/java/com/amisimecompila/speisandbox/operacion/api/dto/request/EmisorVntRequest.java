package com.amisimecompila.speisandbox.operacion.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public record EmisorVntRequest(
        String institucion,
        String sucursal,
        String nombre,
        DocumentoIdentidadRequest documentoIdentidad
) {
}
