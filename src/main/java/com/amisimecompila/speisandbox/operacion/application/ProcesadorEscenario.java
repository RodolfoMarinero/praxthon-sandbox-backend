package com.amisimecompila.speisandbox.operacion.application;

import com.amisimecompila.speisandbox.operacion.domain.*;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ProcesadorEscenario {
    private final MaquinaEstados maquina;

    public ProcesadorEscenario(MaquinaEstados maquina) {
        this.maquina = maquina;
    }

    public void procesar(OperacionEntity operacion, Escenario escenario) {
        transicionar(operacion, EstadoOperacion.EN_PROCESO, null);
        switch (escenario) {
            case S01 -> transicionar(operacion, EstadoOperacion.LIQUIDADO, null);
            case S02 -> devolver(operacion, "PRX-020");
            case S03 -> devolver(operacion, "PRX-021");
            case S04 -> devolver(operacion, "PRX-022");
            case S05 -> { }
            case S06 -> transicionar(operacion, EstadoOperacion.EN_INVESTIGACION, null);
        }
    }

    private void devolver(OperacionEntity operacion, String motivo) {
        maquina.verificar(operacion.getEstado(), EstadoOperacion.DEVUELTO);
        TransicionOperacionEntity t = new TransicionOperacionEntity(EstadoOperacion.DEVUELTO, motivo);
        t.setTipoDevolucion("AUTOMATICA");
        t.setActorSolicitante("SISTEMA");
        operacion.agregarTransicion(t);
    }

    private void transicionar(OperacionEntity operacion, EstadoOperacion destino, String motivo) {
        maquina.verificar(operacion.getEstado(), destino);
        operacion.agregarTransicion(new TransicionOperacionEntity(destino, motivo));
    }
}
