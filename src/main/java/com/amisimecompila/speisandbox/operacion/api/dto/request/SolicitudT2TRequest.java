package com.amisimecompila.speisandbox.operacion.api.dto.request;

import com.amisimecompila.speisandbox.operacion.domain.TipoOperacion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public record SolicitudT2TRequest(
        TipoOperacion tipoOperacion,
        String referenciaSeguimiento,
        ImporteRequest importe,
        EmisorT2TRequest emisor,
        ReceptorRequest receptor,
        String concepto,
        Integer folioNumerico
) implements SolicitudOperacion {
}
