package com.comismar.informes.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.comismar.informes.R;
import com.comismar.informes.model.Informe;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InformeAdapter extends RecyclerView.Adapter<InformeAdapter.ViewHolder> {

    private List<Informe> informes;
    private Context context;
    private OnInformeDeleteListener deleteListener;


    public InformeAdapter(Context context, List<Informe> informes, OnInformeDeleteListener listener) {
        this.context = context;
        this.informes = informes;
        this.deleteListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtReferencia, txtTipo, txtFecha;
        ImageView iconoBorrar;

        public ViewHolder(View itemView) {
            super(itemView);
            txtReferencia = itemView.findViewById(R.id.txtReferencia);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            iconoBorrar = itemView.findViewById(R.id.btnEliminar);
        }
    }

    @NonNull
    @Override
    public InformeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(context).inflate(R.layout.item_informe, parent, false);
        return new ViewHolder(vista);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Informe informe = informes.get(position);
        holder.txtReferencia.setText("Referencia: " + informe.referencia);
        holder.txtTipo.setText("Tipo: " + informe.tipo);
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
        String fechaFormateada = formato.format(new Date(informe.timestamp));
        holder.txtFecha.setText("Fecha: " + fechaFormateada);

        holder.itemView.setOnClickListener(v -> {
            String rutaPdf = informes.get(position).rutaPdf;
            if (rutaPdf != null) {
                File file = new File(rutaPdf);
                Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                context.startActivity(intent);
            } else {
                Toast.makeText(context, "No se encontró el archivo PDF", Toast.LENGTH_SHORT).show();
            }
        });

        holder.iconoBorrar.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onEliminarInforme(informes.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return informes.size();
    }


}
