package com.amisimecompila.speisandbox.operacion.api.dto.request;

import com.amisimecompila.speisandbox.operacion.domain.TipoOperacion;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "tipoOperacion",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SolicitudT2TRequest.class, name = "T2T"),
        @JsonSubTypes.Type(value = SolicitudVntRequest.class, name = "VNT")
})
public sealed interface SolicitudOperacion
        permits SolicitudT2TRequest, SolicitudVntRequest {

    TipoOperacion tipoOperacion();

    String referenciaSeguimiento();

    ImporteRequest importe();

    ReceptorRequest receptor();

    String concepto();

    Integer folioNumerico();
}
