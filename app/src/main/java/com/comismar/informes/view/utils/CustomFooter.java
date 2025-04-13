package com.comismar.informes.view.utils;

import android.graphics.Bitmap;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;

public class CustomFooter extends PdfPageEventHelper {

    private Image footerLogo;

    public CustomFooter(Bitmap footerBitmap) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            footerBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            this.footerLogo = Image.getInstance(stream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        if (footerLogo == null) return;

        try {
            PdfContentByte canvas = writer.getDirectContent();
            Rectangle pageSize = document.getPageSize();

            // Escalar imagen a todo el ancho útil manteniendo relación de aspecto
            float pageWidth = pageSize.getRight() - pageSize.getLeft();
            float originalWidth = footerLogo.getWidth();
            float originalHeight = footerLogo.getHeight();
            float scaleFactor = pageWidth / originalWidth;

            float scaledWidth = pageWidth;
            float scaledHeight = originalHeight * scaleFactor;

            footerLogo.scaleAbsolute(scaledWidth, scaledHeight);

            float x = 0;
            float y = pageSize.getBottom();

            footerLogo.setAbsolutePosition(x, y);
            canvas.addImage(footerLogo);

            // Número de página sobre la imagen (abajo a la derecha)
            Font pageFont = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
            Phrase pageNumber = new Phrase("Página " + writer.getPageNumber(), pageFont);

            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_RIGHT,
                    pageNumber,
                    document.right() - 5,
                    y + 5, // un poco por encima del borde inferior
                    0
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

