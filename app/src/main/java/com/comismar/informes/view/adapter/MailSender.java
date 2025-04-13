package com.comismar.informes.view.adapter;


import java.io.File;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailSender {

    private final String usuario;
    private final String contraseña;

    public MailSender(String usuario, String contraseña) {
        this.usuario = usuario;
        this.contraseña = contraseña;
    }

    public void enviarCorreo(String asunto, String cuerpo, String desde, String hacia, File archivoAdjunto) throws Exception {
        Properties props = new Properties();

        // Configuración para Gmail
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usuario, contraseña);
            }
        });

        Message mensaje = new MimeMessage(session);
        mensaje.setFrom(new InternetAddress(desde));
        mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(hacia));
        mensaje.setSubject(asunto);

        // Cuerpo del correo
        MimeBodyPart cuerpoTexto = new MimeBodyPart();
        cuerpoTexto.setText(cuerpo);

        // Adjunto
        MimeBodyPart adjunto = new MimeBodyPart();
        FileDataSource fuente = new FileDataSource(archivoAdjunto);
        adjunto.setDataHandler(new DataHandler(fuente));
        adjunto.setFileName(archivoAdjunto.getName());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(cuerpoTexto);
        multipart.addBodyPart(adjunto);

        mensaje.setContent(multipart);

        Transport.send(mensaje);
    }
}
