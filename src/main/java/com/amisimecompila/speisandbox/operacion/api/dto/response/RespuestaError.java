package com.amisimecompila.speisandbox.operacion.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaError(
        String referenciaSeguimiento,
        List<ErrorResponse> errores
) {
    public RespuestaError {
        errores = List.copyOf(errores);
    }
}
