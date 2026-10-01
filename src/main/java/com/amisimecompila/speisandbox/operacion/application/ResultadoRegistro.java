package com.amisimecompila.speisandbox.operacion.application;

import com.amisimecompila.speisandbox.operacion.api.dto.response.OperacionResponse;

public record ResultadoRegistro(
        OperacionResponse operacion,
        boolean creada
) {
}
