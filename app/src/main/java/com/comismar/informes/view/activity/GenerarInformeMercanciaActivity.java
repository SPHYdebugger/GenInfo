package com.comismar.informes.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.comismar.informes.R;
import com.comismar.informes.model.AppDatabase;
import com.comismar.informes.model.Informe;
import com.comismar.informes.view.adapter.MailSender;
import com.comismar.informes.view.utils.AppLogger;
import com.comismar.informes.view.utils.AppSettings;
import com.comismar.informes.view.utils.InformeMercanciaPdfGenerator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;

public class GenerarInformeMercanciaActivity extends Activity {

    private EditText inputReferencia, inputSiniestro, inputAsegurado, inputRequirente;
    private EditText inputFechaInspeccion, inputLugar, inputTecnico, inputOtrasPersonas;
    private EditText inputBultosPeso, inputValorMercancia, inputMedioTransporte;
    private EditText inputFechaCarga, inputFechaDescarga, inputFechaSiniestroLugar;
    private EditText inputDanos, inputCausas, inputReserva, inputObservaciones, inputDocP, inputActualizaciones;
    
    private Button btnAdjuntarFotos, btnGenerarInforme, btnBack;
    private LinearLayout layoutImagenesAdjuntas;
    private TextView textAdjuntos;
    private View overlayBloqueo;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int TOAST_SHORT_MS = 2000;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private List<Uri> imagenesAdjuntas = new ArrayList<>();
    private Uri uriFotoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_mercancia);

        initViews();

        btnAdjuntarFotos.setOnClickListener(v -> mostrarDialogoFuenteImagen());
        btnBack.setOnClickListener(v -> finish());
        
        btnGenerarInforme.setOnClickListener(v -> {
            if (validarCampos()) {
                mostrarConfirmacion();
            }
        });
    }

    private void initViews() {
        inputReferencia = findViewById(R.id.inputReferencia);
        inputSiniestro = findViewById(R.id.inputSiniestro);
        inputAsegurado = findViewById(R.id.inputAsegurado);
        inputRequirente = findViewById(R.id.inputRequirente);
        inputFechaInspeccion = findViewById(R.id.inputFechaInspeccion);
        inputLugar = findViewById(R.id.inputLugar);
        inputTecnico = findViewById(R.id.inputTecnico);
        inputOtrasPersonas = findViewById(R.id.inputOtrasPersonas);
        inputBultosPeso = findViewById(R.id.inputBultosPeso);
        inputValorMercancia = findViewById(R.id.inputValorMercancia);
        inputMedioTransporte = findViewById(R.id.inputMedioTransporte);
        inputFechaCarga = findViewById(R.id.inputFechaCarga);
        inputFechaDescarga = findViewById(R.id.inputFechaDescarga);
        inputFechaSiniestroLugar = findViewById(R.id.inputFechaSiniestroLugar);
        inputDanos = findViewById(R.id.inputDanos);
        inputCausas = findViewById(R.id.inputCausas);
        inputReserva = findViewById(R.id.inputReserva);
        inputObservaciones = findViewById(R.id.inputObservaciones);
        inputDocP = findViewById(R.id.inputDocP);
        inputActualizaciones = findViewById(R.id.inputActualizaciones);

        btnAdjuntarFotos = findViewById(R.id.btnAdjuntarFotos);
        btnGenerarInforme = findViewById(R.id.btnGenerarInforme);
        btnBack = findViewById(R.id.btnBack);
        layoutImagenesAdjuntas = findViewById(R.id.layoutImagenesAdjuntas);
        textAdjuntos = findViewById(R.id.textAdjuntos);
        overlayBloqueo = findViewById(R.id.overlayBloqueo);

        CheckBox cbSendCargoCopy = findViewById(R.id.cbSendCargoCopy);
        String cargoEmail = AppSettings.getCargoEmail(this);
        cbSendCargoCopy.setText(getString(R.string.checkbox_send_cargo_copy, cargoEmail));
    }

    private boolean validarCampos() {
        String ref = inputReferencia.getText().toString().trim();
        String sin = inputSiniestro.getText().toString().trim();
        String lug = inputLugar.getText().toString().trim();

        if (ref.isEmpty() || sin.isEmpty() || lug.isEmpty()) {
            new AlertDialog.Builder(this, R.style.AppDialogTheme)
                    .setTitle(R.string.required_fields_title)
                    .setMessage(R.string.required_fields_intro)
                    .setPositiveButton(R.string.close, null)
                    .show();
            return false;
        }
        return true;
    }

    private void mostrarConfirmacion() {
        String ref = inputReferencia.getText().toString().trim();
        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.confirm_create_report_title)
                .setMessage(getString(R.string.confirm_create_report, ref))
                .setPositiveButton(R.string.create, (dialog, which) -> mostrarDialogoOptimizacion())
                .setNegativeButton(R.string.cancel_upper, null)
                .show();
    }

    private void mostrarDialogoOptimizacion() {
        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.optimize_photos_title)
                .setMessage(R.string.optimize_photos_message)
                .setPositiveButton(R.string.yes_upper, (dialog, which) -> generarInforme(true))
                .setNegativeButton(R.string.no_upper, (dialog, which) -> generarInforme(false))
                .show();
    }

    private void generarInforme(boolean optimizarFotos) {
        setOverlayVisible(true);
        AppLogger.logInfo(this, "GenerarInformeMercancia", "Iniciando generación de informe mercancía: " + inputReferencia.getText().toString());

        Informe informe = new Informe();
        informe.tipoInforme = "MERCANCIA";
        informe.tipo = "MERCANCÍA";
        informe.referencia = inputReferencia.getText().toString().trim();
        informe.siniestro = inputSiniestro.getText().toString().trim();
        informe.asegurado = inputAsegurado.getText().toString().trim();
        informe.requirente = inputRequirente.getText().toString().trim();
        informe.fechaInspeccion = inputFechaInspeccion.getText().toString().trim();
        informe.lugar = inputLugar.getText().toString().trim();
        informe.tecnico = inputTecnico.getText().toString().trim();
        informe.otrasPersonas = inputOtrasPersonas.getText().toString().trim();
        informe.bultosPeso = inputBultosPeso.getText().toString().trim();
        informe.valorMercancia = inputValorMercancia.getText().toString().trim();
        informe.medioTransporte = inputMedioTransporte.getText().toString().trim();
        informe.fechaCarga = inputFechaCarga.getText().toString().trim();
        informe.fechaDescarga = inputFechaDescarga.getText().toString().trim();
        informe.fechaSiniestroLugar = inputFechaSiniestroLugar.getText().toString().trim();
        informe.danos = inputDanos.getText().toString().trim();
        informe.causas = inputCausas.getText().toString().trim();
        informe.reserva = inputReserva.getText().toString().trim();
        informe.observaciones = inputObservaciones.getText().toString().trim();
        informe.docPendiente = inputDocP.getText().toString().trim();
        informe.actualizaciones = inputActualizaciones.getText().toString().trim();
        informe.fotosUris = serializarUris(imagenesAdjuntas);
        informe.timestamp = System.currentTimeMillis();

        File pdfGenerado = InformeMercanciaPdfGenerator.generarPdf(this, getExternalFilesDir(null), informe, imagenesAdjuntas, optimizarFotos);

        if (pdfGenerado == null) {
            AppLogger.logError(this, "GenerarInformeMercancia", "Error al generar PDF", null);
            Toast.makeText(this, R.string.error_generating_pdf, Toast.LENGTH_SHORT).show();
            setOverlayVisible(false);
            return;
        }

        informe.rutaPdf = pdfGenerado.getAbsolutePath();

        try {
            AppDatabase.getInstance(this).informeDao().insertar(informe);
            AppLogger.logSuccess(this, "GenerarInformeMercancia", "Informe guardado en DB");
        } catch (Exception e) {
            AppLogger.logError(this, "GenerarInformeMercancia", "Error al guardar en DB", e);
        }

        if (AppSettings.isAutoSendEmailEnabled(this)) {
            enviarCorreo(informe, pdfGenerado);
        } else {
            finalizarFlujo();
        }
    }

    private void enviarCorreo(Informe informe, File pdf) {
        final String destinatarioBackup = AppSettings.getRecipientEmail(this);
        final boolean enviarCopiaCargo = ((CheckBox) findViewById(R.id.cbSendCargoCopy)).isChecked();

        new Thread(() -> {
            try {
                MailSender sender = new MailSender(null, null);
                String cuerpoHtml = construirCuerpoHtml(informe);

                List<String> toEmails = new ArrayList<>();
                List<String> bccEmails = new ArrayList<>();

                if (enviarCopiaCargo) {
                    toEmails.add(AppSettings.getCargoEmail(getApplicationContext()));
                }

                if (toEmails.isEmpty()) {
                    toEmails.add(destinatarioBackup);
                } else {
                    bccEmails.add(destinatarioBackup);
                }

                sender.enviarCorreo(getApplicationContext(), getString(R.string.email_subject_report, informe.referencia), cuerpoHtml, "infogenpdf@gmail.com", toEmails, bccEmails, pdf);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.pdf_sent_email, Toast.LENGTH_SHORT).show();
                    finalizarFlujo();
                });
            } catch (Exception e) {
                AppLogger.logError(this, "GenerarInformeMercancia", "Error al enviar email", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_sending_pdf, Toast.LENGTH_SHORT).show();
                    finalizarFlujo();
                });
            }
        }).start();
    }

    private String construirCuerpoHtml(Informe inf) {
        String base64Logo = MailSender.getResourceToBase64(this, R.drawable.horizontal);
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: Arial, sans-serif; color: #333;\">");
        html.append("<div style=\"text-align: center; margin-bottom: 20px;\">");
        html.append("<img src=\"data:image/jpeg;base64,").append(base64Logo).append("\" style=\"max-width: 100%; height: auto;\">");
        html.append("</div>");
        html.append("<h2 style=\"color: #004a99; border-bottom: 2px solid #004a99; padding-bottom: 10px;\">Informe de Peritaje de Mercancía</h2>");
        html.append("<table style=\"width: 100%; border-collapse: collapse;\">");
        appendRow(html, "Referencia", inf.referencia);
        appendRow(html, "Siniestro", inf.siniestro);
        appendRow(html, "Asegurado", inf.asegurado);
        appendRow(html, "Lugar", inf.lugar);
        html.append("</table>");
        html.append("<p style=\"margin-top: 20px;\">Se adjunta el informe generado con GenInfor v4.1.</p>");
        html.append("<div style=\"margin-top: 30px; padding-top: 10px; border-top: 1px solid #ccc; font-size: 12px; color: #777;\">");
        html.append("<p>Desarrollada por <strong>Santiago Pérez</strong></p>");
        html.append("</div></body></html>");
        return html.toString();
    }

    private void appendRow(StringBuilder html, String label, String value) {
        html.append("<tr><td style=\"padding: 8px; font-weight: bold; width: 40%; border-bottom: 1px solid #eee;\">").append(label).append(":</td>");
        html.append("<td style=\"padding: 8px; border-bottom: 1px solid #eee;\">").append(value != null ? value : "").append("</td></tr>");
    }

    private void finalizarFlujo() {
        setOverlayVisible(false);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setOverlayVisible(boolean visible) {
        if (overlayBloqueo != null) {
            overlayBloqueo.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void mostrarDialogoFuenteImagen() {
        if (imagenesAdjuntas.size() >= 4) {
            Toast.makeText(this, R.string.max_4_images, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] opciones = { getString(R.string.take_photo), getString(R.string.choose_from_gallery) };
        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.attach_photo_title)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                abrirCamara();
                            } else {
                                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                            }
                        } else {
                            abrirCamara();
                        }
                    } else {
                        abrirGaleria();
                    }
                }).show();
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = File.createTempFile("foto_" + System.currentTimeMillis(), ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            } catch (IOException ignored) {}
            if (photoFile != null) {
                uriFotoActual = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriFotoActual);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_images)), REQUEST_IMAGE_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imagenesAdjuntas.add(uriFotoActual);
                actualizarListaImagenes();
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count && imagenesAdjuntas.size() < 4; i++) {
                        imagenesAdjuntas.add(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) {
                    imagenesAdjuntas.add(data.getData());
                }
                actualizarListaImagenes();
            }
        }
    }

    private void actualizarListaImagenes() {
        layoutImagenesAdjuntas.removeAllViews();
        textAdjuntos.setVisibility(imagenesAdjuntas.isEmpty() ? View.GONE : View.VISIBLE);
        for (Uri uri : imagenesAdjuntas) {
            TextView tv = new TextView(this);
            tv.setText(uri.getLastPathSegment());
            tv.setTextColor(android.graphics.Color.BLACK);
            tv.setPadding(0, 8, 0, 8);
            layoutImagenesAdjuntas.addView(tv);
        }
    }

    private String serializarUris(List<Uri> uris) {
        JSONArray array = new JSONArray();
        for (Uri uri : uris) {
            array.put(uri.toString());
        }
        return array.toString();
    }
}
