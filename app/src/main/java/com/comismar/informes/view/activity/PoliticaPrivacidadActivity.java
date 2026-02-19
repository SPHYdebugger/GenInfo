package com.comismar.informes.view.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.comismar.informes.R;

public class PoliticaPrivacidadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documento_legal);

        TextView txtTitulo = findViewById(R.id.txtTituloDocumento);
        TextView txtBody = findViewById(R.id.txtBodyDocumento);
        Button btnVolver = findViewById(R.id.btnVolverDocumento);

        txtTitulo.setText(R.string.privacy_title);
        txtBody.setText(getString(R.string.privacy_body));
        btnVolver.setOnClickListener(v -> finish());
    }
}
