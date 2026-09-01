package com.comismar.informes.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "informes")
public class Informe {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "tipo_informe")
    public String tipoInforme; // "NAVAL" o "MERCANCIA"

    @ColumnInfo(name = "referencia")
    public String referencia;

    @ColumnInfo(name = "tipo")
    public String tipo;

    @ColumnInfo(name = "trabajo")
    public String trabajo;

    @ColumnInfo(name = "descripcion")
    public String descripcion;

    @ColumnInfo(name = "ruta_pdf")
    public String rutaPdf;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "siniestro")
    public String siniestro;

    @ColumnInfo(name = "requirente")
    public String requirente;

    @ColumnInfo(name = "lugar")
    public String lugar;

    @ColumnInfo(name = "tecnico")
    public String tecnico;

    @ColumnInfo(name = "nombre_barco")
    public String nombreBarco;

    @ColumnInfo(name = "matricula")
    public String matricula;

    @ColumnInfo(name = "danos")
    public String danos;

    @ColumnInfo(name = "causas")
    public String causas;

    @ColumnInfo(name = "reserva")
    public String reserva;

    @ColumnInfo(name = "observaciones")
    public String observaciones;

    @ColumnInfo(name = "doc_pendiente")
    public String docPendiente;

    @ColumnInfo(name = "fotos_uris")
    public String fotosUris;

    // Campos específicos de Mercancía
    @ColumnInfo(name = "asegurado")
    public String asegurado;

    @ColumnInfo(name = "fecha_inspeccion")
    public String fechaInspeccion;

    @ColumnInfo(name = "otras_personas")
    public String otrasPersonas;

    @ColumnInfo(name = "bultos_peso")
    public String bultosPeso;

    @ColumnInfo(name = "valor_mercancia")
    public String valorMercancia;

    @ColumnInfo(name = "medio_transporte")
    public String medioTransporte;

    @ColumnInfo(name = "fecha_carga")
    public String fechaCarga;

    @ColumnInfo(name = "fecha_descarga")
    public String fechaDescarga;

    @ColumnInfo(name = "fecha_siniestro_lugar")
    public String fechaSiniestroLugar;

    @ColumnInfo(name = "actualizaciones")
    public String actualizaciones;

}
