package com.amisimecompila.speisandbox.operacion.infrastructure.persistence.entity;

import com.amisimecompila.speisandbox.operacion.domain.Escenario;
import com.amisimecompila.speisandbox.operacion.domain.EstadoOperacion;
import com.amisimecompila.speisandbox.operacion.domain.TipoOperacion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "operaciones")
public class OperacionEntity {

    @Id
    @Column(nullable = false, length = 29)
    private String id;

    @Column(name = "referencia_seguimiento", nullable = false, length = 30)
    private String referenciaSeguimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false, length = 3)
    private TipoOperacion tipoOperacion;

    @Column(name = "importe_valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal importeValor;

    @Column(name = "importe_divisa", nullable = false, length = 3)
    private String importeDivisa;

    @Column(name = "emisor_institucion", nullable = false, length = 3)
    private String emisorInstitucion;

    @Column(name = "emisor_cuenta", length = 18)
    private String emisorCuenta;

    @Column(name = "emisor_sucursal", length = 20)
    private String emisorSucursal;

    @Nationalized
    @Column(name = "emisor_nombre", nullable = false, length = 40)
    private String emisorNombre;

    @Column(name = "emisor_identificacion_fiscal", length = 30)
    private String emisorIdentificacionFiscal;

    @Column(name = "emisor_documento_tipo", length = 20)
    private String emisorDocumentoTipo;

    @Column(name = "emisor_documento_numero", length = 50)
    private String emisorDocumentoNumero;

    @Column(name = "receptor_institucion", nullable = false, length = 3)
    private String receptorInstitucion;

    @Column(name = "receptor_cuenta", nullable = false, length = 18)
    private String receptorCuenta;

    @Nationalized
    @Column(name = "receptor_nombre", nullable = false, length = 40)
    private String receptorNombre;

    @Nationalized
    @Column(nullable = false, length = 40)
    private String concepto;

    @Column(name = "folio_numerico", nullable = false)
    private Integer folioNumerico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOperacion estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Escenario escenario;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;

    @Column(name = "payload_hash", nullable = false, length = 64)
    private String payloadHash;

    @Version
    @Column(nullable = false)
    private long version;

    @OneToMany(
            mappedBy = "operacion",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("momento ASC")
    private List<TransicionOperacionEntity> transiciones = new ArrayList<>();

    protected OperacionEntity() {

    }
    public static OperacionEntity nueva() {

        return new OperacionEntity();

    }
    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = Instant.now();
        }
        if (estado == null) {
            estado = EstadoOperacion.RECIBIDO;
        }
    }

    public void agregarTransicion(TransicionOperacionEntity transicion) {

        transicion.asignarOperacion(this);

        transiciones.add(transicion);

        estado = transicion.getEstado();

    }
    public String getId() {

        return id;

    }
    public void setId(String id) {
        this.id = id;
    }
    public String getReferenciaSeguimiento() {
        return referenciaSeguimiento;
    }
    public void setReferenciaSeguimiento(String value) {
        referenciaSeguimiento = value;
    }
    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }
    public void setTipoOperacion(TipoOperacion value) {
        tipoOperacion = value;
    }
    public BigDecimal getImporteValor() {
        return importeValor;
    }
    public void setImporteValor(BigDecimal value) {
        importeValor = value;
    }
    public String getImporteDivisa() {
        return importeDivisa;
    }
    public void setImporteDivisa(String value) {
        importeDivisa = value;
    }
    public String getEmisorInstitucion() {
        return emisorInstitucion;
    }
    public void setEmisorInstitucion(String value) {
        emisorInstitucion = value;
    }
    public String getEmisorCuenta() {
        return emisorCuenta;
    }
    public void setEmisorCuenta(String value) {
        emisorCuenta = value;
    }
    public String getEmisorSucursal() {
        return emisorSucursal;
    }
    public void setEmisorSucursal(String value) {
        emisorSucursal = value;
    }
    public String getEmisorNombre() {
        return emisorNombre;
    }
    public void setEmisorNombre(String value) {
        emisorNombre = value;
    }
    public String getEmisorIdentificacionFiscal() {
        return emisorIdentificacionFiscal;
    }
    public void setEmisorIdentificacionFiscal(String value) {
        emisorIdentificacionFiscal = value;
    }
    public String getEmisorDocumentoTipo() {
        return emisorDocumentoTipo;
    }
    public void setEmisorDocumentoTipo(String value) {
        emisorDocumentoTipo = value;
    }
    public String getEmisorDocumentoNumero() {
        return emisorDocumentoNumero;
    }
    public void setEmisorDocumentoNumero(String value) {
        emisorDocumentoNumero = value;
    }
    public String getReceptorInstitucion() {
        return receptorInstitucion;
    }
    public void setReceptorInstitucion(String value) {
        receptorInstitucion = value;
    }
    public String getReceptorCuenta() {
        return receptorCuenta;
    }
    public void setReceptorCuenta(String value) {
        receptorCuenta = value;
    }
    public String getReceptorNombre() {
        return receptorNombre;
    }
    public void setReceptorNombre(String value) {
        receptorNombre = value;
    }
    public String getConcepto() {
        return concepto;
    }
    public void setConcepto(String value) {
        concepto = value;
    }
    public Integer getFolioNumerico() {
        return folioNumerico;
    }
    public void setFolioNumerico(Integer value) {
        folioNumerico = value;
    }
    public EstadoOperacion getEstado() {
        return estado;
    }
    public Escenario getEscenario() {
        return escenario;
    }
    public void setEscenario(Escenario value) {
        escenario = value;
    }
    public Instant getFechaRegistro() {
        return fechaRegistro;
    }
    public String getPayloadHash() {
        return payloadHash;
    }
    public void setPayloadHash(String value) {
        payloadHash = value;
    }
    public long getVersion() {
        return version;
    }
    public List<TransicionOperacionEntity> getTransiciones() {
        return List.copyOf(transiciones);
    }
}
