package com.amisimecompila.speisandbox.operacion.api.dto.request;

import com.amisimecompila.speisandbox.operacion.domain.TipoOperacion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = false)
public record SolicitudVntRequest(
        TipoOperacion tipoOperacion,
        String referenciaSeguimiento,
        ImporteRequest importe,
        EmisorVntRequest emisor,
        ReceptorRequest receptor,
        String concepto,
        Integer folioNumerico
) implements SolicitudOperacion {
}
