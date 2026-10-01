package com.amisimecompila.speisandbox.validacion;

import com.amisimecompila.speisandbox.operacion.api.dto.response.ErrorResponse;
import java.util.List;

public record ResultadoValidacion(
        List<ErrorResponse> errores
) {
    public ResultadoValidacion {
        errores = List.copyOf(errores);
    }

    public boolean esValido() {

        return errores.isEmpty();

    }
}
