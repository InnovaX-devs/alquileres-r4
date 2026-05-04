# Fase 1 — Base del proyecto

Este archivo explica qué se hizo en cada issue de la Fase 1, por qué se hizo y qué rol cumple en el proyecto.

---

## #1 — Crear estructura de paquetes

Lo primero que se hizo fue crear las carpetas base del proyecto dentro de `src/main/java/app/`:

- `config/` → configuraciones técnicas de la app
- `controller/` → recibe las requests HTTP y devuelve responses
- `model/` → las entidades que se mapean a tablas en la DB
- `repository/` → acceso a la base de datos
- `service/` → la lógica de negocio

Esta separación se llama **arquitectura en capas** y es el estándar en Spring Boot. Cada capa tiene una responsabilidad clara y no se mezclan. Por ejemplo, el controller nunca habla directamente con la DB — siempre pasa por el service, y el service usa el repository.

---

## #2 — Configurar conexión a base de datos

Se configuró el `application.properties` con los datos de Supabase para que Spring Boot sepa a qué base de datos conectarse. Las líneas clave son:

```properties
spring.datasource.url=jdbc:postgresql://...
spring.datasource.username=postgres
spring.datasource.password=...
spring.datasource.driver-class-name=org.postgresql.Driver
```

El **driver** es el puente entre Java y PostgreSQL — sin él la app no sabe cómo hablar con la DB. Spring Boot usa **HikariCP** como pool de conexiones, que es una biblioteca que administra un conjunto de conexiones reutilizables para no abrir y cerrar una conexión nueva en cada request.

> ⚠️ El `application.properties` está en `.gitignore` y nunca se sube al repo. Para configurarlo localmente seguí los pasos del README.

---

## #3 — Configurar JPA / Hibernate

JPA es una especificación de Java que define cómo mapear clases Java a tablas de una base de datos relacional. Hibernate es la implementación concreta de esa especificación.

Las líneas importantes en el `application.properties`:

```properties
spring.jpa.hibernate.ddl-auto=update
```
Le dice a Hibernate que administre las tablas automáticamente. Si una entidad no tiene tabla, la crea. Si se agrega un campo nuevo, agrega la columna. Nunca borra datos.

```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```
Muestra en la consola el SQL que Hibernate ejecuta, formateado y legible. Útil para debuggear.

```properties
spring.jpa.open-in-view=false
```
Cierra la conexión a la DB en cuanto termina la consulta. Es la práctica correcta — mantenerla abierta innecesariamente desperdicia recursos.

---

## #4 — Configurar CORS global

CORS (Cross-Origin Resource Sharing) es un mecanismo de seguridad del navegador que bloquea requests que vienen de un dominio distinto al del servidor.

Sin CORS configurado, cuando el frontend React en `localhost:5173` intente hacer una request al backend en `localhost:8080`, el navegador la bloquea automáticamente porque son puertos distintos — y para el navegador, eso cuenta como dominio distinto.

El archivo `CorsConfig.java` configura:
- **Origen permitido:** `http://localhost:5173` (el dev server de React)
- **Métodos permitidos:** GET, POST, PUT, DELETE, OPTIONS
- **Headers permitidos:** todos (`*`) para que el token JWT pueda viajar en el header `Authorization`
- **Credenciales:** habilitadas para permitir autenticación

> ⚠️ Cuando se deploye en producción hay que agregar la URL real del frontend a los orígenes permitidos en `CorsConfig.java`.

---

## #5 — Configurar PasswordEncoder

Se configuró BCrypt como algoritmo para encriptar contraseñas en `PasswordEncoderConfig.java`. BCrypt tiene dos características clave:

**Es de un solo sentido** — convertís "miPassword123" en `$2a$10$xK8Qz...` pero nunca podés hacer el camino inverso. Nadie puede recuperar la contraseña original a partir del hash.

**Es lento a propósito** — está diseñado para ser computacionalmente costoso, lo que hace que los ataques de fuerza bruta sean imprácticos.

El flujo en la app es:
- **Registro:** la contraseña se encripta con BCrypt antes de guardarse en la DB
- **Login:** la contraseña ingresada se encripta y se compara con el hash guardado

La contraseña original **nunca** se guarda ni se ve en ningún momento, ni en la DB ni en los logs.

---

## Resumen general

La Fase 1 es la **infraestructura invisible** de la app — no hay ninguna feature visible para el usuario todavía, pero sin esto nada de lo que viene después funcionaría:

| Issue | Qué resuelve |
|-------|-------------|
| #1 | Organización y estructura del código |
| #2 | Conexión a la base de datos |
| #3 | Mapeo automático de clases a tablas |
| #4 | Comunicación entre frontend y backend |
| #5 | Seguridad de contraseñas |