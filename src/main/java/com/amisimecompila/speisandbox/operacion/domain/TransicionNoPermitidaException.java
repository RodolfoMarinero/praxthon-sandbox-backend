package com.amisimecompila.speisandbox.operacion.domain;

public class TransicionNoPermitidaException extends RuntimeException {

    public TransicionNoPermitidaException(
            EstadoOperacion origen,
            EstadoOperacion destino
    ) {
        super("Transición no permitida de " + origen + " a " + destino);
    }

    public CodigoErrorPrx getCodigo() {

        return CodigoErrorPrx.PRX_014;

    }
}
