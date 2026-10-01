package com.amisimecompila.speisandbox.catalogo.infrastructure.persistence.repository;

import com.amisimecompila.speisandbox.catalogo.infrastructure.persistence.entity.InstitucionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitucionRepository
        extends JpaRepository<InstitucionEntity, String> {

    List<InstitucionEntity> findAllByOrderByCodigoAsc();
}
