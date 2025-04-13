package com.comismar.informes.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "informes")
public class Informe {

    @PrimaryKey(autoGenerate = true)
    public int id;

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


}
