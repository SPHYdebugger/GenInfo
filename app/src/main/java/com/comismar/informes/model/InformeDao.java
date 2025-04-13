package com.comismar.informes.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface InformeDao {

    @Insert
    void insertar(Informe informe);

    @Query("SELECT * FROM informes ORDER BY timestamp DESC")
    List<Informe> obtenerTodos();

    @Delete
    void eliminar(Informe informe);
}
