``` mermaid 

classDiagram

%% ===================== ENUMS =====================

class Rol {
  <<enumeration>>
  USER
  ADMIN
}

class Moneda {
  <<enumeration>>
  ARS
  USD
}

class Fuente {
  <<enumeration>>
  MANUAL
  WEB
}

class EstadoSolicitud {
  <<enumeration>>
  PENDIENTE
  ACEPTADA
  RECHAZADA
}

class TipoAlerta {
  <<enumeration>>
  NUEVA_PROPIEDAD
}

%% ===================== ENTIDADES =====================

class Usuario {
  Long id
  String nombre
  String email
  String passwordHash
  Rol rol
  Boolean verificado
  String telefono
  String documento
  String descripcionPerfil
  LocalDateTime createdAt
}

class Inmobiliaria {
  Long id
  String nombre
  String email
  String telefono
  String logoUrl
  Boolean verificada
}

class Propiedad {
  Long id
  String titulo
  String descripcion
  BigDecimal precio
  BigDecimal expensas
  Moneda moneda
  String calle
  Integer altura
  Integer piso
  Integer habitaciones
  Double metrosCuadrados
  Boolean incluyeImpuestos
  Integer plazoActualizacionMeses
  String requisitosIngreso
  Fuente fuente
  String urlOriginal
}

class ImagenPropiedad {
  Long id
  String url
  Integer orden
}

class FuenteExterna {
  Long id
  String nombre
  String urlBase
  Boolean activa
}

class SolicitudVisita {
  Long id
  LocalDateTime fechaSolicitud
  LocalDateTime fechaPropuesta
  String mensaje
  EstadoSolicitud estado
}

class Alerta {
  Long id
  TipoAlerta tipo
  String criterioBusqueda
  Boolean activa
}

class FavoritoPropiedad {
  Long id
  LocalDateTime fechaGuardado
}

class FavoritoInmobiliaria {
  Long id
  LocalDateTime fechaGuardado
}

%% ===================== SERVICIOS =====================

class AuthService {
  +register()
  +login()
  +validarJWT()
}

class PropiedadService {
  +publicarPropiedad()
  +listarDisponibles()
  +obtenerDetalle()
}

class VisitaService {
  +solicitarVisita()
  +cambiarEstado()
}

class EmailService {
  +enviarNotificacionVisita()
  +enviarAlertaNuevaPropiedad()
}

class AlertaService {
  +crearAlerta()
  +evaluarAlertas()
}

class ScrapingService {
  +importarDesdeFuente()
}

%% ===================== RELACIONES =====================

Usuario "1" --> "1" Inmobiliaria : crea
Inmobiliaria "1" --> "N" Propiedad : publica
Propiedad "1" --> "N" ImagenPropiedad : tiene

Propiedad "1" --> "N" SolicitudVisita : recibe
Usuario "1" --> "N" SolicitudVisita : solicita

Usuario "1" --> "N" Alerta : configura

Usuario "1" --> "N" FavoritoPropiedad
FavoritoPropiedad --> Propiedad

Usuario "1" --> "N" FavoritoInmobiliaria
FavoritoInmobiliaria --> Inmobiliaria

SolicitudVisita --> EstadoSolicitud
Propiedad --> Moneda
Propiedad --> Fuente
Usuario --> Rol
Alerta --> TipoAlerta

FuenteExterna --> ScrapingService

VisitaService --> EmailService
AlertaService --> EmailService

```