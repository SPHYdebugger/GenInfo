package com.comismar.informes.view.activity;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.comismar.informes.R;
import com.comismar.informes.model.AppDatabase;
import com.comismar.informes.model.Informe;
import com.comismar.informes.view.adapter.MailSender;
import com.comismar.informes.view.utils.CustomFooter;
import com.comismar.informes.view.utils.CustomHeader;
import com.comismar.informes.view.utils.InformePdfGenerator;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GenerarInformeActivity extends Activity {

    private EditText inputReferencia, inputLugar;
    private Button btnAdjuntarFotos, btnGenerarInforme, btnBack;


    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private LinearLayout layoutImagenesAdjuntas;
    private TextView textAdjuntos;

    private CheckBox checkboxEnviarPdf;

    private static final int REQUEST_CAMERA_PERMISSION = 100;




    private List<Uri> imagenesAdjuntas = new ArrayList<>();
    private Uri uriFotoActual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_informe);


        inputReferencia = findViewById(R.id.inputReferencia);

        inputLugar = findViewById(R.id.inputLugar);
        btnAdjuntarFotos = findViewById(R.id.btnAdjuntarFotos);
        btnGenerarInforme = findViewById(R.id.btnGenerarInforme);
        btnBack = findViewById(R.id.btnBack);
        btnAdjuntarFotos.setOnClickListener(v -> mostrarDialogoFuenteImagen());
        layoutImagenesAdjuntas = findViewById(R.id.layoutImagenesAdjuntas);
        textAdjuntos = findViewById(R.id.textAdjuntos);
        checkboxEnviarPdf = findViewById(R.id.checkboxEnviarPdf);




        btnAdjuntarFotos.setOnClickListener(v -> {
            btnAdjuntarFotos.setOnClickListener(view -> mostrarDialogoFuenteImagen());
        });

        btnGenerarInforme.setOnClickListener(v -> {
            String referencia = inputReferencia.getText().toString().trim();
            String siniestro = ((EditText) findViewById(R.id.inputSiniestro)).getText().toString().trim();
            String lugar = inputLugar.getText().toString().trim();

            // Validar campos obligatorios
            if (referencia.isEmpty() || siniestro.isEmpty() || lugar.isEmpty()) {
                StringBuilder mensaje = new StringBuilder("Debes completar los siguientes campos:\n");
                if (referencia.isEmpty()) mensaje.append("- REFERENCIA\n");
                if (siniestro.isEmpty()) mensaje.append("- SINIESTRO\n");
                if (lugar.isEmpty()) mensaje.append("- LUGAR\n");

                new AlertDialog.Builder(this)
                        .setTitle("Campos obligatorios")
                        .setMessage(mensaje.toString())
                        .setPositiveButton("Cerrar", null)
                        .show();
                return; // ⛔ No continuar
            }

            // Si todo está bien, mostrar confirmación
            int numeroFotos = imagenesAdjuntas.size();
            String mensaje = "¿Estás seguro de crear el informe " + referencia + "?\n" +
                    (numeroFotos > 0 ? "Con " + numeroFotos + " foto(s) adjunta(s)." : "Sin fotos adjuntas.");

            new AlertDialog.Builder(this)
                    .setTitle("Confirmar creación de informe")
                    .setMessage(mensaje)
                    .setPositiveButton("CREAR", (dialog, which) -> generarInforme())
                    .setNegativeButton("CANCELAR", null)
                    .show();
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(GenerarInformeActivity.this, MainActivity.class);
            startActivity(intent);
        });

    }

    private void generarInforme() {
        Toast.makeText(this, "Generando PDF...", Toast.LENGTH_SHORT).show();

        String siniestro = ((EditText) findViewById(R.id.inputSiniestro)).getText().toString();
        String requirente = ((EditText) findViewById(R.id.inputRequirente)).getText().toString();
        String tecnico = ((EditText) findViewById(R.id.inputTecnico)).getText().toString();
        String nombreBarco = ((EditText) findViewById(R.id.inputNombreBarco)).getText().toString();
        String matricula = ((EditText) findViewById(R.id.inputMatricula)).getText().toString();
        String daños = ((EditText) findViewById(R.id.inputDaños)).getText().toString();
        String docP = ((EditText) findViewById(R.id.inputDocP)).getText().toString();
        String causas = ((EditText) findViewById(R.id.inputCausas)).getText().toString();
        String reserva = ((EditText) findViewById(R.id.inputReserva)).getText().toString();
        String observaciones = ((EditText) findViewById(R.id.inputObservaciones)).getText().toString();
        String referencia = inputReferencia.getText().toString().trim();
        String lugar = inputLugar.getText().toString().trim();
        boolean enviarPorCorreo = checkboxEnviarPdf.isChecked();

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
                daños,
                causas,
                reserva,
                observaciones,
                docP,
                imagenesAdjuntas
        );

        // Guardar en la base de datos
        Informe nuevoInforme = new Informe();
        nuevoInforme.tipo = nombreBarco;
        nuevoInforme.referencia = referencia;
        nuevoInforme.trabajo = siniestro;
        nuevoInforme.descripcion = daños;
        nuevoInforme.timestamp = System.currentTimeMillis();
        nuevoInforme.rutaPdf = pdfGenerado.getAbsolutePath();

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        db.informeDao().insertar(nuevoInforme);
        Toast.makeText(this, "Informe añadido a la lista", Toast.LENGTH_SHORT).show();

        if (enviarPorCorreo) {
            new Thread(() -> {
                try {
                    MailSender sender = new MailSender("infogenpdf@gmail.com", "lwoi wagz zywo udae");
                    sender.enviarCorreo(
                            "Informe de intervención: " + referencia,
                            "Se adjunta el informe generado.",
                            "infogenpdf@gmail.com",
                            "infogenpdf@gmail.com",
                            pdfGenerado
                    );
                    runOnUiThread(() -> {
                        Toast.makeText(this, "PDF enviado por email", Toast.LENGTH_SHORT).show();
                        volverAlInicio();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error al enviar PDF", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        } else {
            volverAlInicio();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && uriFotoActual != null) {
                imagenesAdjuntas.add(uriFotoActual);
                Toast.makeText(this, "Foto tomada", Toast.LENGTH_SHORT).show();
                actualizarListaImagenes();
            } else if (requestCode == REQUEST_IMAGE_GALLERY && data != null) {
                Uri imagenGaleria = data.getData();
                imagenesAdjuntas.add(imagenGaleria);
                Toast.makeText(this, "Foto seleccionada", Toast.LENGTH_SHORT).show();
                actualizarListaImagenes();
            }
        }
    }


    private void mostrarDialogoFuenteImagen() {
        String[] opciones = {"Tomar Foto", "Elegir de Galería"};
        new AlertDialog.Builder(this)
                .setTitle("Adjuntar Foto")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                abrirCamara();
                            } else {
                                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                            }
                        } else {
                            // En versiones anteriores a Android 6, el permiso se concede automáticamente al instalar
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }


    private File crearArchivoImagen() throws IOException {
        String nombreArchivo = "foto_" + System.currentTimeMillis();
        File almacenamientoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreArchivo, ".jpg", almacenamientoDir);
        return imagen;
    }

    private void actualizarListaImagenes() {
        layoutImagenesAdjuntas.removeAllViews();

        if (imagenesAdjuntas.isEmpty()) {
            textAdjuntos.setVisibility(View.GONE);
            return;
        }

        textAdjuntos.setVisibility(View.VISIBLE);

        for (Uri uri : imagenesAdjuntas) {
            String nombreArchivo = obtenerNombreArchivoDesdeUri(uri);
            TextView textView = new TextView(this);
            textView.setText("- " + nombreArchivo);
            layoutImagenesAdjuntas.addView(textView);
        }
    }

    private String obtenerNombreArchivoDesdeUri(Uri uri) {
        String nombre = "Imagen";
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

    private void volverAlInicio() {
        Intent intent = new Intent(GenerarInformeActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }



}
