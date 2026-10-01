package com.amisimecompila.speisandbox.validacion;

import com.amisimecompila.speisandbox.catalogo.domain.CatalogoInstituciones;
import com.amisimecompila.speisandbox.operacion.api.dto.request.EmisorT2TRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.EmisorVntRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.ImporteRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.ReceptorRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudOperacion;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudT2TRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudVntRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.response.ErrorResponse;
import com.amisimecompila.speisandbox.operacion.domain.CodigoErrorPrx;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OperacionValidator {

    private static final BigDecimal IMPORTE_MAXIMO =
            new BigDecimal("1000000.00");

    private final ClabeValidator clabeValidator;
    private final CatalogoInstituciones catalogo;

    public OperacionValidator(
            ClabeValidator clabeValidator,
            CatalogoInstituciones catalogo
    ) {
        this.clabeValidator = clabeValidator;
        this.catalogo = catalogo;
    }

    public ResultadoValidacion validar(SolicitudOperacion solicitud) {
        List<ErrorResponse> errores = new ArrayList<>();

        if (solicitud == null) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    null,
                    "La solicitud es obligatoria"
            ));
            return new ResultadoValidacion(errores);
        }

        validarReferencia(solicitud.referenciaSeguimiento(), errores);
        validarImporte(solicitud.importe(), errores);
        validarReceptor(solicitud.receptor(), errores);
        validarConcepto(solicitud.concepto(), errores);
        validarFolio(solicitud.folioNumerico(), errores);

        if (solicitud instanceof SolicitudT2TRequest t2t) {
            validarT2T(t2t, errores);
        } else if (solicitud instanceof SolicitudVntRequest vnt) {
            validarVnt(vnt, errores);
        } else {
            errores.add(error(
                    CodigoErrorPrx.PRX_031,
                    "tipoOperacion",
                    "El tipo de operación debe ser T2T o VNT"
            ));
        }

        return new ResultadoValidacion(errores);
    }

    private void validarReferencia(
            String referencia,
            List<ErrorResponse> errores
    ) {
        if (esVacio(referencia)
                || !referencia.matches("^[A-Za-z0-9]{1,30}$")) {
            errores.add(error(
                    CodigoErrorPrx.PRX_009,
                    "referenciaSeguimiento",
                    "La referencia debe contener de 1 a 30 caracteres alfanuméricos"
            ));
        }
    }

    private void validarImporte(
            ImporteRequest importe,
            List<ErrorResponse> errores
    ) {
        if (importe == null || importe.valor() == null) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    "importe.valor",
                    "El importe es obligatorio"
            ));
            return;
        }

        if (importe.valor().compareTo(BigDecimal.ZERO) <= 0) {
            errores.add(error(
                    CodigoErrorPrx.PRX_004,
                    "importe.valor",
                    "El importe debe ser mayor que cero"
            ));
        }

        if (importe.valor().compareTo(IMPORTE_MAXIMO) > 0
                || importe.valor().scale() > 2) {
            errores.add(error(
                    CodigoErrorPrx.PRX_005,
                    "importe.valor",
                    "El importe admite máximo 1000000.00 y dos decimales"
            ));
        }

        if (!"MXN".equals(importe.divisa())) {
            errores.add(error(
                    CodigoErrorPrx.PRX_006,
                    "importe.divisa",
                    "La divisa debe ser MXN"
            ));
        }
    }

    private void validarReceptor(
            ReceptorRequest receptor,
            List<ErrorResponse> errores
    ) {
        if (receptor == null) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    "receptor",
                    "El receptor es obligatorio"
            ));
            return;
        }

        validarInstitucion(
                receptor.institucion(),
                "receptor.institucion",
                false,
                errores
        );
        validarCuenta(
                receptor.cuenta(),
                receptor.institucion(),
                "receptor.cuenta",
                errores
        );
        validarNombre(receptor.nombre(), "receptor.nombre", errores);
    }

    private void validarT2T(
            SolicitudT2TRequest solicitud,
            List<ErrorResponse> errores
    ) {
        EmisorT2TRequest emisor = solicitud.emisor();
        if (emisor == null) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    "emisor",
                    "El emisor es obligatorio"
            ));
            return;
        }

        validarInstitucion(
                emisor.institucion(),
                "emisor.institucion",
                true,
                errores
        );

        if (esVacio(emisor.cuenta())) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    "emisor.cuenta",
                    "La cuenta emisora es obligatoria en T2T"
            ));
        } else {
            validarCuenta(
                    emisor.cuenta(),
                    emisor.institucion(),
                    "emisor.cuenta",
                    errores
            );
        }

        validarNombre(emisor.nombre(), "emisor.nombre", errores);

        if (solicitud.receptor() != null
                && emisor.cuenta() != null
                && emisor.cuenta().equals(solicitud.receptor().cuenta())) {
            errores.add(error(
                    CodigoErrorPrx.PRX_013,
                    "emisor.cuenta",
                    "La cuenta emisora debe ser distinta de la receptora"
            ));
        }
    }

    private void validarVnt(
            SolicitudVntRequest solicitud,
            List<ErrorResponse> errores
    ) {
        EmisorVntRequest emisor = solicitud.emisor();
        if (emisor == null) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    "emisor",
                    "El emisor es obligatorio"
            ));
            return;
        }

        validarInstitucion(
                emisor.institucion(),
                "emisor.institucion",
                true,
                errores
        );

        if (esVacio(emisor.sucursal())) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    "emisor.sucursal",
                    "La sucursal es obligatoria en VNT"
            ));
        }

        if (emisor.documentoIdentidad() == null
                || esVacio(emisor.documentoIdentidad().tipo())
                || esVacio(emisor.documentoIdentidad().numero())) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    "emisor.documentoIdentidad",
                    "El documento de identidad es obligatorio en VNT"
            ));
        }

        validarNombre(emisor.nombre(), "emisor.nombre", errores);
    }

    private void validarInstitucion(
            String codigo,
            String campo,
            boolean emisor,
            List<ErrorResponse> errores
    ) {
        if (!catalogo.existe(codigo)
                || (emisor && !catalogo.permiteEmision(codigo))) {
            errores.add(error(
                    CodigoErrorPrx.PRX_003,
                    campo,
                    "La institución no existe o no está permitida para ese rol"
            ));
        }
    }

    private void validarCuenta(
            String cuenta,
            String institucion,
            String campo,
            List<ErrorResponse> errores
    ) {
        if (!clabeValidator.tieneFormatoValido(cuenta)) {
            errores.add(error(
                    CodigoErrorPrx.PRX_001,
                    campo,
                    "La cuenta debe contener exactamente 18 dígitos"
            ));
            return;
        }

        if (!clabeValidator.tieneDigitoVerificadorValido(cuenta)) {
            errores.add(error(
                    CodigoErrorPrx.PRX_002,
                    campo,
                    "El dígito verificador de la CLABE es incorrecto"
            ));
        }

        if (catalogo.existe(institucion)
                && !clabeValidator.perteneceAInstitucion(
                        cuenta,
                        institucion
                )) {
            errores.add(error(
                    CodigoErrorPrx.PRX_030,
                    campo,
                    "El prefijo de la CLABE no coincide con la institución"
            ));
        }
    }

    private void validarNombre(
            String nombre,
            String campo,
            List<ErrorResponse> errores
    ) {
        if (esVacio(nombre) || nombre.length() > 40) {
            errores.add(error(
                    CodigoErrorPrx.PRX_011,
                    campo,
                    "El nombre debe contener de 1 a 40 caracteres"
            ));
        }
    }

    private void validarConcepto(
            String concepto,
            List<ErrorResponse> errores
    ) {
        if (esVacio(concepto) || concepto.length() > 40) {
            errores.add(error(
                    CodigoErrorPrx.PRX_007,
                    "concepto",
                    "El concepto debe contener de 1 a 40 caracteres"
            ));
        }
    }

    private void validarFolio(
            Integer folio,
            List<ErrorResponse> errores
    ) {
        if (folio == null || folio < 1 || folio > 9_999_999) {
            errores.add(error(
                    CodigoErrorPrx.PRX_008,
                    "folioNumerico",
                    "El folio debe estar entre 1 y 9999999"
            ));
        }
    }

    private boolean esVacio(String value) {

        return value == null || value.isBlank();

    }
    private ErrorResponse error(
            CodigoErrorPrx codigo,
            String campo,
            String mensaje
    ) {
        return new ErrorResponse(codigo, campo, mensaje);
    }
}
