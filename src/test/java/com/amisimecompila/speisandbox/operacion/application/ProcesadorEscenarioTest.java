package com.amisimecompila.speisandbox.operacion.application;

import static org.assertj.core.api.Assertions.assertThat;
import com.amisimecompila.speisandbox.operacion.domain.*;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

class ProcesadorEscenarioTest {
    @Test
    void escenarioS02TerminaDevuelto() {
        OperacionEntity entity = OperacionEntity.nueva();
        entity.agregarTransicion(new TransicionOperacionEntity(EstadoOperacion.RECIBIDO, null));
        new ProcesadorEscenario(new MaquinaEstados()).procesar(entity, Escenario.S02);
        assertThat(entity.getEstado()).isEqualTo(EstadoOperacion.DEVUELTO);
        assertThat(entity.getTransiciones()).hasSize(3);
    }
}
