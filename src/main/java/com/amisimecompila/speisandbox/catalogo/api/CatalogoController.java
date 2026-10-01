package com.amisimecompila.speisandbox.catalogo.api;

import com.amisimecompila.speisandbox.catalogo.infrastructure.persistence.repository.InstitucionRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoController {
    private final InstitucionRepository repository;

    public CatalogoController(InstitucionRepository repository) {

        this.repository = repository;

    }
    @GetMapping("/instituciones")
    public List<InstitucionResponse> listar() {
        return repository.findAllByOrderByCodigoAsc()
                .stream()
                .map(entity -> new InstitucionResponse(
                        entity.getCodigo(),
                        entity.getNombre()
                ))
                .toList();
    }
}
