package com.comismar.informes.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.comismar.informes.R;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InformePdfGenerator {

    public static File generarPdfDesdeDatos(Context context, File outputDir,
                                            String referencia, String siniestro, String requirente, String lugar,
                                            String tecnico, String nombreBarco, String matricula, String daños,
                                            String causas, String reserva, String observaciones, String docP, List<Uri> imagenesAdjuntas) {
        File file = null;
        String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());
        try {
            Document document = new Document(PageSize.A4, 40, 40, 20, 20);
            String nombreArchivo = "informe_" + referencia.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + System.currentTimeMillis() + ".pdf";


            file = new File(outputDir, nombreArchivo);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            Bitmap footerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.footer);
            writer.setPageEvent(new CustomFooter(footerBitmap));

            document.open();

            Font fontTitle = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fontSection = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            Font fontLabel = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
            Font fontValue = new Font(Font.FontFamily.HELVETICA, 9);

            Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.titulo);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Image logo = Image.getInstance(stream.toByteArray());
            logo.scaleToFit(160, 80);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);

            Paragraph titulo = new Paragraph("INFORME PRELIMINAR EERR", fontTitle);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);


            // Añadir columna de 1 celda vertical con texto legal
            PdfPTable tablaConTextoVertical = new PdfPTable(new float[]{1f, 9f}); // 10% para texto, 90% para tabla
            tablaConTextoVertical.setWidthPercentage(100);
            tablaConTextoVertical.setSpacingBefore(10f);

            // Texto vertical
            Font fontVertical = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.GRAY);
            PdfPCell celdaVertical = new PdfPCell(new Phrase("Rev.: " + fechaHoy + " - Este documento es propiedad de COMISMAR, S.A. y de uso estrictamente confidencial, no podrá ser utilizado ni distribuido sin autorización expresa de la dirección de la empresa.", fontVertical));
            celdaVertical.setRotation(90); // gira el texto
            celdaVertical.setBorder(PdfPCell.NO_BORDER);
            celdaVertical.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaVertical.setVerticalAlignment(Element.ALIGN_MIDDLE);

            // Añadir la celda vertical
            tablaConTextoVertical.addCell(celdaVertical);


            PdfPTable tabla = new PdfPTable(new float[]{1f, 3f});
            tabla.setWidthPercentage(90);
            tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.setSpacingBefore(10f);  // menos espacio
            tabla.setSpacingAfter(10f);



            // Sección 1
            tabla.addCell(getSeccion("1. DATOS INTERVENCIÓN", fontSection));
            tabla.addCell(getCeldaEtiqueta("Referencia"));
            tabla.addCell(getCeldaDato(referencia));
            tabla.addCell(getCeldaEtiqueta("Siniestro"));
            tabla.addCell(getCeldaDato(siniestro));
            tabla.addCell(getCeldaEtiqueta("Requirente"));
            tabla.addCell(getCeldaDato(requirente));


            // Sección 2
            tabla.addCell(getSeccion("2. INSPECCIÓN", fontSection));
            tabla.addCell(getCeldaEtiqueta("Fecha"));
            String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            tabla.addCell(getCeldaDato(fechaActual));
            tabla.addCell(getCeldaEtiqueta("Lugar"));
            tabla.addCell(getCeldaDato(lugar));
            tabla.addCell(getCeldaEtiqueta("Técnico Intervención"));
            tabla.addCell(getCeldaDato("Álvaro Pousada López     COMISMAR S.A."));
            tabla.addCell(getCeldaEtiqueta("Otras personas"));
            tabla.addCell(getCeldaDato(tecnico));


            // Sección 3
            tabla.addCell(getSeccion("3. EMBARCACIÓN", fontSection));
            tabla.addCell(getCeldaEtiqueta("Nombre barco"));
            tabla.addCell(getCeldaDato(nombreBarco));
            tabla.addCell(getCeldaEtiqueta("Matrícula"));
            tabla.addCell(getCeldaDato(matricula));


            // Secciones de texto largo
            tabla.addCell(getSeccion("4. DAÑOS", fontSection));
            tabla.addCell(getTextoLargo(daños, fontValue));

            tabla.addCell(getSeccion("5. CAUSAS", fontSection));
            tabla.addCell(getTextoLargo(causas, fontValue));

            tabla.addCell(getSeccion("6. RESERVA", fontSection));
            tabla.addCell(getTextoLargo(reserva, fontValue));

            tabla.addCell(getSeccion("7. OBSERVACIONES", fontSection));
            tabla.addCell(getTextoLargo(observaciones, fontValue));
            tabla.addCell(getSeccion("DOCUMENTACIÓN PENDIENTE", fontSection));
            tabla.addCell(getTextoLargo(docP, fontValue));


            PdfPCell celdaTablaPrincipal = new PdfPCell(tabla);
            celdaTablaPrincipal.setBorder(PdfPCell.NO_BORDER);
            tablaConTextoVertical.addCell(celdaTablaPrincipal);
            document.add(tablaConTextoVertical);


            document.newPage();

            document.add(new Paragraph(" ")); // espacio
            document.add(new Paragraph("IMÁGENES ADJUNTAS", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            document.add(new Paragraph(" ")); // espacio

            PdfPTable tablaFotos = new PdfPTable(2);
            tablaFotos.setWidthPercentage(100);
            tablaFotos.setSpacingBefore(10f);
            tablaFotos.setSpacingAfter(10f);

            int totalCeldas = 4; // 2 columnas x 2 filas

            if (imagenesAdjuntas != null && !imagenesAdjuntas.isEmpty()) {
                int index = 0;
                for (Uri uri : imagenesAdjuntas) {
                    if (index >= totalCeldas) break;
                    try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                        if (inputStream != null) {
                            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                            if (bmp != null) {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bmp.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                                Image imagen = Image.getInstance(baos.toByteArray());

                                float targetHeight = 170f;
                                float aspectRatio = (float) bmp.getWidth() / bmp.getHeight();
                                float targetWidth = targetHeight * aspectRatio;
                                imagen.scaleAbsolute(targetWidth, targetHeight);
                                imagen.setAlignment(Image.ALIGN_CENTER);

                                PdfPCell celda = new PdfPCell(imagen, true);
                                celda.setFixedHeight(targetHeight + 20);
                                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                                celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                celda.setPadding(5);
                                celda.setBorder(PdfPCell.NO_BORDER);

                                tablaFotos.addCell(celda);
                                index++;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Rellenar hasta completar las 4 celdas
                for (int i = index; i < totalCeldas; i++) {
                    PdfPCell celdaVacia = new PdfPCell();
                    celdaVacia.setFixedHeight(170);
                    celdaVacia.setBorder(PdfPCell.NO_BORDER);
                    tablaFotos.addCell(celdaVacia);
                }

            } else {
                document.add(new Paragraph("\nNo se han adjuntado fotos", fontLabel));
                for (int i = 0; i < totalCeldas; i++) {
                    PdfPCell celda = new PdfPCell(new Phrase(" "));
                    celda.setFixedHeight(170); // mantener altura
                    celda.setBorder(PdfPCell.NO_BORDER);
                    tablaFotos.addCell(celda);
                }
            }


            document.add(tablaFotos);


            Font fontLegal = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.DARK_GRAY);
            Font fontLegalBold = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.DARK_GRAY);
            document.add(new Paragraph("\n\nEste informe, por su carácter exclusivamente técnico, se emite sin prejuzgar cuestiones de derecho y/o responsabilidad de cualquiera de las partes interesadas en el mismo, y a reserva de las condiciones establecidas en la Póliza de Seguros.\n" +
                    "\n" +
                    "Además, y a los efectos del Artículo 335 de la Ley de Enjuiciamiento Civil 1/2000, el abajo firmante D. Álvaro Pousada López en su calidad de PERITO, manifiesta bajo juramento o promesa de decir verdad, que ha actuado y, en su caso, actuará con la mayor objetividad posible, tomando en consideración tanto lo que pueda favorecer como lo que sea susceptible de causar perjuicio a cualquiera de las partes, y que conoce las sanciones penales en las que podría incurrir si incumpliere su deber como perito.\n" +
                    "\n" +
                    "De conformidad con lo dispuesto en la normativa de protección de datos, comunicamos que para los posibles datos personales incluidos en este informe pueden ejercitarse los derechos de acceso, rectificación, oposición y cancelación ante COMISMAR, S.A., sito en la calle Pintor Juan Gris, nº 4 - 28020 – Madrid.\t", fontLegal));



            // Crear párrafo alineado a la derecha
            Paragraph parrafoFecha = new Paragraph("En " + lugar +", " + fechaHoy, fontLabel);
            parrafoFecha.setAlignment(Element.ALIGN_RIGHT);

            // Agregar al documento
            document.add(parrafoFecha);

            // Cargar la imagen (firma, sello, etc.)
            Bitmap imagenBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.firma); // cambia por el nombre real
            ByteArrayOutputStream imgStream = new ByteArrayOutputStream();
            imagenBitmap.compress(Bitmap.CompressFormat.PNG, 100, imgStream);
            Image imagen = Image.getInstance(imgStream.toByteArray());

            // Escalar y posicionar
            imagen.scaleToFit(120, 60); // ajusta tamaño según lo que necesites
            imagen.setAlignment(Image.ALIGN_RIGHT);

            // Añadir espacio antes si hace falta
            document.add(imagen);

            // Crear párrafo alineado a la derecha
            Paragraph parrafoInspector = new Paragraph("COMISARIADO ESPAÑOL MARÍTIMO\n" +
                    "Fdo. Álvaro Pousada López\n" +
                    "COMISARIO DE AVERÍAS \n" +
                    "APCAS Nº 9367\n", fontLegalBold);
            parrafoInspector.setAlignment(Element.ALIGN_RIGHT);
            document.add(parrafoInspector);

            document.close();
            Toast.makeText(context, "PDF guardado", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show();
            return null;
        }
        return file;
    }

    private static PdfPCell getCeldaEtiqueta(String texto) {
        Font fontBold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fontBold));
        celda.setBackgroundColor(new BaseColor(230, 230, 250));
        celda.setPadding(4);  // menos padding
        return celda;
    }

    private static PdfPCell getCeldaDato(String texto) {
        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 9);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fontNormal));
        celda.setPadding(4);
        return celda;
    }

    private static PdfPCell getTextoLargo(String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setColspan(2);
        celda.setMinimumHeight(40);  // menos altura
        celda.setPadding(4);
        return celda;
    }

    private static PdfPCell getSeccion(String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setColspan(2);
        celda.setBackgroundColor(new BaseColor(0, 51, 102));
        celda.setPadding(3);
        celda.setMinimumHeight(25); // menos altura para la sección
        return celda;
    }

}
