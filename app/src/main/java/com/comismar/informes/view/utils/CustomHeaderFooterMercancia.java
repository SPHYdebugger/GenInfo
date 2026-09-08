package com.comismar.informes.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.comismar.informes.R;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomHeaderFooterMercancia extends PdfPageEventHelper {

    private final Context context;
    private final String referencia;
    private final String siniestro;
    private final Font fontSmall = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.GRAY);
    private final Font fontHeaderLabel = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.WHITE);
    private final Font fontHeaderValue = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK);

    public CustomHeaderFooterMercancia(Context context, String referencia, String siniestro) {
        this.context = context;
        this.referencia = referencia;
        this.siniestro = siniestro;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();

        // 1. CAJETÍN IZQUIERDA (Nº Expte y Nº Siniestro)
        PdfPTable cajetin = new PdfPTable(2);
        try {
            cajetin.setTotalWidth(150);
            cajetin.setWidths(new float[]{40, 60});

            addCajetinFila(cajetin, "N.º Expte.:", referencia);
            addCajetinFila(cajetin, "N.º Siniestro:", siniestro);

            cajetin.writeSelectedRows(0, -1, 36, 810, cb);
        } catch (Exception ignored) {}

        // 2. LOGO DERECHA
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.comimarcargo);
            java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image logo = Image.getInstance(stream.toByteArray());
            logo.scaleToFit(120, 50);
            logo.setAbsolutePosition(440, 775);
            cb.addImage(logo);
        } catch (Exception ignored) {}

        // 3. TEXTO VERTICAL IZQUIERDA
        String fechaActual = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault()).format(new Date());
        String verticalText = String.format("Rev.: %s - Este documento es propiedad de COMISMAR, S.A. y de uso estrictamente confidencial, no podrá ser utilizado ni distribuido sin autorización expresa de la dirección de la empresa.", fechaActual);
        
        cb.saveState();
        Phrase phraseVertical = new Phrase(verticalText, new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.GRAY));
        // Centrado verticalmente (421 es la mitad de A4) y a 25 puntos del borde izquierdo
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, phraseVertical, 25, 421, 90);
        cb.restoreState();

        // 4. NÚMERO DE PÁGINA (ABAJO)
        String pageText = "Página " + writer.getPageNumber();
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, new Phrase(pageText, fontSmall), 300, 20, 0);
    }

    private void addCajetinFila(PdfPTable table, String label, String value) {
        PdfPCell cellLabel = new PdfPCell(new Phrase(label, fontHeaderLabel));
        cellLabel.setBackgroundColor(new BaseColor(13, 71, 115));
        cellLabel.setPadding(3);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Phrase(value != null ? value : "", fontHeaderValue));
        cellValue.setPadding(3);
        table.addCell(cellValue);
    }
}
