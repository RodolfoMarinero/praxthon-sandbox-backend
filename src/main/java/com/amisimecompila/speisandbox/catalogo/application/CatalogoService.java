package com.amisimecompila.speisandbox.catalogo.application;

import com.amisimecompila.speisandbox.catalogo.api.InstitucionResponse;
import com.amisimecompila.speisandbox.catalogo.infrastructure.persistence.repository.InstitucionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoService {

    private final InstitucionRepository repository;

    public CatalogoService(InstitucionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<InstitucionResponse> listarInstituciones() {
        return repository.findAllByOrderByCodigoAsc()
                .stream()
                .map(entity -> new InstitucionResponse(
                        entity.getCodigo(),
                        entity.getNombre()
                ))
                .toList();
    }
}
