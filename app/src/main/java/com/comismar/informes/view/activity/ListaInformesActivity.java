package com.comismar.informes.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.comismar.informes.R;
import com.comismar.informes.model.AppDatabase;
import com.comismar.informes.model.Informe;
import com.comismar.informes.view.adapter.InformeAdapter;
import com.comismar.informes.view.adapter.OnInformeDeleteListener;
import com.comismar.informes.view.utils.AppLogger;

import java.io.File;
import java.util.List;

public class ListaInformesActivity extends AppCompatActivity implements OnInformeDeleteListener {

    private RecyclerView recyclerInformes;
    private InformeAdapter adapter;
    private List<Informe> listaInformes;
    private TextView txtSinInformes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_informes);

        // Obtener datos desde Room (modelo)
        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            listaInformes = db.informeDao().obtenerTodos();
        } catch (Exception e) {
            AppLogger.logError(this, "ListaInformesActivity", "No se pudieron ver los informes", e);
            Toast.makeText(this, R.string.error_loading_reports, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Mostrar cuántos informes hay
        Toast.makeText(this, getString(R.string.reports_found_count, listaInformes.size()), Toast.LENGTH_SHORT).show();

        // Configurar RecyclerView
        recyclerInformes = findViewById(R.id.recyclerInformes);
        txtSinInformes = findViewById(R.id.txtSinInformes);
        Button btnVolverLista = findViewById(R.id.btnVolverLista);
        recyclerInformes.setLayoutManager(new LinearLayoutManager(this));
        recyclerInformes.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
        adapter = new InformeAdapter(this, listaInformes, this::onEliminarInforme);
        recyclerInformes.setAdapter(adapter);
        actualizarEstadoVacio();

        btnVolverLista.setOnClickListener(v -> {
            Intent intent = new Intent(ListaInformesActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }


    @Override
    public void onEliminarInforme(Informe informe) {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.delete_report)
                .setMessage(R.string.delete_report_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    int position = listaInformes.indexOf(informe);
                    if (position == -1) return;

                    RecyclerView.ViewHolder viewHolder = recyclerInformes.findViewHolderForAdapterPosition(position);
                    if (viewHolder != null) {
                        viewHolder.itemView.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction(() -> {
                                    eliminarInformeYActualizar(informe, position);
                                })
                                .start();
                    } else {
                        eliminarInformeYActualizar(informe, position);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }



    private void eliminarInformeYActualizar(Informe informe, int position) {
        // Eliminar archivo PDF
        if (informe.rutaPdf != null) {
            File archivo = new File(informe.rutaPdf);
            if (archivo.exists()) archivo.delete();
        }

        // Eliminar de la base de datos y lista
        AppDatabase.getInstance(getApplicationContext()).informeDao().eliminar(informe);
        AppLogger.logSuccess(this, "ListaInformesActivity", "Informe eliminado: " + informe.referencia);
        listaInformes.remove(position);

        // Notificar eliminación con animación
        adapter.notifyItemRemoved(position);
        actualizarEstadoVacio();

        Toast.makeText(this, R.string.report_deleted, Toast.LENGTH_SHORT).show();
    }

    private void actualizarEstadoVacio() {
        if (listaInformes == null || listaInformes.isEmpty()) {
            txtSinInformes.setVisibility(View.VISIBLE);
            recyclerInformes.setVisibility(View.GONE);
        } else {
            txtSinInformes.setVisibility(View.GONE);
            recyclerInformes.setVisibility(View.VISIBLE);
        }
    }


}
