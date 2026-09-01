package com.comismar.informes.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.comismar.informes.R;
import com.comismar.informes.model.Informe;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InformeMercanciaPdfGenerator {

    private static final BaseColor COLOR_CABECERA = new BaseColor(13, 71, 115); // Azul Comismar
    private static final Font FONT_TITLE = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
    private static final Font FONT_LABEL = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK);
    private static final Font FONT_VALUE = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_SECTION = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);

    public static File generarPdf(Context context, File dir, Informe informe, List<Uri> fotos, boolean optimizar) {
        String fileName = "informe_mercancia_" + informe.referencia + "_" + System.currentTimeMillis() + ".pdf";
        File file = new File(dir, fileName);

        // Aumentamos el margen superior (90) para dejar espacio a la cabecera
        Document document = new Document(PageSize.A4, 36, 36, 90, 36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            writer.setPageEvent(new CustomHeaderFooterMercancia(context, informe.referencia, informe.siniestro));
            document.open();

            // 1. DATOS INTERVENCIÓN
            document.add(crearCabeceraSeccion("1. DATOS INTERVENCIÓN"));
            PdfPTable table1 = new PdfPTable(2);
            table1.setWidthPercentage(100);
            table1.setWidths(new float[]{30, 70});
            addFila(table1, "Referencia", informe.referencia);
            addFila(table1, "Nº de siniestro", informe.siniestro);
            addFila(table1, "Asegurado", informe.asegurado);
            addFila(table1, "Requirente", informe.requirente);
            document.add(table1);

            // 2. INSPECCIÓN
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("2. INSPECCIÓN"));
            PdfPTable table2 = new PdfPTable(2);
            table2.setWidthPercentage(100);
            table2.setWidths(new float[]{30, 70});
            addFila(table2, "Fecha", informe.fechaInspeccion);
            addFila(table2, "Lugar", informe.lugar);
            addFila(table2, "Técnico Intervención", informe.tecnico);
            addFila(table2, "Otras personas", informe.otrasPersonas);
            document.add(table2);

            // 3. MERCANCÍA
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("3. MERCANCÍA"));
            PdfPTable table3 = new PdfPTable(2);
            table3.setWidthPercentage(100);
            table3.setWidths(new float[]{30, 70});
            addFila(table3, "Bultos y peso:", informe.bultosPeso);
            addFila(table3, "Valor:", informe.valorMercancia);
            document.add(table3);

            // 3. TRANSPORTE (bis)
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("3. TRANSPORTE"));
            PdfPTable table4 = new PdfPTable(2);
            table4.setWidthPercentage(100);
            table4.setWidths(new float[]{30, 70});
            addFila(table4, "Medio de transporte", informe.medioTransporte);
            addFila(table4, "Fecha carga / origen", informe.fechaCarga);
            addFila(table4, "Fecha descarga / destino", informe.fechaDescarga);
            addFila(table4, "Fecha siniestro / lugar", informe.fechaSiniestroLugar);
            document.add(table4);

            // 4. DESCRIPCIÓN Y ALCANCE DE LA AVERÍA
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("4. DESCRIPCIÓN Y ALCANCE DE LA AVERÍA"));
            document.add(crearParrafoBloque(informe.danos));

            // 5. CAUSA DE LA AVERÍA
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("5. CAUSA DE LA AVERÍA"));
            document.add(crearParrafoBloque(informe.causas));

            // 6. RESERVA
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("6. RESERVA"));
            document.add(crearParrafoBloque(informe.reserva));

            // 7. OBSERVACIONES
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("7. OBSERVACIONES"));
            document.add(crearParrafoBloque(informe.observaciones));

            // 8. DOCUMENTACIÓN PENDIENTE
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("8. DOCUMENTACIÓN PENDIENTE"));
            document.add(crearParrafoBloque(informe.docPendiente));

            // 9. ACTUALIZACIONES
            document.add(Chunk.NEWLINE);
            document.add(crearCabeceraSeccion("9. ACTUALIZACIONES"));
            document.add(crearParrafoBloque(informe.actualizaciones));

            // PAGINA 2: REPORTAJE FOTOGRÁFICO
            document.newPage();
            document.add(new Paragraph("REPORTAJE FOTOGRÁFICO", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            document.add(Chunk.NEWLINE);

            if (fotos != null && !fotos.isEmpty()) {
                PdfPTable photoTable = new PdfPTable(2);
                photoTable.setWidthPercentage(100);
                photoTable.setSpacingBefore(10f);

                for (Uri uri : fotos) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, optimizar ? 30 : 80, stream);
                        Image img = Image.getInstance(stream.toByteArray());
                        img.scaleToFit(250, 180);
                        
                        PdfPCell cell = new PdfPCell(img);
                        cell.setBorder(Rectangle.NO_BORDER);
                        cell.setPadding(5);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        photoTable.addCell(cell);
                    } catch (Exception e) {
                        photoTable.addCell(new PdfPCell(new Paragraph("Error al cargar imagen")));
                    }
                }
                photoTable.completeRow();
                document.add(photoTable);
            }

            // TEXTO LEGAL Y FIRMA
            document.add(new Paragraph("\n\n" + context.getString(R.string.report_legal_text), new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL)));
            
            String fechaActual = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault()).format(new Date());
            Paragraph pLugarFecha = new Paragraph("\nEn " + informe.lugar + ", " + fechaActual, FONT_VALUE);
            pLugarFecha.setAlignment(Element.ALIGN_RIGHT);
            document.add(pLugarFecha);

            // Firma
            try {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.firma);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Image firmaImg = Image.getInstance(stream.toByteArray());
                firmaImg.scaleToFit(100, 100);
                firmaImg.setAlignment(Element.ALIGN_RIGHT);
                document.add(firmaImg);
            } catch (Exception ignored) {}

            Paragraph pFirmaText = new Paragraph(context.getString(R.string.report_signature_block), new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD));
            pFirmaText.setAlignment(Element.ALIGN_RIGHT);
            document.add(pFirmaText);

            document.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PdfPTable crearCabeceraSeccion(String titulo) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Paragraph(titulo, FONT_SECTION));
        cell.setBackgroundColor(COLOR_CABECERA);
        cell.setPadding(5);
        cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(cell);
        return table;
    }

    private static void addFila(PdfPTable table, String label, String value) {
        PdfPCell cellLabel = new PdfPCell(new Paragraph(label, FONT_LABEL));
        cellLabel.setPadding(5);
        table.addCell(cellLabel);

        PdfPCell cellValue = new PdfPCell(new Paragraph(value != null ? value : "", FONT_VALUE));
        cellValue.setPadding(5);
        table.addCell(cellValue);
    }

    private static PdfPTable crearParrafoBloque(String texto) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Paragraph(texto != null ? texto : "", FONT_VALUE));
        cell.setPadding(5);
        cell.setMinimumHeight(40);
        table.addCell(cell);
        return table;
    }
}
