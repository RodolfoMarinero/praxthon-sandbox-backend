package com.amisimecompila.speisandbox.operacion.api;

import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudOperacion;
import com.amisimecompila.speisandbox.operacion.api.dto.response.OperacionResponse;
import com.amisimecompila.speisandbox.operacion.api.dto.response.PaginaOperacionesResponse;
import com.amisimecompila.speisandbox.operacion.application.OperacionService;
import com.amisimecompila.speisandbox.operacion.application.ResultadoRegistro;
import com.amisimecompila.speisandbox.operacion.domain.Escenario;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/operaciones")
public class OperacionController {
    private final OperacionService service;

    public OperacionController(OperacionService service) {

        this.service = service;

    }
    @PostMapping
    public ResponseEntity<OperacionResponse> registrar(
            @RequestHeader(name = "Clave-Idempotencia", required = false)
            UUID clave,
            @RequestHeader(name = "X-Escenario-Forzado", required = false)
            Escenario escenario,
            @RequestBody SolicitudOperacion solicitud
    ) {
        ResultadoRegistro resultado = service.registrar(
                solicitud,
                clave,
                escenario
        );
        if (!resultado.creada()) {
            return ResponseEntity.ok(resultado.operacion());
        }
        return ResponseEntity.created(URI.create(
                "/api/v1/operaciones/" + resultado.operacion().id()
        )).body(resultado.operacion());
    }

    @GetMapping
    public PaginaOperacionesResponse listar(
            @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int tamano
    ) {
        return service.listar(pagina, tamano);
    }

    @GetMapping("/{id}")
    public OperacionResponse consultar(@PathVariable String id) {
        return service.consultar(id);
    }
}
