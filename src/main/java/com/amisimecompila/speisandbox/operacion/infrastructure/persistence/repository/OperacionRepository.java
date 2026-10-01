package com.amisimecompila.speisandbox.operacion.infrastructure.persistence.repository;

import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.OperacionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperacionRepository
        extends JpaRepository<OperacionEntity, String> {

    boolean existsByReferenciaSeguimiento(String referenciaSeguimiento);

    @EntityGraph(attributePaths = "transiciones")
    Optional<OperacionEntity> findOneWithTransicionesById(String id);
}
