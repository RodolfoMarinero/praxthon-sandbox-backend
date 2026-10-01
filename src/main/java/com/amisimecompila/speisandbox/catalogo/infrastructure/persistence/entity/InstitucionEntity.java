package com.amisimecompila.speisandbox.catalogo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "instituciones")
public class InstitucionEntity {

    @Id
    @Column(nullable = false, length = 3)
    private String codigo;

    @Nationalized
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "permite_emision", nullable = false)
    private boolean permiteEmision;

    @Column(nullable = false)
    private boolean disponible;

    protected InstitucionEntity() {

    }
    public String getCodigo() {

        return codigo;

    }
    public String getNombre() {

        return nombre;

    }
    public boolean isPermiteEmision() {

        return permiteEmision;

    }
    public boolean isDisponible() {

        return disponible;

    }
}
