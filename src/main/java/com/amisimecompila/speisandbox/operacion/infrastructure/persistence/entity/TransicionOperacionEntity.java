package com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity;

import com.amisimecompila.speisandbox.operacion.domain.EstadoOperacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "transiciones_operacion")
public class TransicionOperacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operacion_id", nullable = false)
    private OperacionEntity operacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOperacion estado;

    @Column(nullable = false)
    private Instant momento;

    @Column(length = 20)
    private String motivo;

    @Column(name = "tipo_devolucion", length = 20)
    private String tipoDevolucion;

    @Column(name = "actor_solicitante", length = 30)
    private String actorSolicitante;

    protected TransicionOperacionEntity() {

    }
    public TransicionOperacionEntity(
            EstadoOperacion estado,
            String motivo
    ) {
        this.estado = estado;
        this.motivo = motivo;
        this.momento = Instant.now();
    }

    void asignarOperacion(OperacionEntity operacion) {
        this.operacion = operacion;
    }

    public EstadoOperacion getEstado() {

        return estado;

    }
    public Instant getMomento() {
        return momento;
    }
    public String getMotivo() {
        return motivo;
    }
    public void setTipoDevolucion(String value) {
        tipoDevolucion = value;
    }
    public void setActorSolicitante(String value) {
        actorSolicitante = value;
    }
}
