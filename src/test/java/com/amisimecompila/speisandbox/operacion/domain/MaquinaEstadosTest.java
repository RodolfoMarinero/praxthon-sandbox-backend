package com.amisimecompila.speisandbox.operacion.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class MaquinaEstadosTest {
    private final MaquinaEstados maquina = new MaquinaEstados();

    @Test
    void permiteTransicionesDesdeRecibido() {
        assertThat(maquina.puedeTransicionar(
                EstadoOperacion.RECIBIDO,
                EstadoOperacion.EN_PROCESO
        )).isTrue();
        assertThat(maquina.puedeTransicionar(
                EstadoOperacion.RECIBIDO,
                EstadoOperacion.RECHAZADO
        )).isTrue();
    }

    @Test
    void liquidadoEsTerminal() {
        assertThatThrownBy(() -> maquina.verificar(
                EstadoOperacion.LIQUIDADO,
                EstadoOperacion.DEVUELTO
        )).isInstanceOf(TransicionNoPermitidaException.class);
    }
}
