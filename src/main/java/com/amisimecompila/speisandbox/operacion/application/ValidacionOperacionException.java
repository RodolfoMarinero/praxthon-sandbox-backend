package com.amisimecompila.speisandbox.operacion.application;

import com.amisimecompila.speisandbox.operacion.api.dto.response.ErrorResponse;
import java.util.List;

public class ValidacionOperacionException extends RuntimeException {

    private final String referencia;
    private final List<ErrorResponse> errores;

    public ValidacionOperacionException(
            String referencia,
            List<ErrorResponse> errores
    ) {
        this.referencia = referencia;
        this.errores = List.copyOf(errores);
    }

    public String getReferencia() {

        return referencia;

    }
    public List<ErrorResponse> getErrores() {
        return errores;
    }
}
