package com.comismar.informes.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.comismar.informes.R;

public class SeleccionTipoInformeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccion_tipo);

        Button btnNaval = findViewById(R.id.btnNaval);
        Button btnMercancia = findViewById(R.id.btnMercancia);
        Button btnBack = findViewById(R.id.btnBackSelect);

        btnNaval.setOnClickListener(v -> {
            Intent intent = new Intent(this, GenerarInformeActivity.class);
            startActivity(intent);
        });

        btnMercancia.setOnClickListener(v -> {
            Intent intent = new Intent(this, GenerarInformeMercanciaActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
