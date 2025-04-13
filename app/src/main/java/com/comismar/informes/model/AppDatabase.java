package com.comismar.informes.model;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Informe.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InformeDao informeDao();

    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "comismar_db")
                    .allowMainThreadQueries() // ⚠️ SOLO para pruebas
                    .build();
        }
        return INSTANCE;
    }
}
