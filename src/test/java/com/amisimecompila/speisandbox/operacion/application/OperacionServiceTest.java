package com.amisimecompila.speisandbox.operacion.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amisimecompila.speisandbox.idempotencia.infrastructure.persistence.repository.IdempotenciaRepository;
import com.amisimecompila.speisandbox.operacion.api.dto.request.EmisorT2TRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.ImporteRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.ReceptorRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudT2TRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.response.ErrorResponse;
import com.amisimecompila.speisandbox.operacion.domain.CodigoErrorPrx;
import com.amisimecompila.speisandbox.operacion.domain.ResolutorEscenario;
import com.amisimecompila.speisandbox.operacion.domain.TipoOperacion;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.mapper.OperacionPersistenceMapper;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.repository.OperacionRepository;
import com.amisimecompila.speisandbox.shared.util.OperacionIdGenerator;
import com.amisimecompila.speisandbox.shared.util.PayloadHasher;
import com.amisimecompila.speisandbox.validacion.OperacionValidator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class OperacionServiceTest {

    @Test
    void referenciaDuplicadaProducePrx010() {
        OperacionRepository repository = mock(OperacionRepository.class);
        when(repository.existsByReferenciaSeguimiento(anyString()))
                .thenReturn(true);

        OperacionValidator validator = mock(OperacionValidator.class);
        when(validator.validar(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.amisimecompila.speisandbox.validacion.ResultadoValidacion(
                        java.util.List.of()
                ));

        PayloadHasher hasher = mock(PayloadHasher.class);
        when(hasher.calcular(org.mockito.ArgumentMatchers.any()))
                .thenReturn("a".repeat(64));

        OperacionService service = new OperacionService(
                repository,
                mock(IdempotenciaRepository.class),
                validator,
                mock(ResolutorEscenario.class),
                mock(OperacionPersistenceMapper.class),
                mock(OperacionResponseMapper.class),
                hasher,
                mock(OperacionIdGenerator.class),
                mock(ApplicationEventPublisher.class)
        );

        assertThatThrownBy(() -> service.registrar(
                solicitudValida(),
                null,
                null
        )).isInstanceOfSatisfying(
                ValidacionOperacionException.class,
                exception -> assertThat(exception.getErrores())
                        .extracting(ErrorResponse::codigo)
                        .containsExactly(CodigoErrorPrx.PRX_010)
        );
    }

    private SolicitudT2TRequest solicitudValida() {

        return new SolicitudT2TRequest(
                TipoOperacion.T2T,
                "PRX20260928000100",
                new ImporteRequest(new BigDecimal("100.00"), "MXN"),
                new EmisorT2TRequest(
                        "801",
                        "801180000118359717",
                        "Ordenante",
                        null
                ),
                new ReceptorRequest(
                        "802",
                        "802180000200030011",
                        "Beneficiario"
                ),
                "Prueba duplicada",
                100
        );

    }
}
