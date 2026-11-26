# Deploy (issue #90)

- **Backend:** Render (plan Free, con Docker)
- **Frontend:** Vercel (plan Hobby)
- **Base de datos:** PostgreSQL en Supabase

Cada lado necesita la URL del otro, así que hay que hacerlo en este orden.

> El plan Free de Render apaga el backend tras 15 minutos sin uso. La primera
> petición después de eso tarda alrededor de un minuto. Antes de una demo,
> abrí la página un par de minutos antes.

---

## 1. Base de datos (Supabase)

1. En el proyecto de Supabase, tocá **Connect** y elegí **Session pooler**.
   No uses la conexión directa: es solo IPv6 y Render no llega.
2. Tomá de ahí el host, el puerto (5432) y el usuario (`postgres.<id-del-proyecto>`).
3. Armá la URL JDBC:
   ```
   jdbc:postgresql://<host-del-pooler>:5432/postgres?sslmode=require
   ```

Las tablas se crean solas al arrancar el backend (`ddl-auto=update`).

## 2. Backend en Render

**New → Web Service** → repo `InnovaX-devs/alquileres-r4`.

| Campo | Valor |
|---|---|
| Language | **Docker** |
| Branch | `main` |
| Root Directory | `backend` |
| Dockerfile Path | `backend/Dockerfile` (o `./Dockerfile` si ya toma la carpeta `backend`) |
| Instance Type | Free |

**Environment Variables:**

| Variable | Valor |
|---|---|
| `DB_URL` | la URL JDBC del paso 1 |
| `DB_USERNAME` | `postgres.<id-del-proyecto>` |
| `DB_PASSWORD` | contraseña de la base |
| `JWT_SECRET` | una clave larga y nueva (por ejemplo, la salida de `openssl rand -base64 48`) |
| `MAIL_USERNAME` | el Gmail que manda los avisos |
| `MAIL_PASSWORD` | la contraseña de aplicación de ese Gmail |
| `CLOUDINARY_CLOUD_NAME` | de la cuenta de Cloudinary |
| `CLOUDINARY_API_KEY` | ídem |
| `CLOUDINARY_API_SECRET` | ídem |
| `CORS_ALLOWED_ORIGINS` | por ahora `http://localhost:5173` (se cambia en el paso 4) |
| `FRONTEND_URL` | por ahora `http://localhost:5173` (se cambia en el paso 4) |

**Deploy.** El primer build tarda varios minutos. Cuando termine, la URL queda
como `https://<nombre>.onrender.com`. Para comprobarla, abrí
`https://<nombre>.onrender.com/propiedades`: tiene que devolver JSON.

## 3. Frontend en Vercel

**Add New → Project** → importar `InnovaX-devs/alquileres-r4`.

| Campo | Valor |
|---|---|
| Root Directory | `frontend` |
| Framework Preset | Vite (lo detecta solo) |
| Environment Variable | `VITE_API_URL` = `https://<nombre>.onrender.com` (sin barra al final) |

**Deploy.** Queda en `https://<proyecto>.vercel.app`.

`VITE_API_URL` se graba al compilar. Si después la cambiás, hay que hacer
**Redeploy** en Vercel.

## 4. Habilitar el front en el CORS del backend

En Render → **Environment**, cambiá `CORS_ALLOWED_ORIGINS` a:

```
https://<proyecto>.vercel.app,https://<proyecto>-*.vercel.app
```

El segundo valor habilita las previews de los PR.

En la misma pantalla cambiá `FRONTEND_URL` a `https://<proyecto>.vercel.app` (sin barra
final). La usa el link para compartir por WhatsApp: redirige ahí al detalle de la
propiedad y toma de ahí la imagen genérica de la vista previa.

Guardá: Render redeploya solo.

## 5. Probar

Abrí `https://<proyecto>.vercel.app` y probá registrarte, iniciar sesión,
publicar una propiedad con foto y ubicación, y recargar en `/propiedades/<id>`
(no tiene que dar 404).

Si el navegador muestra un error de CORS, revisá que la URL de Vercel esté
escrita igual en `CORS_ALLOWED_ORIGINS`: con `https` y sin barra final.

---

A partir de acá, cada merge a `main` vuelve a desplegar los dos solos.
