package com.comismar.informes.view.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.comismar.informes.R;
import com.comismar.informes.model.AppDatabase;
import com.comismar.informes.model.Informe;
import com.comismar.informes.view.adapter.InformeAdapter;
import com.comismar.informes.view.adapter.OnInformeDeleteListener;

import java.io.File;
import java.util.List;

public class ListaInformesActivity extends AppCompatActivity implements OnInformeDeleteListener {

    private RecyclerView recyclerInformes;
    private InformeAdapter adapter;
    private List<Informe> listaInformes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_informes);

        // Obtener datos desde Room (modelo)
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        listaInformes = db.informeDao().obtenerTodos();

        // Mostrar cuántos informes hay
        Toast.makeText(this, "Informes encontrados: " + listaInformes.size(), Toast.LENGTH_SHORT).show();

        // Configurar RecyclerView
        recyclerInformes = findViewById(R.id.recyclerInformes);
        recyclerInformes.setLayoutManager(new LinearLayoutManager(this));
        recyclerInformes.setItemAnimator(new androidx.recyclerview.widget.DefaultItemAnimator());
        adapter = new InformeAdapter(this, listaInformes, this::onEliminarInforme);
        recyclerInformes.setAdapter(adapter);
    }


    @Override
    public void onEliminarInforme(Informe informe) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar informe")
                .setMessage("¿Estás seguro de que quieres eliminar este informe?")
                .setPositiveButton("Sí", (dialog, which) -> {
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
                .setNegativeButton("Cancelar", null)
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
        listaInformes.remove(position);

        // Notificar eliminación con animación
        adapter.notifyItemRemoved(position);

        Toast.makeText(this, "Informe eliminado", Toast.LENGTH_SHORT).show();
    }



}
