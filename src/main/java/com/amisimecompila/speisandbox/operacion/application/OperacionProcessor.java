package com.amisimecompila.speisandbox.operacion.application;

import com.amisimecompila.speisandbox.operacion.domain.Escenario;
import com.amisimecompila.speisandbox.operacion.domain.EstadoOperacion;
import com.amisimecompila.speisandbox.operacion.domain.MaquinaEstados;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.OperacionEntity;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.TransicionOperacionEntity;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.repository.OperacionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class OperacionProcessor {

    private final OperacionRepository repository;
    private final MaquinaEstados maquinaEstados;

    public OperacionProcessor(
            OperacionRepository repository,
            MaquinaEstados maquinaEstados
    ) {
        this.repository = repository;
        this.maquinaEstados = maquinaEstados;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void procesar(OperacionRegistradaEvent event) {
        OperacionEntity operacion = repository
                .findOneWithTransicionesById(
                        event.operacionId()
                )
                .orElseThrow();

        String motivoEnProceso =
                event.escenario() == Escenario.S05
                        ? "PRX-023"
                        : null;

        transicionar(
                operacion,
                EstadoOperacion.EN_PROCESO,
                motivoEnProceso
        );

        switch (event.escenario()) {
            case S01 -> transicionar(
                    operacion,
                    EstadoOperacion.LIQUIDADO,
                    null
            );

            case S02 -> devolver(
                    operacion,
                    "PRX-020"
            );

            case S03 -> devolver(
                    operacion,
                    "PRX-021"
            );

            case S04 -> devolver(
                    operacion,
                    "PRX-022"
            );

            case S05 -> {
                // La operación permanece en EN_PROCESO.
            }

            case S06 -> transicionar(
                    operacion,
                    EstadoOperacion.EN_INVESTIGACION,
                    "PRX-024"
            );
        }

        repository.save(operacion);
    }

    private void transicionar(
            OperacionEntity operacion,
            EstadoOperacion destino,
            String motivo
    ) {
        maquinaEstados.verificar(
                operacion.getEstado(),
                destino
        );

        TransicionOperacionEntity transicion =
                new TransicionOperacionEntity(
                        destino,
                        motivo
                );

        operacion.agregarTransicion(transicion);
    }

    private void devolver(
            OperacionEntity operacion,
            String motivo
    ) {
        maquinaEstados.verificar(
                operacion.getEstado(),
                EstadoOperacion.DEVUELTO
        );

        TransicionOperacionEntity transicion =
                new TransicionOperacionEntity(
                        EstadoOperacion.DEVUELTO,
                        motivo
                );

        transicion.setTipoDevolucion(
                "AUTOMATICA"
        );

        transicion.setActorSolicitante(
                "SISTEMA"
        );

        operacion.agregarTransicion(
                transicion
        );
    }
}