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
import com.comismar.informes.view.utils.InformePdfGenerator;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditarInformeActivity extends Activity {

    public static final String EXTRA_INFORME_ID = "informeId";

    private EditText inputReferencia, inputLugar;
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
        setContentView(R.layout.activity_generar_informe);

        inputReferencia = findViewById(R.id.inputReferencia);
        inputLugar = findViewById(R.id.inputLugar);
        btnAdjuntarFotos = findViewById(R.id.btnAdjuntarFotos);
        btnGenerarInforme = findViewById(R.id.btnGenerarInforme);
        btnBack = findViewById(R.id.btnBack);
        layoutImagenesAdjuntas = findViewById(R.id.layoutImagenesAdjuntas);
        textAdjuntos = findViewById(R.id.textAdjuntos);
        overlayBloqueo = findViewById(R.id.overlayBloqueo);

        btnGenerarInforme.setText(R.string.modify_report);

        int informeId = getIntent().getIntExtra(EXTRA_INFORME_ID, -1);
        informeOriginal = AppDatabase.getInstance(getApplicationContext()).informeDao().obtenerPorId(informeId);
        if (informeOriginal == null) {
            AppLogger.logError(this, "EditarInformeActivity", "No se pudo cargar informe para editar", null);
            Toast.makeText(this, R.string.report_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        precargarDatos(informeOriginal);

        btnAdjuntarFotos.setOnClickListener(v -> mostrarDialogoFuenteImagen());
        btnGenerarInforme.setOnClickListener(v -> onModificarInforme());
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EditarInformeActivity.this, ListaInformesActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void precargarDatos(Informe informe) {
        ((EditText) findViewById(R.id.inputSiniestro)).setText(informe.siniestro);
        ((EditText) findViewById(R.id.inputRequirente)).setText(informe.requirente);
        ((EditText) findViewById(R.id.inputTecnico)).setText(informe.tecnico);
        ((EditText) findViewById(R.id.inputNombreBarco)).setText(informe.nombreBarco);
        ((EditText) findViewById(R.id.inputMatricula)).setText(informe.matricula);
        ((EditText) findViewById(R.id.inputDaños)).setText(informe.danos);
        ((EditText) findViewById(R.id.inputCausas)).setText(informe.causas);
        ((EditText) findViewById(R.id.inputReserva)).setText(informe.reserva);
        ((EditText) findViewById(R.id.inputObservaciones)).setText(informe.observaciones);
        ((EditText) findViewById(R.id.inputDocP)).setText(informe.docPendiente);
        inputReferencia.setText(informe.referencia);
        inputLugar.setText(informe.lugar);

        imagenesAdjuntas = deserializarUris(informe.fotosUris);
        actualizarListaImagenes();
    }

    private void onModificarInforme() {
        String referencia = inputReferencia.getText().toString().trim();
        String siniestro = ((EditText) findViewById(R.id.inputSiniestro)).getText().toString().trim();
        String lugar = inputLugar.getText().toString().trim();

        if (referencia.isEmpty() || siniestro.isEmpty() || lugar.isEmpty()) {
            StringBuilder mensaje = new StringBuilder(getString(R.string.required_fields_intro));
            if (referencia.isEmpty())
                mensaje.append(getString(R.string.required_reference));
            if (siniestro.isEmpty())
                mensaje.append(getString(R.string.required_incident));
            if (lugar.isEmpty())
                mensaje.append(getString(R.string.required_place));

            new AlertDialog.Builder(this, R.style.AppDialogTheme)
                    .setTitle(R.string.required_fields_title)
                    .setMessage(mensaje.toString())
                    .setPositiveButton(R.string.close, null)
                    .show();
            return;
        }

        int numeroFotos = imagenesAdjuntas.size();
        String mensaje = getString(R.string.confirm_modify_report, referencia) + "\n" +
                (numeroFotos > 0 ? getString(R.string.with_attached_photos, numeroFotos) : getString(R.string.without_photos));

        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.confirm_modify_report_title)
                .setMessage(mensaje)
                .setPositiveButton(R.string.continue_upper, (dialog, which) -> mostrarDialogoOptimizacion())
                .setNegativeButton(R.string.cancel_upper, null)
                .show();
    }

    private void mostrarDialogoOptimizacion() {
        String mensaje = getString(R.string.optimize_photos_message);
        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.optimize_photos_title)
                .setMessage(mensaje)
                .setPositiveButton(R.string.yes_upper, (dialog, which) -> mostrarDialogoSobrescribir(true))
                .setNegativeButton(R.string.no_upper, (dialog, which) -> mostrarDialogoSobrescribir(false))
                .show();
    }

    private void mostrarDialogoSobrescribir(boolean optimizarFotos) {
        String mensaje = getString(R.string.overwrite_or_copy_message);
        new AlertDialog.Builder(this, R.style.AppDialogTheme)
                .setTitle(R.string.save_changes_title)
                .setMessage(mensaje)
                .setPositiveButton(R.string.overwrite, (dialog, which) -> procesarModificacion(optimizarFotos, true))
                .setNegativeButton(R.string.copy, (dialog, which) -> procesarModificacion(optimizarFotos, false))
                .show();
    }

    private void procesarModificacion(boolean optimizarFotos, boolean sobrescribir) {
        setOverlayVisible(true);
        Toast.makeText(this, R.string.generating_pdf, Toast.LENGTH_SHORT).show();

        String siniestro = ((EditText) findViewById(R.id.inputSiniestro)).getText().toString();
        String requirente = ((EditText) findViewById(R.id.inputRequirente)).getText().toString();
        String tecnico = ((EditText) findViewById(R.id.inputTecnico)).getText().toString();
        String nombreBarco = ((EditText) findViewById(R.id.inputNombreBarco)).getText().toString();
        String matricula = ((EditText) findViewById(R.id.inputMatricula)).getText().toString();
        String danos = ((EditText) findViewById(R.id.inputDaños)).getText().toString();
        String docP = ((EditText) findViewById(R.id.inputDocP)).getText().toString();
        String causas = ((EditText) findViewById(R.id.inputCausas)).getText().toString();
        String reserva = ((EditText) findViewById(R.id.inputReserva)).getText().toString();
        String observaciones = ((EditText) findViewById(R.id.inputObservaciones)).getText().toString();
        String referencia = inputReferencia.getText().toString().trim();
        String lugar = inputLugar.getText().toString().trim();
        boolean enviarPorCorreo = AppSettings.isAutoSendEmailEnabled(this);

        File pdfGenerado = InformePdfGenerator.generarPdfDesdeDatos(
                this,
                getExternalFilesDir(null),
                referencia,
                siniestro,
                requirente,
                lugar,
                tecnico,
                nombreBarco,
                matricula,
                danos,
                causas,
                reserva,
                observaciones,
                docP,
                imagenesAdjuntas,
                optimizarFotos);

        if (pdfGenerado == null) {
            AppLogger.logError(this, "EditarInformeActivity", "No se pudo guardar un informe (pdf null)", null);
            Toast.makeText(this, R.string.error_generating_pdf, Toast.LENGTH_SHORT).show();
            setOverlayVisible(false);
            return;
        }

        String mensajeResultado;
        String sufijoReferencia = sobrescribir ? " MOD" : " COP";
        File destinoPdf = construirPdfDestino(pdfGenerado, sobrescribir);
        if (destinoPdf != null && !destinoPdf.equals(pdfGenerado)) {
            if (destinoPdf.exists()) {
                destinoPdf.delete();
            }
            boolean renombrado = pdfGenerado.renameTo(destinoPdf);
            if (renombrado) {
                pdfGenerado = destinoPdf;
            }
        }

        if (sobrescribir) {
            if (informeOriginal.rutaPdf != null) {
                File archivo = new File(informeOriginal.rutaPdf);
                if (archivo.exists()) {
                    archivo.delete();
                }
            }
            informeOriginal.tipo = nombreBarco;
            informeOriginal.referencia = aplicarSufijoCorto(referencia, sufijoReferencia);
            informeOriginal.trabajo = siniestro;
            informeOriginal.descripcion = danos;
            informeOriginal.timestamp = System.currentTimeMillis();
            informeOriginal.rutaPdf = pdfGenerado.getAbsolutePath();
            informeOriginal.siniestro = siniestro;
            informeOriginal.requirente = requirente;
            informeOriginal.lugar = lugar;
            informeOriginal.tecnico = tecnico;
            informeOriginal.nombreBarco = nombreBarco;
            informeOriginal.matricula = matricula;
            informeOriginal.danos = danos;
            informeOriginal.causas = causas;
            informeOriginal.reserva = reserva;
            informeOriginal.observaciones = observaciones;
            informeOriginal.docPendiente = docP;
            informeOriginal.fotosUris = serializarUris(imagenesAdjuntas);

            try {
                AppDatabase.getInstance(getApplicationContext()).informeDao().actualizar(informeOriginal);
            } catch (Exception e) {
                AppLogger.logError(this, "EditarInformeActivity", "No se pudo guardar un informe (update DB)", e);
                Toast.makeText(this, R.string.error_saving_report, Toast.LENGTH_SHORT).show();
                setOverlayVisible(false);
                return;
            }
            mensajeResultado = getString(R.string.report_modified);
        } else {
            Informe nuevoInforme = new Informe();
            nuevoInforme.tipo = nombreBarco;
            nuevoInforme.referencia = aplicarSufijoCorto(referencia, sufijoReferencia);
            nuevoInforme.trabajo = siniestro;
            nuevoInforme.descripcion = danos;
            nuevoInforme.timestamp = System.currentTimeMillis();
            nuevoInforme.rutaPdf = pdfGenerado.getAbsolutePath();
            nuevoInforme.siniestro = siniestro;
            nuevoInforme.requirente = requirente;
            nuevoInforme.lugar = lugar;
            nuevoInforme.tecnico = tecnico;
            nuevoInforme.nombreBarco = nombreBarco;
            nuevoInforme.matricula = matricula;
            nuevoInforme.danos = danos;
            nuevoInforme.causas = causas;
            nuevoInforme.reserva = reserva;
            nuevoInforme.observaciones = observaciones;
            nuevoInforme.docPendiente = docP;
            nuevoInforme.fotosUris = serializarUris(imagenesAdjuntas);

            try {
                AppDatabase.getInstance(getApplicationContext()).informeDao().insertar(nuevoInforme);
            } catch (Exception e) {
                AppLogger.logError(this, "EditarInformeActivity", "No se pudo guardar un informe (insert DB copia)", e);
                Toast.makeText(this, R.string.error_saving_report, Toast.LENGTH_SHORT).show();
                setOverlayVisible(false);
                return;
            }
            mensajeResultado = getString(R.string.copy_created);
        }

        final File pdfAdjunto = pdfGenerado;
        if (enviarPorCorreo) {
            final String destinatario = AppSettings.getRecipientEmail(this);
            new Thread(() -> {
                try {
                    MailSender sender = new MailSender("infogenpdf@gmail.com", "lwoi wagz zywo udae");
                    sender.enviarCorreo(
                            getString(R.string.email_subject_report, referencia),
                            getString(R.string.email_body_report),
                            "infogenpdf@gmail.com",
                            destinatario,
                            pdfAdjunto);
                    runOnUiThread(() -> mostrarToastsEnCadena(
                            new String[] { mensajeResultado, getString(R.string.pdf_sent_email) },
                            () -> {
                                setOverlayVisible(false);
                                volverAListado();
                            }));
                } catch (Exception e) {
                    AppLogger.logError(this, "EditarInformeActivity", "No se pudo enviar informe por mail", e);
                    runOnUiThread(() -> mostrarToastsEnCadena(
                            new String[] { mensajeResultado, getString(R.string.error_sending_pdf) },
                            () -> {
                                setOverlayVisible(false);
                                volverAListado();
                            }));
                }
            }).start();
        } else {
            mostrarToastsEnCadena(
                    new String[] { mensajeResultado },
                    () -> {
                        setOverlayVisible(false);
                        volverAListado();
                    });
        }
    }

    private String aplicarSufijoCorto(String referencia, String sufijo) {
        if (referencia == null) return "";
        if (referencia.endsWith(sufijo)) return referencia;
        return referencia + sufijo;
    }

    private File construirPdfDestino(File pdfGenerado, boolean sobrescribir) {
        if (pdfGenerado == null) return null;
        String baseOriginal = obtenerNombreBase(informeOriginal != null ? informeOriginal.rutaPdf : null);
        if (baseOriginal == null || baseOriginal.isEmpty()) {
            baseOriginal = "informe_" + System.currentTimeMillis();
        }
        String sufijo = sobrescribir ? "_MOD" : "_COP";
        return new File(pdfGenerado.getParentFile(), baseOriginal + sufijo + ".pdf");
    }

    private String obtenerNombreBase(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) return null;
        File f = new File(ruta);
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && uriFotoActual != null) {
                imagenesAdjuntas.add(uriFotoActual);
                añadirImagenAGaleria(uriFotoActual); // Copiar a la galería pública
                Toast.makeText(this, R.string.photo_taken, Toast.LENGTH_SHORT).show();
                actualizarListaImagenes();
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
                            if (checkSelfPermission(
                                    android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                abrirCamara();
                            } else {
                                requestPermissions(new String[] { android.Manifest.permission.CAMERA },
                                        REQUEST_CAMERA_PERMISSION);
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
            File fotoArchivo = null;
            try {
                fotoArchivo = crearArchivoImagen();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (fotoArchivo != null) {
                uriFotoActual = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", fotoArchivo);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriFotoActual);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void añadirImagenAGaleria(Uri uriPrivada) {
        if (uriPrivada == null) return;
        try {
            ContentValues values = new ContentValues();
            String fileName = "GenInfor_" + System.currentTimeMillis() + ".jpg";
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GenInfor");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            }

            Uri externalUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (externalUri != null) {
                try (InputStream in = getContentResolver().openInputStream(uriPrivada);
                     OutputStream out = getContentResolver().openOutputStream(externalUri)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    getContentResolver().update(externalUri, values, null, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        Intent chooser = Intent.createChooser(intent, getString(R.string.select_images));
        startActivityForResult(chooser, REQUEST_IMAGE_GALLERY);
    }

    private File crearArchivoImagen() throws IOException {
        String nombreArchivo = "foto_" + System.currentTimeMillis();
        File almacenamientoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(nombreArchivo, ".jpg", almacenamientoDir);
    }

    private Uri copiarImagenAApp(Uri origen) {
        if (origen == null) return null;
        try {
            File almacenamientoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (almacenamientoDir == null) return null;
            File destino = File.createTempFile("galeria_", ".jpg", almacenamientoDir);
            try (java.io.InputStream in = getContentResolver().openInputStream(origen);
                 java.io.OutputStream out = new java.io.FileOutputStream(destino)) {
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
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                miniatura.setImageBitmap(bitmap);
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
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    nombre = cursor.getString(index);
                }
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            File archivo = new File(uri.getPath());
            nombre = archivo.getName();
        }
        return nombre;
    }

    private void volverAListado() {
        Intent intent = new Intent(EditarInformeActivity.this, ListaInformesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarToastsEnCadena(String[] mensajes, Runnable onFinish) {
        if (mensajes == null || mensajes.length == 0) {
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }
        mostrarToastCadena(0, mensajes, onFinish);
    }

    private void mostrarToastCadena(int index, String[] mensajes, Runnable onFinish) {
        if (index >= mensajes.length) {
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }
        Toast.makeText(this, mensajes[index], Toast.LENGTH_SHORT).show();
        uiHandler.postDelayed(() -> mostrarToastCadena(index + 1, mensajes, onFinish), TOAST_SHORT_MS);
    }

    private void setOverlayVisible(boolean visible) {
        if (overlayBloqueo != null) {
            overlayBloqueo.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if (visible) {
            ocultarTeclado();
        }
    }

    private void ocultarTeclado() {
        View focus = getCurrentFocus();
        if (focus == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
        focus.clearFocus();
    }

    private String serializarUris(List<Uri> uris) {
        JSONArray array = new JSONArray();
        if (uris != null) {
            for (Uri uri : uris) {
                array.put(uri.toString());
            }
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
        } catch (JSONException ignored) {
        }
        return result;
    }
}
