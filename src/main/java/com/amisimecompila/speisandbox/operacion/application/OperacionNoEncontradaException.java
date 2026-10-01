package com.amisimecompila.speisandbox.operacion.application;

public class OperacionNoEncontradaException extends RuntimeException {

    public OperacionNoEncontradaException(String id) {

        super("No existe la operación " + id);

    }
}
