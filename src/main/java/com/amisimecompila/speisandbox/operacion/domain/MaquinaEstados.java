package com.amisimecompila.speisandbox.operacion.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class MaquinaEstados {

    private final Map<EstadoOperacion, Set<EstadoOperacion>> transiciones =
            new EnumMap<>(EstadoOperacion.class);

    public MaquinaEstados() {

        transiciones.put(
                EstadoOperacion.RECIBIDO,
                EnumSet.of(
                        EstadoOperacion.EN_PROCESO,
                        EstadoOperacion.RECHAZADO
                )
        );

        transiciones.put(
                EstadoOperacion.EN_PROCESO,
                EnumSet.of(
                        EstadoOperacion.LIQUIDADO,
                        EstadoOperacion.DEVUELTO,
                        EstadoOperacion.EN_INVESTIGACION
                )
        );

        transiciones.put(
                EstadoOperacion.EN_INVESTIGACION,
                EnumSet.of(
                        EstadoOperacion.LIQUIDADO,
                        EstadoOperacion.DEVUELTO
                )
        );

        transiciones.put(
                EstadoOperacion.LIQUIDADO,
                EnumSet.noneOf(EstadoOperacion.class)
        );

        transiciones.put(
                EstadoOperacion.DEVUELTO,
                EnumSet.noneOf(EstadoOperacion.class)
        );

        transiciones.put(
                EstadoOperacion.RECHAZADO,
                EnumSet.noneOf(EstadoOperacion.class)
        );

    }
    public boolean puedeTransicionar(
            EstadoOperacion origen,
            EstadoOperacion destino
    ) {
        return origen != null
                && destino != null
                && transiciones
                        .getOrDefault(origen, Set.of())
                        .contains(destino);
    }

    public void verificar(
            EstadoOperacion origen,
            EstadoOperacion destino
    ) {
        if (!puedeTransicionar(origen, destino)) {
            throw new TransicionNoPermitidaException(origen, destino);
        }
    }
}
