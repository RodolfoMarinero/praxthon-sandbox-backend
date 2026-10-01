package com.amisimecompila.speisandbox.idempotencia.infrastructure.persistence.repository;

import com.amisimecompila.speisandbox.idempotencia.infrastructure.persistence.entity.IdempotenciaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotenciaRepository
        extends JpaRepository<IdempotenciaEntity, Long> {

    @EntityGraph(attributePaths = {
            "operacion",
            "operacion.transiciones"
    })
    Optional<IdempotenciaEntity> findByClave(String clave);
}
