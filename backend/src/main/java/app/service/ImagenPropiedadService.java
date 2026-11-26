package app.service;

import app.model.ImagenPropiedad;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.ImagenPropiedadRepository;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import app.storage.AlmacenamientoImagenes;
import app.storage.AlmacenamientoImagenesException;
import app.storage.ArchivoImagen;
import app.storage.ImagenAlmacenada;
import app.storage.ImagenesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

/**
 * Imágenes de propiedades (issue #59).
 *
 * Contiene las reglas de negocio (quién puede subir, qué archivos se aceptan, en qué orden quedan).
 * El lugar físico donde se guarda la imagen lo decide {@link AlmacenamientoImagenes}: este servicio
 * no sabe que existe Cloudinary.
 */
@Service
public class ImagenPropiedadService {

    private static final Logger log = LoggerFactory.getLogger(ImagenPropiedadService.class);

    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final ImagenPropiedadRepository imagenPropiedadRepository;
    private final AlmacenamientoImagenes almacenamiento;
    private final ImagenesProperties properties;

    public ImagenPropiedadService(PropiedadRepository propiedadRepository,
                                  UsuarioRepository usuarioRepository,
                                  ImagenPropiedadRepository imagenPropiedadRepository,
                                  AlmacenamientoImagenes almacenamiento,
                                  ImagenesProperties properties) {
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.imagenPropiedadRepository = imagenPropiedadRepository;
        this.almacenamiento = almacenamiento;
        this.properties = properties;
    }

    /**
     * Sube la imagen al almacenamiento y la registra como la última de la propiedad.
     *
     * No es @Transactional a propósito: la subida es una llamada de red y no conviene tener una
     * transacción abierta mientras dura. Si la imagen se sube pero falla el guardado en la base,
     * se intenta borrarla del almacenamiento para no dejar archivos huérfanos.
     */
    public ImagenPropiedad subir(Long propiedadId, MultipartFile archivo, String emailUsuario) {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!propiedad.esPublicadaPor(usuario)) {
            log.warn("Subida de imagen denegada: propiedadId={}, usuarioId={}", propiedadId, usuario.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el publicador de la propiedad puede subir imágenes");
        }

        validarArchivo(archivo);

        if (imagenPropiedadRepository.countByPropiedadId(propiedadId) >= properties.maximoPorPropiedad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La propiedad ya tiene el máximo de " + properties.maximoPorPropiedad() + " imágenes");
        }

        ImagenAlmacenada almacenada = subirAlAlmacenamiento(propiedadId, archivo);

        ImagenPropiedad imagen = new ImagenPropiedad();
        imagen.setPropiedad(propiedad);
        imagen.setUrl(almacenada.url());
        imagen.setIdentificadorExterno(almacenada.identificador());
        imagen.setOrden(imagenPropiedadRepository.findMaxOrdenByPropiedadId(propiedadId) + 1);

        try {
            ImagenPropiedad guardada = imagenPropiedadRepository.save(imagen);
            log.info("Imagen de propiedad guardada: id={}, propiedadId={}, orden={}",
                    guardada.getId(), propiedadId, guardada.getOrden());
            return guardada;
        } catch (RuntimeException e) {
            eliminarHuerfana(almacenada);
            throw e;
        }
    }

    /**
     * Quita una foto de la propiedad: la borra de la base y después del almacenamiento. Si el
     * almacenamiento falla, la foto ya no se muestra y solo queda huérfana allá.
     */
    public void eliminar(Long propiedadId, Long imagenId, String emailUsuario) {
        Propiedad propiedad = propiedadRepository.findById(propiedadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!propiedad.esPublicadaPor(usuario)) {
            log.warn("Eliminación de imagen denegada: propiedadId={}, usuarioId={}", propiedadId, usuario.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo el publicador de la propiedad puede quitar imágenes");
        }

        ImagenPropiedad imagen = imagenPropiedadRepository.findById(imagenId)
                .filter(i -> i.getPropiedad() != null && propiedadId.equals(i.getPropiedad().getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada"));

        imagenPropiedadRepository.delete(imagen);
        log.info("Imagen de propiedad eliminada: id={}, propiedadId={}", imagenId, propiedadId);

        if (imagen.getIdentificadorExterno() != null) {
            try {
                almacenamiento.eliminar(imagen.getIdentificadorExterno());
            } catch (RuntimeException e) {
                log.error("No se pudo eliminar la imagen del almacenamiento: identificador={}",
                        imagen.getIdentificadorExterno(), e);
            }
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen es obligatoria");
        }
        if (archivo.getSize() > properties.tamanioMaximoBytes()) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "La imagen supera el tamaño máximo de " + (properties.tamanioMaximoBytes() / (1024 * 1024)) + " MB");
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !properties.tiposPermitidos().contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Formato de imagen no permitido. Formatos aceptados: " + String.join(", ", properties.tiposPermitidos()));
        }
    }

    private ImagenAlmacenada subirAlAlmacenamiento(Long propiedadId, MultipartFile archivo) {
        try {
            ArchivoImagen imagen = new ArchivoImagen(archivo.getBytes(), archivo.getOriginalFilename(),
                    archivo.getContentType());
            return almacenamiento.subir(imagen, properties.carpetaBase() + "/propiedades/" + propiedadId);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen enviada", e);
        } catch (AlmacenamientoImagenesException e) {
            log.error("Error al subir imagen: propiedadId={}", propiedadId, e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se pudo subir la imagen. Intentá nuevamente más tarde", e);
        }
    }

    private void eliminarHuerfana(ImagenAlmacenada almacenada) {
        try {
            almacenamiento.eliminar(almacenada.identificador());
        } catch (RuntimeException e) {
            log.error("No se pudo eliminar la imagen huérfana: identificador={}", almacenada.identificador(), e);
        }
    }
}
