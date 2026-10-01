package com.amisimecompila.speisandbox.operacion.api.dto.response;

import com.amisimecompila.speisandbox.operacion.domain.CodigoErrorPrx;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        CodigoErrorPrx codigo,
        String campo,
        String mensaje
) {
}
