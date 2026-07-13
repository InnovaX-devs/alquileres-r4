package app.service;

import java.math.BigDecimal;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmail(String destinatario, String asunto, String cuerpo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);
        mailSender.send(mensaje);
    }

    public void enviarNotificacionVisita(String emailPublicador, 
                                        String nombrePublicador,
                                        String tituloPropiedad,
                                        String nombreSolicitante,
                                        String emailSolicitante,
                                        String telefonoSolicitante,
                                        String mensaje) {
        String asunto = "Nueva solicitud de visita - " + tituloPropiedad;

        String cuerpo = "Hola " + nombrePublicador + ",\n\n"
                + nombreSolicitante + " está interesado/a en tu propiedad \"" + tituloPropiedad + "\".\n\n"
                + "Podés contactarlo/a en:\n"
                + "Email: " + emailSolicitante + "\n"
                + "Teléfono: " + (telefonoSolicitante != null ? telefonoSolicitante : "No especificado") + "\n\n"
                + "Mensaje: " + (mensaje != null ? mensaje : "Sin mensaje") + "\n\n"
                + "Saludos,\n"
                + "Equipo Alquileres R4";

        enviarEmail(emailPublicador, asunto, cuerpo);
    }

    public void enviarNotificacionAlerta(String emailUsuario,
                                        String nombreUsuario,
                                        String tituloPropiedad,
                                        String calle,
                                        String altura,
                                        BigDecimal precio,
                                        String moneda) {
        String asunto = "Nueva propiedad que coincide con tu alerta";

        String cuerpo = "Hola " + nombreUsuario + ",\n\n"
                + "Encontramos una propiedad que coincide con tus criterios de búsqueda:\n\n"
                + "Propiedad: " + tituloPropiedad + "\n"
                + "Dirección: " + calle + " " + altura + "\n"
                + "Precio: " + moneda + " " + precio + "\n\n"
                + "Ingresá a Alquileres R4 para ver el detalle completo.\n\n"
                + "Saludos,\n"
                + "Equipo Alquileres R4";

        enviarEmail(emailUsuario, asunto, cuerpo);
    }
}