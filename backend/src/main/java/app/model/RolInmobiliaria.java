package app.model;

/**
 * Rol de un usuario dentro de su inmobiliaria (issue #61).
 * Independiente de {@link Rol}, que es el rol en la plataforma (USER / ADMIN).
 */
public enum RolInmobiliaria {
    /** Gestiona la inmobiliaria: edita sus datos y agrega o quita miembros. Es quien la creó. */
    ADMINISTRADOR,
    /** Publica propiedades a nombre de la inmobiliaria y gestiona sus solicitudes de visita. */
    AGENTE
}
