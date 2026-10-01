package com.amisimecompila.speisandbox.operacion.api.dto.response;

import com.amisimecompila.speisandbox.operacion.domain.EstadoOperacion;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransicionResponse(
        EstadoOperacion estado,
        Instant momento,
        String motivo
) {
}
