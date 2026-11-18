# Conocimiento del Proyecto - Alquileres R4

## Descripción general
Sistema de gestión de propiedades inmobiliarias. Permite publicar propiedades, solicitar visitas, guardar favoritos, configurar alertas e importar propiedades desde fuentes externas via scraping.

---

## Stack tecnológico

| Capa | Tecnología |
|------|-----------|
| Backend | Spring Boot 3.3.0 (API REST) |
| Base de datos | PostgreSQL (Supabase) + Spring Data JPA + Hibernate |
| Autenticación | Spring Security + JWT (jjwt 0.12.5) |
| Frontend | React + Vite + Tailwind (arranca cuando termine el backend) |
| Scraping | Jsoup 1.17.2 |
| Imágenes | Cloudinary SDK Java 1.36.0 |
| Deploy | Railway + Docker |
| Java | 21 |
| Build | Maven |

---

## Repositorio
- **Estructura:** Monorepo (backend y frontend en el mismo repo)
- **URL:** https://github.com/piliorlando/alquileres-r4
- **Rama principal:** main
- **Convención de ramas:** `feat/issue-N-descripcion`
- **Convención de commits:** `feat: descripcion - closes #N` (cierra issues automáticamente al mergear)

### Estructura del monorepo
```
alquileres-r4/
├── backend/         → Spring Boot
│   ├── src/
│   ├── pom.xml
│   └── ...
├── frontend/        → React + Vite + Tailwind (se crea cuando termine el backend)
│   ├── src/
│   ├── package.json
│   └── ...
└── README.md
```

> Los comandos de Maven se corren desde `backend/`.

---

## Estructura del backend
```
src/
└── main/
    ├── java/app/
    │   ├── config/         → CorsConfig, JwtFilter, JwtUtil, PasswordEncoderConfig, SecurityConfig
    │   ├── controller/     → AlertaController, AuthController, InmobiliariaController, PropiedadController, UsuarioController, VisitaController
    │   ├── model/          → Alerta, EstadoSolicitud, FavoritoInmobiliaria, FavoritoPropiedad, Fuente, FuenteExterna, ImagenPropiedad, Inmobiliaria, Moneda, Propiedad, Rol, SolicitudVisita, TipoAlerta, Usuario
    │   ├── repository/     → AlertaRepository, FavoritoInmobiliariaRepository, FavoritoPropiedadRepository, FuenteExternaRepository, ImagenPropiedadRepository, InmobiliariaRepository, PropiedadRepository, SolicitudVisitaRepository, UsuarioRepository
    │   ├── service/        → AlertaService, AuthService, EmailService, InmobiliariaService, PropiedadService, ScrapingService, UsuarioService, VisitaService
    │   └── App.java        → clase principal con @SpringBootApplication
    └── resources/
        ├── application.properties         → configuración real (en .gitignore, nunca se sube)
        └── application.properties.example → plantilla con placeholders para nuevos devs
```

---

## Configuración (application.properties)
```properties
# Base de datos
spring.datasource.url=jdbc:postgresql://db.fbcxqwsmbnomqxrwyufs.supabase.co:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=AlquileresR4!
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# JWT
jwt.secret=CLAVE_SECRETA
jwt.expiration=86400000

# App
spring.application.name=app
server.port=8080
```

---

## Base de datos - Supabase
- **Host:** db.fbcxqwsmbnomqxrwyufs.supabase.co
- **Puerto:** 5432
- **DB:** postgres
- **Usuario:** postgres
- **Contraseña:** AlquileresR4!
- **Región:** South America (São Paulo)
- **Nota importante:** Supabase free tier usa IPv6. La conexión directa falla localmente en redes IPv4 pero Hibernate igual levanta gracias a `spring.jpa.database-platform`. Se resuelve al deployar en Railway.

---

## Modelo de datos

### Enums
- `Rol` → USER, ADMIN
- `Moneda` → ARS, USD
- `Fuente` → MANUAL, WEB
- `EstadoSolicitud` → PENDIENTE, ACEPTADA, RECHAZADA
- `TipoAlerta` → NUEVA_PROPIEDAD

### Entidades
- `Usuario` → id, nombre, email, passwordHash, rol, verificado, telefono, documento, descripcionPerfil, createdAt
- `Inmobiliaria` → id, nombre, email, telefono, logoUrl, verificada
- `Propiedad` → id, titulo, descripcion, precio, expensas, moneda, calle, altura, piso, habitaciones, metrosCuadrados, incluyeImpuestos, plazoActualizacionMeses, requisitosIngreso, fuente, urlOriginal
- `ImagenPropiedad` → id, url, orden
- `SolicitudVisita` → id, fechaSolicitud, fechaPropuesta, mensaje, estado
- `Alerta` → id, tipo, criterioBusqueda, activa
- `FavoritoPropiedad` → id, fechaGuardado
- `FavoritoInmobiliaria` → id, fechaGuardado
- `FuenteExterna` → id, nombre, urlBase, activa

### Relaciones
- Usuario 1 → 1 Inmobiliaria
- Inmobiliaria 1 → N Propiedad
- Propiedad 1 → N ImagenPropiedad
- Propiedad 1 → N SolicitudVisita
- Usuario 1 → N SolicitudVisita
- Usuario 1 → N Alerta
- Usuario 1 → N FavoritoPropiedad → Propiedad
- Usuario 1 → N FavoritoInmobiliaria → Inmobiliaria

---

## Estado de issues

### ✅ Completados
- #1 Crear estructura de paquetes
- #2 Configurar conexión a base de datos
- #3 Configurar JPA / Hibernate
- #4 Configurar CORS global
- #5 Configurar PasswordEncoder

### Pendientes por fase
- **Fase 2:** #12 Entidad Usuario, #13 Entidad Inmobiliaria, #14 Relación Usuario→Inmobiliaria
- **Fase 3:** #6-#11 Seguridad + JWT + Auth endpoints
- **Fase 4:** #17-#22 Propiedades
- **Fase 5:** #23-#26 Visitas
- **Fase 6:** #27-#29 Emails
- **Fase 7:** #30-#34 Favoritos
- **Fase 8:** #35-#38 Alertas
- **Fase 9:** #39-#42 Scraping
- **Fase 10:** #43-#47 Validaciones
- **Fase 11:** #48-#51 Calidad de arquitectura

---

## Comandos Maven
```bash
mvn clean install              # primera vez o después de cambiar pom.xml
mvn spring-boot:run            # correr la app
mvn clean install -DskipTests  # instalar sin correr tests
```

---

## Colaboradores
- **piliorlando** (owner)
- **Mila** (collaborator)