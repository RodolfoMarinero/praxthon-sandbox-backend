package com.amisimecompila.speisandbox.operacion.application;

import com.amisimecompila.speisandbox.operacion.domain.Escenario;

public record OperacionRegistradaEvent(
        String operacionId,
        Escenario escenario
) {
}
