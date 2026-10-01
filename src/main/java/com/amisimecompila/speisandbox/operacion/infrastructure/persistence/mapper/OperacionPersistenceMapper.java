package com.amisimecompila.speisandbox.operacion.infrastructure.persistence.mapper;

import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudOperacion;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudT2TRequest;
import com.amisimecompila.speisandbox.operacion.api.dto.request.SolicitudVntRequest;
import com.amisimecompila.speisandbox.operacion.domain.Escenario;
import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.OperacionEntity;
import org.springframework.stereotype.Component;

@Component
public class OperacionPersistenceMapper {

    public OperacionEntity toEntity(
            SolicitudOperacion solicitud,
            String id,
            String payloadHash,
            Escenario escenario
    ) {
        OperacionEntity entity = OperacionEntity.nueva();
        entity.setId(id);
        entity.setReferenciaSeguimiento(solicitud.referenciaSeguimiento());
        entity.setTipoOperacion(solicitud.tipoOperacion());
        entity.setImporteValor(solicitud.importe().valor());
        entity.setImporteDivisa(solicitud.importe().divisa());
        entity.setReceptorInstitucion(solicitud.receptor().institucion());
        entity.setReceptorCuenta(solicitud.receptor().cuenta());
        entity.setReceptorNombre(solicitud.receptor().nombre());
        entity.setConcepto(solicitud.concepto());
        entity.setFolioNumerico(solicitud.folioNumerico());
        entity.setPayloadHash(payloadHash);
        entity.setEscenario(escenario);

        if (solicitud instanceof SolicitudT2TRequest t2t) {
            mapearT2T(t2t, entity);
        } else if (solicitud instanceof SolicitudVntRequest vnt) {
            mapearVnt(vnt, entity);
        }

        return entity;
    }

    private void mapearT2T(
            SolicitudT2TRequest solicitud,
            OperacionEntity entity
    ) {
        entity.setEmisorInstitucion(solicitud.emisor().institucion());
        entity.setEmisorCuenta(solicitud.emisor().cuenta());
        entity.setEmisorNombre(solicitud.emisor().nombre());
        entity.setEmisorIdentificacionFiscal(
                solicitud.emisor().identificacionFiscal()
        );
    }

    private void mapearVnt(
            SolicitudVntRequest solicitud,
            OperacionEntity entity
    ) {
        entity.setEmisorInstitucion(solicitud.emisor().institucion());
        entity.setEmisorSucursal(solicitud.emisor().sucursal());
        entity.setEmisorNombre(solicitud.emisor().nombre());
        entity.setEmisorDocumentoTipo(
                solicitud.emisor().documentoIdentidad().tipo()
        );
        entity.setEmisorDocumentoNumero(
                solicitud.emisor().documentoIdentidad().numero()
        );
    }
}
