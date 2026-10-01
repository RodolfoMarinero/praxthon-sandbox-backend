package com.amisimecompila.speisandbox.shared.api;

import com.amisimecompila.speisandbox.operacion.api.dto.response.ErrorResponse;
import com.amisimecompila.speisandbox.operacion.api.dto.response.RespuestaError;
import com.amisimecompila.speisandbox.operacion.application.ConflictoIdempotenciaException;
import com.amisimecompila.speisandbox.operacion.application.OperacionNoEncontradaException;
import com.amisimecompila.speisandbox.operacion.application.ValidacionOperacionException;
import com.amisimecompila.speisandbox.operacion.domain.CodigoErrorPrx;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidacionOperacionException.class)
    ResponseEntity<RespuestaError> handleValidation(
            ValidacionOperacionException exception
    ) {
        return ResponseEntity.unprocessableEntity().body(
                new RespuestaError(
                        exception.getReferencia(),
                        exception.getErrores()
                )
        );
    }

    @ExceptionHandler(ConflictoIdempotenciaException.class)
    ResponseEntity<RespuestaError> handleIdempotency() {
        return ResponseEntity.status(409).body(error(
                CodigoErrorPrx.PRX_015,
                "Clave-Idempotencia",
                "La clave fue utilizada con un cuerpo diferente"
        ));
    }

    @ExceptionHandler(OperacionNoEncontradaException.class)
    ResponseEntity<RespuestaError> handleNotFound(
            OperacionNoEncontradaException exception
    ) {
        return ResponseEntity.status(404).body(error(
                null,
                null,
                exception.getMessage()
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<RespuestaError> handleMalformedJson(
            HttpMessageNotReadableException exception
    ) {
        Throwable cause = exception.getMostSpecificCause();

        if (cause instanceof UnrecognizedPropertyException property) {
            return ResponseEntity.unprocessableEntity().body(error(
                    CodigoErrorPrx.PRX_012,
                    buildPath(property),
                    "Campo prohibido para el tipo de operación"
            ));
        }

        if (cause instanceof InvalidTypeIdException) {
            return ResponseEntity.unprocessableEntity().body(error(
                    CodigoErrorPrx.PRX_031,
                    "tipoOperacion",
                    "El tipo de operación debe ser T2T o VNT"
            ));
        }

        return ResponseEntity.unprocessableEntity().body(error(
                CodigoErrorPrx.PRX_011,
                null,
                "El cuerpo JSON es inválido"
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<RespuestaError> handleParameterType(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity.unprocessableEntity().body(error(
                CodigoErrorPrx.PRX_011,
                exception.getName(),
                "El valor tiene un formato inválido"
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<RespuestaError> handleConstraintViolation(
            ConstraintViolationException exception
    ) {
        List<ErrorResponse> errores = exception.getConstraintViolations()
                .stream()
                .map(violation -> new ErrorResponse(
                        CodigoErrorPrx.PRX_011,
                        lastSegment(violation.getPropertyPath().toString()),
                        violation.getMessage()
                ))
                .toList();

        return ResponseEntity.unprocessableEntity().body(
                new RespuestaError(null, errores)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<RespuestaError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception
    ) {
        List<ErrorResponse> errores = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(field -> new ErrorResponse(
                        CodigoErrorPrx.PRX_011,
                        field.getField(),
                        field.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity.unprocessableEntity().body(
                new RespuestaError(null, errores)
        );
    }

    private String buildPath(UnrecognizedPropertyException exception) {
        String parent = exception.getPath()
                .stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining("."));

        if (parent.isBlank()) {
            return exception.getPropertyName();
        }
        if (parent.endsWith(exception.getPropertyName())) {
            return parent;
        }
        return parent + "." + exception.getPropertyName();
    }

    private String lastSegment(String path) {

        int position = path.lastIndexOf('.');

        return position < 0 ? path : path.substring(position + 1);

    }
    private RespuestaError error(
            CodigoErrorPrx codigo,
            String campo,
            String mensaje
    ) {
        return new RespuestaError(
                null,
                List.of(new ErrorResponse(codigo, campo, mensaje))
        );
    }
}
