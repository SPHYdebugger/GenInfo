package com.comismar.informes.model;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class InformeDao_Impl implements InformeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Informe> __insertionAdapterOfInforme;

  private final EntityDeletionOrUpdateAdapter<Informe> __deletionAdapterOfInforme;

  public InformeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfInforme = new EntityInsertionAdapter<Informe>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `informes` (`id`,`referencia`,`tipo`,`trabajo`,`descripcion`,`ruta_pdf`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Informe entity) {
        statement.bindLong(1, entity.id);
        if (entity.referencia == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.referencia);
        }
        if (entity.tipo == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.tipo);
        }
        if (entity.trabajo == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.trabajo);
        }
        if (entity.descripcion == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.descripcion);
        }
        if (entity.rutaPdf == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.rutaPdf);
        }
        statement.bindLong(7, entity.timestamp);
      }
    };
    this.__deletionAdapterOfInforme = new EntityDeletionOrUpdateAdapter<Informe>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `informes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Informe entity) {
        statement.bindLong(1, entity.id);
      }
    };
  }

  @Override
  public void insertar(final Informe informe) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfInforme.insert(informe);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void eliminar(final Informe informe) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfInforme.handle(informe);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Informe> obtenerTodos() {
    final String _sql = "SELECT * FROM informes ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfReferencia = CursorUtil.getColumnIndexOrThrow(_cursor, "referencia");
      final int _cursorIndexOfTipo = CursorUtil.getColumnIndexOrThrow(_cursor, "tipo");
      final int _cursorIndexOfTrabajo = CursorUtil.getColumnIndexOrThrow(_cursor, "trabajo");
      final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
      final int _cursorIndexOfRutaPdf = CursorUtil.getColumnIndexOrThrow(_cursor, "ruta_pdf");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final List<Informe> _result = new ArrayList<Informe>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Informe _item;
        _item = new Informe();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfReferencia)) {
          _item.referencia = null;
        } else {
          _item.referencia = _cursor.getString(_cursorIndexOfReferencia);
        }
        if (_cursor.isNull(_cursorIndexOfTipo)) {
          _item.tipo = null;
        } else {
          _item.tipo = _cursor.getString(_cursorIndexOfTipo);
        }
        if (_cursor.isNull(_cursorIndexOfTrabajo)) {
          _item.trabajo = null;
        } else {
          _item.trabajo = _cursor.getString(_cursorIndexOfTrabajo);
        }
        if (_cursor.isNull(_cursorIndexOfDescripcion)) {
          _item.descripcion = null;
        } else {
          _item.descripcion = _cursor.getString(_cursorIndexOfDescripcion);
        }
        if (_cursor.isNull(_cursorIndexOfRutaPdf)) {
          _item.rutaPdf = null;
        } else {
          _item.rutaPdf = _cursor.getString(_cursorIndexOfRutaPdf);
        }
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
