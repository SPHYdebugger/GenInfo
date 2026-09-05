package com.comismar.informes.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.os.ConfigurationCompat;

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
                                            String causas, String reserva, String observaciones, String docP,
                                            List<Uri> imagenesAdjuntas, boolean optimizarFotos) {
        File file = null;
        Locale reportLocale = ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0);
        if (reportLocale == null) {
            reportLocale = Locale.getDefault();
        }
        String fechaHoy = new SimpleDateFormat(context.getString(R.string.report_date_long_pattern), reportLocale).format(new Date());
        try {
            Document document = new Document(PageSize.A4, 40, 40, 20, 20);
            
            String fechaArchivo = new SimpleDateFormat("ddMMyy", Locale.getDefault()).format(new Date());
            String refLimpia = referencia.replaceAll("[^a-zA-Z0-9_-]", "_");
            String nombreArchivo = refLimpia + "_informe_preliminar_" + fechaArchivo + ".pdf";

            file = new File(outputDir, nombreArchivo);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            Bitmap footerBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pienuevo);
            byte[] footerBytes = comprimirImagenDecorativa(footerBitmap, optimizarFotos);
            writer.setPageEvent(new CustomFooter(footerBytes, context.getString(R.string.report_page_prefix)));

            document.open();

            Font fontTitle = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font fontSection = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            Font fontLabel = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
            Font fontValue = new Font(Font.FontFamily.HELVETICA, 9);

            Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.titulo);
            byte[] logoBytes = comprimirImagenDecorativa(logoBitmap, optimizarFotos);
            Image logo = Image.getInstance(logoBytes);
            logo.scaleToFit(160, 80);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);

            Paragraph titulo = new Paragraph(context.getString(R.string.report_title), fontTitle);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(20);
            document.add(titulo);


            // Texto vertical de revisión (ahora ocupa toda la altura de la página de forma absoluta)
            PdfContentByte cb = writer.getDirectContent();
            Font fontVertical = new Font(Font.FontFamily.HELVETICA, 6, Font.NORMAL, BaseColor.GRAY);
            Phrase phraseVertical = new Phrase(context.getString(R.string.report_vertical_revision, fechaHoy), fontVertical);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, phraseVertical, 20, PageSize.A4.getHeight() / 2, 90);

            PdfPTable tabla = new PdfPTable(new float[]{1f, 3f});
            tabla.setWidthPercentage(90);
            tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
            tabla.setSpacingBefore(10f);
            tabla.setSpacingAfter(50f); // Aumentado para dejar separación con el pie de página



            // Sección 1
            tabla.addCell(getSeccion(context.getString(R.string.report_section_1), fontSection));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_reference)));
            tabla.addCell(getCeldaDato(referencia));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_incident)));
            tabla.addCell(getCeldaDato(siniestro));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_requester)));
            tabla.addCell(getCeldaDato(requirente));


            // Sección 2
            tabla.addCell(getSeccion(context.getString(R.string.report_section_2), fontSection));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_date)));
            String fechaActual = new SimpleDateFormat(context.getString(R.string.report_date_time_pattern), reportLocale).format(new Date());
            tabla.addCell(getCeldaDato(fechaActual));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_place)));
            tabla.addCell(getCeldaDato(lugar));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_technician)));
            tabla.addCell(getCeldaDato(context.getString(R.string.report_default_technician)));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_other_people)));
            tabla.addCell(getCeldaDato(tecnico));


            // Sección 3
            tabla.addCell(getSeccion(context.getString(R.string.report_section_3), fontSection));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_boat_name)));
            tabla.addCell(getCeldaDato(nombreBarco));
            tabla.addCell(getCeldaEtiqueta(context.getString(R.string.report_label_registration)));
            tabla.addCell(getCeldaDato(matricula));


            // Secciones de texto largo
            tabla.addCell(getSeccion(context.getString(R.string.report_section_4), fontSection));
            tabla.addCell(getTextoLargo(daños, fontValue));

            tabla.addCell(getSeccion(context.getString(R.string.report_section_5), fontSection));
            tabla.addCell(getTextoLargo(causas, fontValue));

            tabla.addCell(getSeccion(context.getString(R.string.report_section_6), fontSection));
            tabla.addCell(getTextoLargo(reserva, fontValue));

            tabla.addCell(getSeccion(context.getString(R.string.report_section_7), fontSection));
            tabla.addCell(getTextoLargo(observaciones, fontValue));
            tabla.addCell(getSeccion(context.getString(R.string.report_section_pending_docs), fontSection));
            tabla.addCell(getTextoLargoEstrecho(docP, fontValue));


            document.add(tabla);


            document.newPage();

            document.add(new Paragraph(context.getString(R.string.report_attached_images), new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));

            PdfPTable tablaFotos = new PdfPTable(2);
            tablaFotos.setWidthPercentage(100);
            tablaFotos.setSpacingBefore(10f);
            tablaFotos.setSpacingAfter(10f);

            int totalCeldas = 4; // 2 columnas x 2 filas

            if (imagenesAdjuntas != null && !imagenesAdjuntas.isEmpty()) {
                int index = 0;
                int totalImagenes = Math.min(imagenesAdjuntas.size(), totalCeldas);
                int maxBytesPorImagen = calcularMaxBytesPorImagen(totalImagenes, optimizarFotos);
                for (Uri uri : imagenesAdjuntas) {
                    if (index >= totalCeldas) break;
                    try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                        if (inputStream != null) {
                            Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                            if (bmp != null) {
                                byte[] imagenBytes = comprimirImagen(bmp, optimizarFotos, maxBytesPorImagen);
                                Image imagen = Image.getInstance(imagenBytes);

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
                document.add(new Paragraph("\n" + context.getString(R.string.report_no_attached_photos), fontLabel));
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
            document.add(new Paragraph("\n\n" + context.getString(R.string.report_legal_text), fontLegal));



            // Crear párrafo alineado a la derecha
            Paragraph parrafoFecha = new Paragraph(context.getString(R.string.report_location_date, lugar, fechaHoy), fontLabel);
            parrafoFecha.setAlignment(Element.ALIGN_RIGHT);

            // Agregar al documento
            document.add(parrafoFecha);

            // Cargar la imagen (firma, sello, etc.)
            Bitmap imagenBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.firma); // cambia por el nombre real
            byte[] firmaBytes = comprimirImagenDecorativa(imagenBitmap, optimizarFotos);
            Image imagen = Image.getInstance(firmaBytes);

            // Escalar y posicionar
            imagen.scaleToFit(120, 60); // ajusta tamaño según lo que necesites
            imagen.setAlignment(Image.ALIGN_RIGHT);

            // Añadir espacio antes si hace falta
            document.add(imagen);

            // Crear párrafo alineado a la derecha
            Paragraph parrafoInspector = new Paragraph(context.getString(R.string.report_signature_block), fontLegalBold);
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

    private static int calcularMaxBytesPorImagen(int totalImagenes, boolean optimizarFotos) {
        if (!optimizarFotos || totalImagenes <= 0) {
            return Integer.MAX_VALUE;
        }
        int maxBytesTotal = 500_000; // deja margen para el resto del PDF
        int maxPorImagen = maxBytesTotal / totalImagenes;
        return Math.max(60_000, maxPorImagen);
    }

    private static byte[] comprimirImagen(Bitmap bitmap, boolean optimizarFotos, int maxBytes) {
        if (!optimizarFotos) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            return baos.toByteArray();
        }

        int quality = 85;
        byte[] data = null;
        while (quality >= 30) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            data = baos.toByteArray();
            if (data.length <= maxBytes) {
                return data;
            }
            quality -= 10;
        }
        return data != null ? data : new byte[0];
    }

    private static byte[] comprimirImagenDecorativa(Bitmap bitmap, boolean optimizarFotos) {
        ByteArrayOutputStream originalStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, originalStream);
        byte[] original = originalStream.toByteArray();

        if (!optimizarFotos) {
            return original;
        }

        int targetBytes = Math.max(1, original.length / 2);
        byte[] mejor = null;

        for (int quality = 85; quality >= 30; quality -= 5) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            byte[] candidato = baos.toByteArray();

            if (mejor == null || candidato.length < mejor.length) {
                mejor = candidato;
            }
            if (candidato.length <= targetBytes) {
                return candidato;
            }
        }

        return mejor != null ? mejor : original;
    }

    private static PdfPCell getCeldaEtiqueta(String texto) {
        Font fontBold = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fontBold));
        celda.setBackgroundColor(new BaseColor(230, 230, 250));
        celda.setPadding(3);  // Reducido de 4 a 3
        return celda;
    }

    private static PdfPCell getCeldaDato(String texto) {
        Font fontNormal = new Font(Font.FontFamily.HELVETICA, 9);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fontNormal));
        celda.setPadding(3); // Reducido de 4 a 3
        return celda;
    }

    private static PdfPCell getTextoLargo(String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setColspan(2);
        celda.setMinimumHeight(35);  // Reducido de 40 a 35
        celda.setPadding(3); // Reducido de 4 a 3
        return celda;
    }

    private static PdfPCell getTextoLargoEstrecho(String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setColspan(2);
        celda.setMinimumHeight(40);  // Restaurado a 40 para que no sea tan pequeño
        celda.setPadding(3);
        return celda;
    }

    private static PdfPCell getSeccion(String texto, Font font) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, font));
        celda.setColspan(2);
        celda.setBackgroundColor(new BaseColor(0, 51, 102));
        celda.setPadding(2); // Reducido de 3 a 2
        celda.setMinimumHeight(20); // Reducido de 25 a 20
        return celda;
    }

}
