package com.amisimecompila.speisandbox.operacion.api.dto.response;

import com.amisimecompila.speisandbox.operacion.domain.EstadoOperacion;
import com.amisimecompila.speisandbox.operacion.domain.TipoOperacion;
import java.time.Instant;
import java.util.List;

public record OperacionResponse(
        String id,
        String referenciaSeguimiento,
        EstadoOperacion estado,
        TipoOperacion tipoOperacion,
        ImporteResponse importe,
        Instant fechaRegistro,
        List<TransicionResponse> transiciones
) {
}
