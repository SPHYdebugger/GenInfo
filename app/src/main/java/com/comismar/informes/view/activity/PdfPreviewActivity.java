package com.comismar.informes.view.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.comismar.informes.R;
import com.comismar.informes.view.utils.AppLogger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfPreviewActivity extends AppCompatActivity {

    public static final String EXTRA_RUTA_PDF = "rutaPdf";
    public static final String EXTRA_INFORME_ID = "informeId";

    private ImageView pdfImage;
    private File pdfFile;
    private int informeId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_preview);

        findViewById(R.id.btnBackPreview).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.btnAbrirPdf).setOnClickListener(v -> abrirEnVisorExterno());
        findViewById(R.id.btnEditarPdf).setOnClickListener(v -> abrirEditor());

        pdfImage = findViewById(R.id.pdfImage);

        String rutaPdf = getIntent().getStringExtra(EXTRA_RUTA_PDF);
        informeId = getIntent().getIntExtra(EXTRA_INFORME_ID, -1);
        if (rutaPdf == null || rutaPdf.trim().isEmpty()) {
            AppLogger.logError(this, "PdfPreviewActivity", "No se pudieron ver los informes (ruta vacía)", null);
            Toast.makeText(this, R.string.pdf_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pdfFile = new File(rutaPdf);
        if (!pdfFile.exists()) {
            AppLogger.logError(this, "PdfPreviewActivity", "No se pudieron ver los informes (archivo inexistente)", null);
            Toast.makeText(this, R.string.pdf_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        new Thread(() -> {
            Bitmap merged = renderizarPdfComoImagen(pdfFile);
            runOnUiThread(() -> {
                if (merged != null) {
                    pdfImage.setImageBitmap(merged);
                } else {
                    AppLogger.logError(this, "PdfPreviewActivity", "No se pudieron ver los informes (render null)", null);
                    Toast.makeText(this, R.string.error_open_pdf, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }).start();
    }

    private void abrirEditor() {
        if (informeId <= 0) {
            AppLogger.logError(this, "PdfPreviewActivity", "No se pudo abrir edición desde preview (id inválido)", null);
            Toast.makeText(this, R.string.report_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        
        com.comismar.informes.model.Informe informe = com.comismar.informes.model.AppDatabase.getInstance(this).informeDao().obtenerPorId(informeId);
        if (informe == null) {
            Toast.makeText(this, R.string.report_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        android.content.Intent intent;
        if ("MERCANCIA".equalsIgnoreCase(informe.tipoInforme)) {
            intent = new android.content.Intent(this, EditarInformeMercanciaActivity.class);
            intent.putExtra(EditarInformeMercanciaActivity.EXTRA_INFORME_ID, informeId);
        } else {
            intent = new android.content.Intent(this, EditarInformeActivity.class);
            intent.putExtra(EditarInformeActivity.EXTRA_INFORME_ID, informeId);
        }
        startActivity(intent);
    }

    private void abrirEnVisorExterno() {
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(this, R.string.pdf_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        android.net.Uri uri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                pdfFile);
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            AppLogger.logError(this, "PdfPreviewActivity", "No se pudo abrir el PDF en visor externo", e);
            Toast.makeText(this, R.string.no_pdf_viewer, Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap renderizarPdfComoImagen(File file) {
        ParcelFileDescriptor pfd = null;
        PdfRenderer renderer = null;
        try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            renderer = new PdfRenderer(pfd);

            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int targetWidth = metrics.widthPixels;

            int pageCount = renderer.getPageCount();
            if (pageCount == 0) {
                return null;
            }

            List<Bitmap> pages = new ArrayList<>();
            int totalHeight = 0;

            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                int targetHeight = (int) ((float) targetWidth * page.getHeight() / page.getWidth());
                Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                pages.add(bitmap);
                totalHeight += targetHeight;
            }

            int extraRightPx = 20;
            int extraBottomPx = 20;
            int cropLeftPx = 15;
            int cropTopPx = 15;
            int mergedWidth = Math.max(1, targetWidth + extraRightPx - cropLeftPx);
            int mergedHeight = Math.max(1, totalHeight + extraBottomPx - cropTopPx);
            Bitmap merged = Bitmap.createBitmap(mergedWidth, mergedHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(merged);
            canvas.drawColor(Color.WHITE);
            int y = -cropTopPx;
            for (Bitmap pageBitmap : pages) {
                canvas.drawBitmap(pageBitmap, -cropLeftPx, y, null);
                y += pageBitmap.getHeight();
                pageBitmap.recycle();
            }
            return merged;
        } catch (IOException e) {
            return null;
        } finally {
            if (renderer != null) {
                renderer.close();
            }
            if (pfd != null) {
                try {
                    pfd.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
