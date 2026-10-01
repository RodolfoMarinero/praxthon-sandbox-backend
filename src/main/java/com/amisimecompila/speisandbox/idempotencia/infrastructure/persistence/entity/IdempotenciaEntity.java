package com.amisimecompila.speisandbox.idempotencia.infrastructure.persistence.entity;

import com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity.OperacionEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotencias")
public class IdempotenciaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave_idempotencia", nullable = false, unique = true, length = 36)
    private String clave;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operacion_id", nullable = false, unique = true)
    private OperacionEntity operacion;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_expiracion", nullable = false)
    private Instant fechaExpiracion;

    protected IdempotenciaEntity() {

    }
    public IdempotenciaEntity(
            String clave,
            String payloadHash,
            OperacionEntity operacion
    ) {
        this.clave = clave;
        this.payloadHash = payloadHash;
        this.operacion = operacion;
        this.fechaCreacion = Instant.now();
        this.fechaExpiracion = fechaCreacion.plusSeconds(86_400);
    }

    public String getPayloadHash() {

        return payloadHash;

    }
    public OperacionEntity getOperacion() {
        return operacion;
    }
}
