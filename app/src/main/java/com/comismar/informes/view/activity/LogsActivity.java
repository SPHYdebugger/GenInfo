package com.comismar.informes.view.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.comismar.informes.R;
import com.comismar.informes.view.utils.AppLogger;

public class LogsActivity extends AppCompatActivity {

    private TextView txtLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        txtLogs = findViewById(R.id.txtLogs);
        Button btnClearLogs = findViewById(R.id.btnClearLogs);
        Button btnBackLogs = findViewById(R.id.btnBackLogs);

        cargarLogs();

        btnClearLogs.setOnClickListener(v -> {
            AppLogger.clearLogs(this);
            cargarLogs();
            Toast.makeText(this, R.string.logs_cleared, Toast.LENGTH_SHORT).show();
        });

        btnBackLogs.setOnClickListener(v -> finish());
    }

    private void cargarLogs() {
        txtLogs.setText(AppLogger.readLogs(this));
    }
}
