package com.amisimecompila.speisandbox.operacion.infrastructure.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.amisimecompila.speisandbox.operacion.api.dto.request.EmisorT2TRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.ImporteRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.ReceptorRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudT2TRequest;
import com.amisimecompila.speisandbox.operacion.domain.Escenario;
import com.amisimecompila.speisandbox.operacion.domain.TipoOperacion;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OperacionPersistenceMapperTest {

    private final OperacionPersistenceMapper mapper =
            new OperacionPersistenceMapper();

    @Test
    void mapeaSolicitudT2T() {
        SolicitudT2TRequest request = new SolicitudT2TRequest(
                TipoOperacion.T2T,
                "PRX20260921000001",
                new ImporteRequest(new BigDecimal("1500.50"), "MXN"),
                new EmisorT2TRequest(
                        "801",
                        "801180000118359717",
                        "Ana Ruiz Delgado",
                        "RUDA900112HN4"
                ),
                new ReceptorRequest(
                        "802",
                        "802180000200030011",
                        "Luis Cano Mora"
                ),
                "Pago de servicios",
                4821
        );

        var entity = mapper.toEntity(
                request,
                "op_01J8ZQ4M7X2NRVA6K3TDY9C5EB",
                "a".repeat(64),
                Escenario.S01
        );

        assertThat(entity.getTipoOperacion())
                .isEqualTo(TipoOperacion.T2T);
        assertThat(entity.getEmisorCuenta())
                .isEqualTo("801180000118359717");
        assertThat(entity.getEmisorSucursal())
                .isNull();
    }
}
