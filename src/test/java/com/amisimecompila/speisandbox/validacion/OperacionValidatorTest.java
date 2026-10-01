package com.amisimecompila.speisandbox.validacion;

import static org.assertj.core.api.Assertions.assertThat;

import com.amisimecompila.speisandbox.catalogo.domain.CatalogoInstituciones;
import com.amisimecompila.speisandbox.operacion.api.dto.request.EmisorT2TRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.ImporteRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.ReceptorRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudT2TRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.response.ErrorResponse;
import com.amisimecompila.speisandbox.operacion.domain.CodigoErrorPrx;
import com.amisimecompila.speisandbox.operacion.domain.TipoOperacion;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OperacionValidatorTest {

    private final OperacionValidator validator = new OperacionValidator(
            new ClabeValidator(),
            new CatalogoInstituciones()
    );

    @Test
    void acumulaErroresConElCatalogoPrxVigente() {
        SolicitudT2TRequest solicitud = new SolicitudT2TRequest(
                TipoOperacion.T2T,
                "referencia-invalida",
                new ImporteRequest(new BigDecimal("-0.001"), "USD"),
                new EmisorT2TRequest("804", "123", "", null),
                new ReceptorRequest("802", "456", ""),
                "",
                0
        );

        ResultadoValidacion resultado = validator.validar(solicitud);

        assertThat(resultado.esValido()).isFalse();
        assertThat(resultado.errores())
                .extracting(ErrorResponse::codigo)
                .contains(
                        CodigoErrorPrx.PRX_001,
                        CodigoErrorPrx.PRX_003,
                        CodigoErrorPrx.PRX_004,
                        CodigoErrorPrx.PRX_005,
                        CodigoErrorPrx.PRX_006,
                        CodigoErrorPrx.PRX_007,
                        CodigoErrorPrx.PRX_008,
                        CodigoErrorPrx.PRX_009,
                        CodigoErrorPrx.PRX_011
                );
    }

    @Test
    void distingueDigitoVerificadorIncorrectoDelFormato() {
        SolicitudT2TRequest solicitud = solicitudValida(
                "801180000118359718",
                "802180000200030011"
        );

        ResultadoValidacion resultado = validator.validar(solicitud);

        assertThat(resultado.errores())
                .extracting(ErrorResponse::codigo)
                .contains(CodigoErrorPrx.PRX_002)
                .doesNotContain(CodigoErrorPrx.PRX_001);
    }

    @Test
    void detectaPrefijoDistintoDeLaInstitucion() {
        SolicitudT2TRequest solicitud = new SolicitudT2TRequest(
                TipoOperacion.T2T,
                "PRX20260928000002",
                new ImporteRequest(new BigDecimal("100.00"), "MXN"),
                new EmisorT2TRequest(
                        "802",
                        "801180000118359717",
                        "Ordenante",
                        null
                ),
                new ReceptorRequest(
                        "803",
                        "802180000200030011",
                        "Beneficiario"
                ),
                "Prueba de prefijos",
                2
        );

        ResultadoValidacion resultado = validator.validar(solicitud);

        assertThat(resultado.errores())
                .extracting(ErrorResponse::codigo)
                .contains(CodigoErrorPrx.PRX_030);
    }

    @Test
    void detectaCuentaEmisoraIgualAReceptora() {
        SolicitudT2TRequest solicitud = solicitudValida(
                "801180000118359717",
                "801180000118359717"
        );

        ResultadoValidacion resultado = validator.validar(solicitud);

        assertThat(resultado.errores())
                .extracting(ErrorResponse::codigo)
                .contains(CodigoErrorPrx.PRX_013);
    }

    private SolicitudT2TRequest solicitudValida(
            String cuentaEmisora,
            String cuentaReceptora
    ) {
        return new SolicitudT2TRequest(
                TipoOperacion.T2T,
                "PRX20260928000001",
                new ImporteRequest(new BigDecimal("1500.50"), "MXN"),
                new EmisorT2TRequest(
                        "801",
                        cuentaEmisora,
                        "Ana Ruiz Delgado",
                        null
                ),
                new ReceptorRequest(
                        cuentaReceptora.startsWith("801") ? "801" : "802",
                        cuentaReceptora,
                        "Luis Cano Mora"
                ),
                "Pago de servicios",
                4821
        );
    }
}
