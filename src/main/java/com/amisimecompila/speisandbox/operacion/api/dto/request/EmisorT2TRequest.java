package com.amisimecompila.speisandbox.operacion.api.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public record EmisorT2TRequest(
        String institucion,
        String cuenta,
        String nombre,
        String identificacionFiscal
) {
}
