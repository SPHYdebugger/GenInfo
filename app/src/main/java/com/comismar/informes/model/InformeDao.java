package com.comismar.informes.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InformeDao {

    @Insert
    void insertar(Informe informe);

    @Query("SELECT * FROM informes ORDER BY timestamp DESC")
    List<Informe> obtenerTodos();

    @Query("SELECT * FROM informes WHERE id = :id LIMIT 1")
    Informe obtenerPorId(int id);

    @Update
    void actualizar(Informe informe);

    @Query("DELETE FROM informes")
    void borrarTodos();

    @Delete
    void eliminar(Informe informe);
}
