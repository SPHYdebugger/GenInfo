package com.comismar.informes.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.comismar.informes.R;
import com.comismar.informes.model.Informe;
import com.comismar.informes.view.activity.PdfPreviewActivity;

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
        holder.txtReferencia.setText(context.getString(R.string.reference_colon, informe.referencia));
        
        String tipoMostrar = informe.tipoInforme != null ? informe.tipoInforme : informe.tipo;
        holder.txtTipo.setText(context.getString(R.string.type_colon, tipoMostrar));
        
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault());
        String fechaFormateada = formato.format(new Date(informe.timestamp));
        holder.txtFecha.setText(context.getString(R.string.date_colon, fechaFormateada));

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) {
                return;
            }
            Informe current = informes.get(adapterPos);
            String rutaPdf = current.rutaPdf;
            if (rutaPdf != null) {
                android.content.Intent intent = new android.content.Intent(context, PdfPreviewActivity.class);
                intent.putExtra(PdfPreviewActivity.EXTRA_RUTA_PDF, rutaPdf);
                intent.putExtra(PdfPreviewActivity.EXTRA_INFORME_ID, current.id);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, R.string.pdf_not_found, Toast.LENGTH_SHORT).show();
            }
        });

        holder.iconoBorrar.setOnClickListener(v -> {
            if (deleteListener != null) {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_POSITION) {
                    return;
                }
                deleteListener.onEliminarInforme(informes.get(adapterPos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return informes.size();
    }


}
