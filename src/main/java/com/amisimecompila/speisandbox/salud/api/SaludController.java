package com.amisimecompila.speisandbox.salud.api;

import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.repository.OperacionRepository;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/salud")
public class SaludController {
    private final OperacionRepository repository;

    public SaludController(OperacionRepository repository) {

        this.repository = repository;

    }
    @GetMapping
    public SaludResponse consultar() {
        return new SaludResponse(
                "UP",
                Instant.now(),
                repository.count()
        );
    }
    public record SaludResponse(
            String estado,
            Instant momento,
            long operacionesRegistradas
    ) {
    }
}
