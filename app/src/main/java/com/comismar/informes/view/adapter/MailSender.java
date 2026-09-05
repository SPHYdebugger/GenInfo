package com.comismar.informes.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import com.comismar.informes.view.utils.AppLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.comismar.informes.BuildConfig;

public class MailSender {

    private static final String API_KEY = BuildConfig.BREVO_API_KEY;
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";
    
    public MailSender(String usuario, String contraseña) {
    }

    /**
     * Envía un correo con destinatarios visibles (to) y ocultos (bcc)
     */
    public void enviarCorreo(Context context, String asunto, String cuerpoHtml, String desde, List<String> toEmails, List<String> bccEmails, File archivoAdjunto) throws Exception {
        String logTo = toEmails != null ? String.join(", ", toEmails) : "ninguno";
        String logBcc = bccEmails != null ? String.join(", ", bccEmails) : "ninguno";
        AppLogger.logInfo(context, "MailSender", "Iniciando envío HTML. To: [" + logTo + "] | Bcc: [" + logBcc + "]");
        
        OkHttpClient client = new OkHttpClient();

        JsonObject root = new JsonObject();
        
        // Emisor
        JsonObject sender = new JsonObject();
        sender.addProperty("name", "InfoGen Informes");
        sender.addProperty("email", desde);
        root.add("sender", sender);

        // Destinatarios principales (To)
        JsonArray toArray = new JsonArray();
        if (toEmails != null) {
            for (String email : toEmails) {
                JsonObject recipient = new JsonObject();
                recipient.addProperty("email", email);
                toArray.add(recipient);
            }
        }
        root.add("to", toArray);

        // Destinatarios ocultos (Bcc)
        if (bccEmails != null && !bccEmails.isEmpty()) {
            JsonArray bccArray = new JsonArray();
            for (String email : bccEmails) {
                JsonObject recipient = new JsonObject();
                recipient.addProperty("email", email);
                bccArray.add(recipient);
            }
            root.add("bcc", bccArray);
        }

        root.addProperty("subject", asunto);
        root.addProperty("htmlContent", cuerpoHtml);

        if (archivoAdjunto != null && archivoAdjunto.exists()) {
            AppLogger.logInfo(context, "MailSender", "Adjuntando archivo: " + archivoAdjunto.getName());
            JsonArray attachments = new JsonArray();
            JsonObject attachment = new JsonObject();
            attachment.addProperty("name", archivoAdjunto.getName());
            attachment.addProperty("content", fileToBase64(archivoAdjunto));
            attachments.add(attachment);
            root.add("attachment", attachments);
        }

        RequestBody body = RequestBody.create(
                root.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(BREVO_URL)
                .addHeader("api-key", API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "sin cuerpo";
            if (response.isSuccessful()) {
                AppLogger.logSuccess(context, "MailSender", "Correo HTML enviado con éxito. Respuesta: " + responseBody);
            } else {
                AppLogger.logError(context, "MailSender", "Error API Brevo (" + response.code() + "): " + responseBody, null);
                throw new IOException("Error en la API de Brevo: " + response.code() + " " + responseBody);
            }
        } catch (Exception e) {
            AppLogger.logError(context, "MailSender", "Excepción durante envío HTTP", e);
            throw e;
        }
    }

    public void enviarCorreoKeepAlive(Context context, String emailSistema) throws Exception {
        String body = "<html><body><p>Pulso de actividad para mantener la API Key activa.</p></body></html>";
        List<String> to = new ArrayList<>();
        to.add(emailSistema);
        enviarCorreo(context, "Renovación por inactividad", body, emailSistema, to, null, null);
    }

    public static String getResourceToBase64(Context context, int resourceId) {
        try {
            Bitmap bm = BitmapFactory.decodeResource(context.getResources(), resourceId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] b = baos.toByteArray();
            return Base64.encodeToString(b, Base64.NO_WRAP);
        } catch (Exception e) {
            return "";
        }
    }

    private String fileToBase64(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            int offset = 0;
            int numRead;
            while (offset < bytes.length && (numRead = fis.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
