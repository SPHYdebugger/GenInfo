package com.comismar.informes.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.comismar.informes.R;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CustomHeader {

    private final Context context;
    private final String lugar;

    public CustomHeader(Context context, String lugar) {
        this.context = context;
        this.lugar = lugar;
    }

    public PdfPTable getTable() throws Exception {
        // LOGO
        Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.grupo_comismar_logo);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        Image logo = Image.getInstance(stream.toByteArray());
        logo.scaleAbsoluteHeight(70);
        logo.scaleAbsoluteWidth(logo.getWidth() * (60f / logo.getHeight()));
        logo.setAlignment(Image.ALIGN_LEFT);

        // TEXTO
        Paragraph titulo = new Paragraph("Informe de inspección rutinaria\n",
                new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
        String fecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        Paragraph info = new Paragraph("Fecha: " + fecha + "    Lugar: " + lugar + "\n\n",
                new Font(Font.FontFamily.HELVETICA, 12));

        // TABLA INTERNA
        PdfPTable innerTable = new PdfPTable(2);
        innerTable.setWidthPercentage(100);
        innerTable.setWidths(new int[]{1, 5});

        PdfPCell logoCell = new PdfPCell(logo, false);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        textCell.addElement(titulo);
        textCell.addElement(info);

        innerTable.addCell(logoCell);
        innerTable.addCell(textCell);

        // WRAPPER TABLE CON DESPLAZAMIENTO
        PdfPTable wrapperTable = new PdfPTable(1);
        wrapperTable.setWidthPercentage(100);

        PdfPCell wrapped = new PdfPCell(innerTable);
        wrapped.setBorder(Rectangle.NO_BORDER);
        wrapped.setPaddingLeft(50); // Mover todo el header 50 a la derecha

        wrapperTable.addCell(wrapped);
        return wrapperTable;
    }
}