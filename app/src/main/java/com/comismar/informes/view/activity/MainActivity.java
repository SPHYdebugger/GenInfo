
package com.comismar.informes.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.comismar.informes.R;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNuevoInforme = findViewById(R.id.btnNuevoInforme);
        Button btnVerInformes = findViewById(R.id.btnVerInformes);


        btnNuevoInforme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GenerarInformeActivity.class);
                startActivity(intent);
            }
        });

        btnVerInformes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ListaInformesActivity.class);
                startActivity(intent);
            }
        });
    }
}
