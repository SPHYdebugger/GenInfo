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

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.graphics.BitmapFactory;
import java.util.ArrayList;
import java.util.List;

public class EditarInformeMercanciaActivity extends Activity {

    public static final String EXTRA_INFORME_ID = "informeId";

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

    private Informe informeOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_mercancia);

        initViews();
        btnGenerarInforme.setText(R.string.modify_report);

        int informeId = getIntent().getIntExtra(EXTRA_INFORME_ID, -1);
        informeOriginal = AppDatabase.getInstance(getApplicationContext()).informeDao().obtenerPorId(informeId);
        if (informeOriginal == null) {
            AppLogger.logError(this, "EditarInformeMercancia", "No se pudo cargar informe para editar", null);
            Toast.makeText(this, R.string.report_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        precargarDatos(informeOriginal);

        btnAdjuntarFotos.setOnClickListener(v -> mostrarDialogoFuenteImagen());
        btnGenerarInforme.setOnClickListener(v -> onModificarInforme());
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaInformesActivity.class);
            startActivity(intent);
            finish();
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

    private void precargarDatos(Informe informe) {
        inputReferencia.setText(informe.referencia);
        inputSiniestro.setText(informe.siniestro);
        inputAsegurado.setText(informe.asegurado);
        inputRequirente.setText(informe.requirente);
        inputFechaInspeccion.setText(informe.fechaInspeccion);
        inputLugar.setText(informe.lugar);
        inputTecnico.setText(informe.tecnico);
        inputOtrasPersonas.setText(informe.otrasPersonas);
        inputBultosPeso.setText(informe.bultosPeso);
        inputValorMercancia.setText(informe.valorMercancia);
        inputMedioTransporte.setText(informe.medioTransporte);
        inputFechaCarga.setText(informe.fechaCarga);
        inputFechaDescarga.setText(informe.fechaDescarga);
        inputFechaSiniestroLugar.setText(informe.fechaSiniestroLugar);
        inputDanos.setText(informe.danos);
        inputCausas.setText(informe.causas);
        inputReserva.setText(informe.reserva);
        inputObservaciones.setText(informe.observaciones);
        inputDocP.setText(informe.docPendiente);
        inputActualizaciones.setText(informe.actualizaciones);

        imagenesAdjuntas = deserializarUris(informe.fotosUris);
        actualizarListaImagenes();
    }

    private void onModificarInforme() {
        String ref = inputReferencia.getText().toString().trim();
        String sin = inputSiniestro.getText().toString().trim();
        String lug = inputLugar.getText().toString().trim();

        if (ref.isEmpty() || sin.isEmpty() || lug.isEmpty()) {
            new AlertDialog.Builder(this, R.style.AppDialogTheme)
                    .setTitle(R.string.required_fields_title)
                    .setMessage(R.string.required_fields_intro)
                    .setPositiveButton(R.string.close, null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.confirm_modify_report_title)
                .setMessage(getString(R.string.confirm_modify_report, ref))
                .setPositiveButton(R.string.continue_upper, (dialog, which) -> mostrarDialogoOptimizacion())
                .setNegativeButton(R.string.cancel_upper, null)
                .show();
    }

    private void mostrarDialogoOptimizacion() {
        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.optimize_photos_title)
                .setMessage(R.string.optimize_photos_message)
                .setPositiveButton(R.string.yes_upper, (dialog, which) -> mostrarDialogoSobrescribir(true))
                .setNegativeButton(R.string.no_upper, (dialog, which) -> mostrarDialogoSobrescribir(false))
                .show();
    }

    private void mostrarDialogoSobrescribir(boolean optimizarFotos) {
        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.save_changes_title)
                .setMessage(R.string.overwrite_or_copy_message)
                .setPositiveButton(R.string.overwrite, (dialog, which) -> procesarModificacion(optimizarFotos, true))
                .setNegativeButton(R.string.copy, (dialog, which) -> procesarModificacion(optimizarFotos, false))
                .show();
    }

    private void procesarModificacion(boolean optimizarFotos, boolean sobrescribir) {
        setOverlayVisible(true);
        AppLogger.logInfo(this, "EditarInformeMercancia", "Modificando informe: " + inputReferencia.getText().toString() + " (sobrescribir=" + sobrescribir + ")");

        if (sobrescribir) {
            String rutaAnterior = informeOriginal.rutaPdf;

            informeOriginal.tipoInforme = "MERCANCIA";
            informeOriginal.tipo = "MERCANCÍA";
            informeOriginal.referencia = inputReferencia.getText().toString().trim();
            informeOriginal.siniestro = inputSiniestro.getText().toString().trim();
            informeOriginal.asegurado = inputAsegurado.getText().toString().trim();
            informeOriginal.requirente = inputRequirente.getText().toString().trim();
            informeOriginal.fechaInspeccion = inputFechaInspeccion.getText().toString().trim();
            informeOriginal.lugar = inputLugar.getText().toString().trim();
            informeOriginal.tecnico = inputTecnico.getText().toString().trim();
            informeOriginal.otrasPersonas = inputOtrasPersonas.getText().toString().trim();
            informeOriginal.bultosPeso = inputBultosPeso.getText().toString().trim();
            informeOriginal.valorMercancia = inputValorMercancia.getText().toString().trim();
            informeOriginal.medioTransporte = inputMedioTransporte.getText().toString().trim();
            informeOriginal.fechaCarga = inputFechaCarga.getText().toString().trim();
            informeOriginal.fechaDescarga = inputFechaDescarga.getText().toString().trim();
            informeOriginal.fechaSiniestroLugar = inputFechaSiniestroLugar.getText().toString().trim();
            informeOriginal.danos = inputDanos.getText().toString().trim();
            informeOriginal.causas = inputCausas.getText().toString().trim();
            informeOriginal.reserva = inputReserva.getText().toString().trim();
            informeOriginal.observaciones = inputObservaciones.getText().toString().trim();
            informeOriginal.docPendiente = inputDocP.getText().toString().trim();
            informeOriginal.actualizaciones = inputActualizaciones.getText().toString().trim();
            informeOriginal.fotosUris = serializarUris(imagenesAdjuntas);
            informeOriginal.timestamp = System.currentTimeMillis();

            File pdfGenerado = InformeMercanciaPdfGenerator.generarPdf(this, getExternalFilesDir(null), informeOriginal, imagenesAdjuntas, optimizarFotos);

            if (pdfGenerado == null) {
                AppLogger.logError(this, "EditarInformeMercancia", "Error al generar PDF", null);
                Toast.makeText(this, R.string.error_generating_pdf, Toast.LENGTH_SHORT).show();
                setOverlayVisible(false);
                return;
            }

            if (rutaAnterior != null && !rutaAnterior.equals(pdfGenerado.getAbsolutePath())) {
                File archivoAnterior = new File(rutaAnterior);
                if (archivoAnterior.exists()) {
                    archivoAnterior.delete();
                }
            }

            informeOriginal.rutaPdf = pdfGenerado.getAbsolutePath();
            AppDatabase.getInstance(this).informeDao().actualizar(informeOriginal);

            if (AppSettings.isAutoSendEmailEnabled(this)) {
                enviarCorreo(informeOriginal, pdfGenerado);
            } else {
                volverAListado();
            }
        } else {
            Informe nuevoInforme = new Informe();
            nuevoInforme.tipoInforme = "MERCANCIA";
            nuevoInforme.tipo = "MERCANCÍA";
            String refBase = inputReferencia.getText().toString().trim();
            nuevoInforme.referencia = refBase.endsWith(" COP") ? refBase : refBase + " COP";
            nuevoInforme.siniestro = inputSiniestro.getText().toString().trim();
            nuevoInforme.asegurado = inputAsegurado.getText().toString().trim();
            nuevoInforme.requirente = inputRequirente.getText().toString().trim();
            nuevoInforme.fechaInspeccion = inputFechaInspeccion.getText().toString().trim();
            nuevoInforme.lugar = inputLugar.getText().toString().trim();
            nuevoInforme.tecnico = inputTecnico.getText().toString().trim();
            nuevoInforme.otrasPersonas = inputOtrasPersonas.getText().toString().trim();
            nuevoInforme.bultosPeso = inputBultosPeso.getText().toString().trim();
            nuevoInforme.valorMercancia = inputValorMercancia.getText().toString().trim();
            nuevoInforme.medioTransporte = inputMedioTransporte.getText().toString().trim();
            nuevoInforme.fechaCarga = inputFechaCarga.getText().toString().trim();
            nuevoInforme.fechaDescarga = inputFechaDescarga.getText().toString().trim();
            nuevoInforme.fechaSiniestroLugar = inputFechaSiniestroLugar.getText().toString().trim();
            nuevoInforme.danos = inputDanos.getText().toString().trim();
            nuevoInforme.causas = inputCausas.getText().toString().trim();
            nuevoInforme.reserva = inputReserva.getText().toString().trim();
            nuevoInforme.observaciones = inputObservaciones.getText().toString().trim();
            nuevoInforme.docPendiente = inputDocP.getText().toString().trim();
            nuevoInforme.actualizaciones = inputActualizaciones.getText().toString().trim();
            nuevoInforme.fotosUris = serializarUris(imagenesAdjuntas);
            nuevoInforme.timestamp = System.currentTimeMillis();

            File pdfGenerado = InformeMercanciaPdfGenerator.generarPdf(this, getExternalFilesDir(null), nuevoInforme, imagenesAdjuntas, optimizarFotos);

            if (pdfGenerado == null) {
                AppLogger.logError(this, "EditarInformeMercancia", "Error al generar PDF", null);
                Toast.makeText(this, R.string.error_generating_pdf, Toast.LENGTH_SHORT).show();
                setOverlayVisible(false);
                return;
            }

            nuevoInforme.rutaPdf = pdfGenerado.getAbsolutePath();
            AppDatabase.getInstance(this).informeDao().insertar(nuevoInforme);

            if (AppSettings.isAutoSendEmailEnabled(this)) {
                enviarCorreo(nuevoInforme, pdfGenerado);
            } else {
                volverAListado();
            }
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
                    volverAListado();
                });
            } catch (Exception e) {
                AppLogger.logError(this, "EditarInformeMercancia", "Error al enviar email", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.error_sending_pdf, Toast.LENGTH_SHORT).show();
                    volverAListado();
                });
            }
        }).start();
    }

    private String construirCuerpoHtml(Informe inf) {
        String base64Logo = MailSender.getResourceToBase64(this, R.drawable.horizontal);
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family: Arial, sans-serif; color: #333;\">");
        html.append("<div style=\"text-align: center; margin-bottom: 20px;\">");
        html.append("<img src=\"data:image/jpeg;base64,")
            .append(base64Logo)
            .append("\" style=\"max-width: 100%; height: auto;\">")
            .append("</div>");
        html.append("<h2 style=\"color: #004a99; border-bottom: 2px solid #004a99; padding-bottom: 10px;\">Detalles del Informe (Mercancía)</h2>")
            .append("<table style=\"width: 100%; border-collapse: collapse;\">");
        appendRow(html, "Referencia", inf.referencia);
        appendRow(html, "Siniestro", inf.siniestro);
        appendRow(html, "Asegurado", inf.asegurado);
        appendRow(html, "Lugar", inf.lugar);
        html.append("</table>")
            .append("<p style=\"margin-top: 20px;\">Se adjunta el informe generado con GenInfor v4.1.</p>")
            .append("<div style=\"margin-top: 30px; padding-top: 10px; border-top: 1px solid #ccc; font-size: 12px; color: #777;\">")
            .append("<p>Desarrollada por <strong>Santiago Pérez</strong></p>")
            .append("</div></body></html>");
        return html.toString();
    }

    private void appendRow(StringBuilder html, String label, String value) {
        html.append("<tr><td style=\"padding: 8px; font-weight: bold; width: 40%; border-bottom: 1px solid #eee;\">").append(label).append(":</td>")
            .append("<td style=\"padding: 8px; border-bottom: 1px solid #eee;\">").append(value != null ? value : "").append("</td></tr>");
    }

    private void volverAListado() {
        setOverlayVisible(false);
        Intent intent = new Intent(this, ListaInformesActivity.class);
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

    private Uri copiarImagenAApp(Uri origen) {
        if (origen == null) return null;
        try {
            File almacenamientoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (almacenamientoDir == null) return null;
            File destino = File.createTempFile("galeria_", ".jpg", almacenamientoDir);
            try (InputStream in = getContentResolver().openInputStream(origen);
                 OutputStream out = new FileOutputStream(destino)) {
                if (in == null) return null;
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            return Uri.fromFile(destino);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && uriFotoActual != null) {
                if (imagenesAdjuntas.size() < 4) {
                    imagenesAdjuntas.add(uriFotoActual);
                    Toast.makeText(this, R.string.photo_taken, Toast.LENGTH_SHORT).show();
                    actualizarListaImagenes();
                } else {
                    Toast.makeText(this, R.string.max_4_images, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                int slotsDisponibles = 4 - imagenesAdjuntas.size();
                if (slotsDisponibles <= 0) {
                    Toast.makeText(this, R.string.max_4_images, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    int añadidas = 0;
                    for (int i = 0; i < count && añadidas < slotsDisponibles; i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        Uri copia = copiarImagenAApp(uri);
                        imagenesAdjuntas.add(copia != null ? copia : uri);
                        añadidas++;
                    }
                    Toast.makeText(this, getString(R.string.photos_selected_count, añadidas), Toast.LENGTH_SHORT).show();
                } else if (data.getData() != null) {
                    Uri imagenGaleria = data.getData();
                    Uri copia = copiarImagenAApp(imagenGaleria);
                    imagenesAdjuntas.add(copia != null ? copia : imagenGaleria);
                    Toast.makeText(this, R.string.photo_selected, Toast.LENGTH_SHORT).show();
                }
                actualizarListaImagenes();
            }
        }
    }

    private void actualizarListaImagenes() {
        layoutImagenesAdjuntas.removeAllViews();

        if (imagenesAdjuntas.isEmpty()) {
            textAdjuntos.setVisibility(View.GONE);
            return;
        }

        textAdjuntos.setVisibility(View.VISIBLE);

        for (int i = 0; i < imagenesAdjuntas.size(); i++) {
            Uri uri = imagenesAdjuntas.get(i);

            LinearLayout contenedor = new LinearLayout(this);
            contenedor.setOrientation(LinearLayout.HORIZONTAL);
            contenedor.setPadding(0, 8, 0, 8);
            contenedor.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            contenedor.setGravity(Gravity.CENTER_VERTICAL);

            ImageView miniatura = new ImageView(this);
            miniatura.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            miniatura.setScaleType(ImageView.ScaleType.CENTER_CROP);
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                if (inputStream != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    miniatura.setImageBitmap(bitmap);
                }
            } catch (SecurityException se) {
                imagenesAdjuntas.remove(uri);
                Toast.makeText(this, R.string.photo_access_lost, Toast.LENGTH_SHORT).show();
                actualizarListaImagenes();
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }

            TextView nombre = new TextView(this);
            nombre.setText(obtenerNombreArchivoDesdeUri(uri));
            nombre.setTextSize(14);
            nombre.setPadding(16, 0, 0, 0);
            nombre.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            ImageView btnEliminar = new ImageView(this);
            btnEliminar.setImageResource(android.R.drawable.ic_menu_delete);
            btnEliminar.setPadding(16, 0, 16, 0);
            btnEliminar.setOnClickListener(v -> {
                imagenesAdjuntas.remove(uri);
                actualizarListaImagenes();
            });

            contenedor.addView(miniatura);
            contenedor.addView(nombre);
            contenedor.addView(btnEliminar);

            layoutImagenesAdjuntas.addView(contenedor);
        }
    }

    private String obtenerNombreArchivoDesdeUri(Uri uri) {
        String nombre = getString(R.string.image_label);
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    nombre = cursor.getString(index);
                }
                cursor.close();
            }
        } else if (uri.getScheme() != null && uri.getScheme().equals("file")) {
            File archivo = new File(uri.getPath());
            nombre = archivo.getName();
        }
        return nombre;
    }

    private String serializarUris(List<Uri> uris) {
        JSONArray array = new JSONArray();
        for (Uri uri : uris) {
            array.put(uri.toString());
        }
        return array.toString();
    }

    private List<Uri> deserializarUris(String json) {
        List<Uri> result = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return result;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                result.add(Uri.parse(array.getString(i)));
            }
        } catch (JSONException ignored) {}
        return result;
    }
}
