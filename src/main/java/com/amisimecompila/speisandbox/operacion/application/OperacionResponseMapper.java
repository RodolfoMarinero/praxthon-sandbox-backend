package com.amisimecompila.speisandbox.operacion.application;

import com.amisimecompila.speisandbox.operacion.api.dto.response.ImporteResponse;
import com.amisimecompila.speisandbox.operacion.api.dto.response.OperacionResponse;
import com.amisimecompila.speisandbox.operacion.api.dto.response.TransicionResponse;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.OperacionEntity;
import org.springframework.stereotype.Component;

@Component
public class OperacionResponseMapper {

    public OperacionResponse toResponse(OperacionEntity entity) {

        return new OperacionResponse(
                entity.getId(),
                entity.getReferenciaSeguimiento(),
                entity.getEstado(),
                entity.getTipoOperacion(),
                new ImporteResponse(
                        entity.getImporteValor(),
                        entity.getImporteDivisa()
                ),
                entity.getFechaRegistro(),
                entity.getTransiciones().stream()
                        .map(item -> new TransicionResponse(
                                item.getEstado(),
                                item.getMomento(),
                                item.getMotivo()
                        ))
                        .toList()
        );

    }
}
