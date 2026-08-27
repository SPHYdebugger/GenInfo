package com.comismar.informes.view.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.comismar.informes.R;
import com.comismar.informes.model.AppDatabase;
import com.comismar.informes.model.Informe;
import com.comismar.informes.view.utils.AppLogger;
import com.comismar.informes.view.utils.AppSettings;

import java.io.File;
import java.util.List;

import org.json.JSONArray;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnAjustarCorreo = findViewById(R.id.btnAjustarCorreo);
        Button btnVerLogs = findViewById(R.id.btnVerLogs);
        Button btnCambiarIdioma = findViewById(R.id.btnCambiarIdioma);
        Button btnBorrarDatos = findViewById(R.id.btnBorrarDatos);
        Button btnBackSettings = findViewById(R.id.btnBackSettings);

        btnAjustarCorreo.setOnClickListener(v -> abrirDialogoCorreo());
        btnVerLogs.setOnClickListener(v -> startActivity(new Intent(this, LogsActivity.class)));
        btnCambiarIdioma.setOnClickListener(v -> alternarIdioma());
        btnBorrarDatos.setOnClickListener(v -> confirmarBorradoTotal());
        btnBackSettings.setOnClickListener(v -> finish());
    }

    private void abrirDialogoCorreo() {
        String correoActual = AppSettings.getRecipientEmail(this);
        boolean autoEnvioActual = AppSettings.isAutoSendEmailEnabled(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setText(correoActual);
        input.setHint(R.string.recipient_account_message);
        layout.addView(input);

        CheckBox cbAutoSend = new CheckBox(this);
        cbAutoSend.setText(R.string.send_pdf_auto_email);
        cbAutoSend.setChecked(autoEnvioActual);
        cbAutoSend.setPadding(0, 20, 0, 0);
        layout.addView(cbAutoSend);

        new AlertDialog.Builder(this)
                .setTitle(R.string.recipient_account_title)
                .setView(layout)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String nuevoCorreo = input.getText().toString().trim();
                    if (nuevoCorreo.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(nuevoCorreo).matches()) {
                        Toast.makeText(this, R.string.invalid_email, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AppSettings.setRecipientEmail(this, nuevoCorreo);
                    AppSettings.setAutoSendEmailEnabled(this, cbAutoSend.isChecked());
                    Toast.makeText(this, R.string.email_updated, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmarBorradoTotal() {
        Toast.makeText(this, R.string.delete_data_warning, Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_data_title)
                .setMessage(R.string.delete_data_message)
                .setPositiveButton(R.string.delete_action, (dialog, which) -> borrarTodosLosDatos())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void alternarIdioma() {
        String actual = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        boolean en = actual != null && actual.toLowerCase().startsWith("en");
        String nuevo = en ? "es" : "en";
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(nuevo));
        Toast.makeText(this, en ? R.string.language_changed_es : R.string.language_changed_en, Toast.LENGTH_SHORT).show();
        recreate();
    }

    private void borrarTodosLosDatos() {
        try {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            List<Informe> informes = db.informeDao().obtenerTodos();

            for (Informe informe : informes) {
                borrarArchivoAppSiCorresponde(informe.rutaPdf);
                borrarFotosSerializadas(informe.fotosUris);
            }

            // Limpieza adicional solo de ficheros con nombres generados por la app.
            limpiarArchivosGeneradosEnApp();

            db.informeDao().borrarTodos();
            AppLogger.clearLogs(this);
            Toast.makeText(this, R.string.data_deleted, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AppLogger.logError(this, "SettingsActivity", "Fallo borrando datos", e);
            Toast.makeText(this, R.string.delete_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void borrarFotosSerializadas(String fotosUrisJson) {
        if (fotosUrisJson == null || fotosUrisJson.trim().isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(fotosUrisJson);
            for (int i = 0; i < arr.length(); i++) {
                String uriText = arr.optString(i, null);
                if (uriText == null || uriText.trim().isEmpty()) continue;
                android.net.Uri uri = android.net.Uri.parse(uriText);
                if ("file".equalsIgnoreCase(uri.getScheme())) {
                    borrarArchivoAppSiCorresponde(uri.getPath());
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void borrarArchivoAppSiCorresponde(String path) {
        if (path == null || path.trim().isEmpty()) return;
        File file = new File(path);
        if (!file.exists()) return;
        if (!esRutaDentroDeExternoApp(file)) return;
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    private boolean esRutaDentroDeExternoApp(File file) {
        File appExternal = getExternalFilesDir(null);
        if (appExternal == null) return false;
        try {
            String base = appExternal.getCanonicalPath();
            String target = file.getCanonicalPath();
            return target.startsWith(base + File.separator) || target.equals(base);
        } catch (Exception e) {
            return false;
        }
    }

    private void limpiarArchivosGeneradosEnApp() {
        File appExternal = getExternalFilesDir(null);
        if (appExternal == null || !appExternal.exists()) return;

        File[] rootFiles = appExternal.listFiles();
        if (rootFiles != null) {
            for (File f : rootFiles) {
                if (f.isFile() && nombreEsGeneradoPorApp(f.getName())) {
                    //noinspection ResultOfMethodCallIgnored
                    f.delete();
                }
            }
        }

        File pictures = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        if (pictures != null && pictures.exists()) {
            File[] picFiles = pictures.listFiles();
            if (picFiles != null) {
                for (File f : picFiles) {
                    if (f.isFile() && nombreEsGeneradoPorApp(f.getName())) {
                        //noinspection ResultOfMethodCallIgnored
                        f.delete();
                    }
                }
            }
        }
    }

    private boolean nombreEsGeneradoPorApp(String name) {
        if (name == null) return false;
        return name.startsWith("informe_")
                || name.startsWith("foto_")
                || name.startsWith("galeria_")
                || name.endsWith("_MOD.pdf")
                || name.endsWith("_COP.pdf");
    }
}
