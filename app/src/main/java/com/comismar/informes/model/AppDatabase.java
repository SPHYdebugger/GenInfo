package com.comismar.informes.model;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Informe.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract InformeDao informeDao();

    private static AppDatabase INSTANCE;

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "comismar_db")
                    .allowMainThreadQueries() // ⚠️ SOLO para pruebas
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE informes ADD COLUMN siniestro TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN requirente TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN lugar TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN tecnico TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN nombre_barco TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN matricula TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN danos TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN causas TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN reserva TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN observaciones TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN doc_pendiente TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN fotos_uris TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE informes ADD COLUMN tipo_informe TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN asegurado TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN fecha_inspeccion TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN otras_personas TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN bultos_peso TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN valor_mercancia TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN medio_transporte TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN fecha_carga TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN fecha_descarga TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN fecha_siniestro_lugar TEXT");
            database.execSQL("ALTER TABLE informes ADD COLUMN actualizaciones TEXT");
        }
    };
}
