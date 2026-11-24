package app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

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

    /** Avisa al solicitante si el publicador aceptó o rechazó su solicitud de visita. */
    public void enviarRespuestaVisita(String emailSolicitante,
                                      String nombreSolicitante,
                                      String tituloPropiedad,
                                      String direccion,
                                      LocalDateTime fechaPropuesta,
                                      boolean aceptada) {
        String fecha = fechaPropuesta != null ? fechaPropuesta.format(FORMATO_FECHA) : "la fecha propuesta";
        String saludo = "Hola " + (nombreSolicitante != null ? nombreSolicitante : "") + ",\n\n";

        String asunto;
        String cuerpo;
        if (aceptada) {
            asunto = "Visita confirmada - " + tituloPropiedad;
            cuerpo = saludo
                    + "¡Buenas noticias! Aceptaron tu solicitud para visitar \"" + tituloPropiedad + "\".\n\n"
                    + "Dirección: " + direccion + "\n"
                    + "Fecha: " + fecha + "\n\n"
                    + "Si necesitás cambiar el horario, contactá al publicador desde la propiedad en Alquileres R4.\n\n";
        } else {
            asunto = "Solicitud de visita rechazada - " + tituloPropiedad;
            cuerpo = saludo
                    + "El publicador de \"" + tituloPropiedad + "\" no pudo aceptar tu solicitud de visita para el "
                    + fecha + ".\n\n"
                    + "Podés seguir buscando otras propiedades en Alquileres R4.\n\n";
        }
        cuerpo += "Saludos,\nEquipo Alquileres R4";

        enviarEmail(emailSolicitante, asunto, cuerpo);
    }
}
