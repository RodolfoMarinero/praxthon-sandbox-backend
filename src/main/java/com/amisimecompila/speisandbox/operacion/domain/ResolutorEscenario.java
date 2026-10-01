package com.amisimecompila.speisandbox.operacion.domain;

import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudOperacion;
import org.springframework.stereotype.Component;

@Component
public class ResolutorEscenario {

    public Escenario resolver(
            SolicitudOperacion solicitud,
            Escenario forzado
    ) {
        if (forzado != null) {
            return forzado;
        }
        if ("805".equals(solicitud.receptor().institucion())) {
            return Escenario.S04;
        }

        String cuenta = solicitud.receptor().cuenta();
        if (cuenta == null || cuenta.length() != 18) {
            return Escenario.S01;
        }

        return switch (cuenta.substring(13, 17)) {
            case "9002" -> Escenario.S02;
            case "9003" -> Escenario.S03;
            case "9004" -> Escenario.S04;
            case "9005" -> Escenario.S05;
            case "9006" -> Escenario.S06;
            default -> Escenario.S01;
        };
    }
}
