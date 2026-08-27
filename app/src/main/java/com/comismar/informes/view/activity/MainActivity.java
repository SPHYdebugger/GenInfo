package com.comismar.informes.view.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.comismar.informes.R;
import com.comismar.informes.view.utils.AppSettings;
import com.comismar.informes.view.utils.KeepAliveWorker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private final Handler secretHandler = new Handler(Looper.getMainLooper());
    private final Runnable openSettingsRunnable = this::abrirSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnNuevoInforme = findViewById(R.id.btnNuevoInforme);
        Button btnVerInformes = findViewById(R.id.btnVerInformes);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnCerrarApp = findViewById(R.id.btnCerrarApp);
        ImageView headerImage = findViewById(R.id.headerImage);
        TextView txtPoliticaPrivacidad = findViewById(R.id.txtPoliticaPrivacidad);
        TextView txtDatosContacto = findViewById(R.id.txtDatosContacto);

        btnNuevoInforme.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GenerarInformeActivity.class);
            startActivity(intent);
        });

        btnVerInformes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListaInformesActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> mostrarConfirmacionLogout());

        btnCerrarApp.setOnClickListener(v -> closeApp());

        txtPoliticaPrivacidad.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PoliticaPrivacidadActivity.class);
            startActivity(intent);
        });

        txtDatosContacto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DatosContactoActivity.class);
            startActivity(intent);
        });

        programarMantenerClaveViva();

        headerImage.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    secretHandler.postDelayed(openSettingsRunnable, 10_000);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    secretHandler.removeCallbacks(openSettingsRunnable);
                    return true;
                default:
                    return true;
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        secretHandler.removeCallbacks(openSettingsRunnable);
    }

    private void abrirSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    private void closeApp() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
        finishAffinity();
    }

    private void mostrarConfirmacionLogout() {
        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.accept_upper, (dialog, which) -> ejecutarLogout())
                .setNegativeButton(R.string.cancel_upper, null)
                .show();
    }

    private void ejecutarLogout() {
        AppSettings.setRememberLoginEnabled(this, false);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void programarMantenerClaveViva() {
        PeriodicWorkRequest keepAliveRequest =
                new PeriodicWorkRequest.Builder(KeepAliveWorker.class, 60, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "BrevoApiKeyKeepAlive",
                ExistingPeriodicWorkPolicy.KEEP,
                keepAliveRequest
        );
    }
}
