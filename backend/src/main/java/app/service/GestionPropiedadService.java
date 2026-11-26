package app.service;

import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.EstadoPropiedad;
import app.model.ImagenPropiedad;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.FavoritoPropiedadRepository;
import app.repository.PropiedadRepository;
import app.repository.SolicitudVisitaRepository;
import app.repository.UsuarioRepository;
import app.storage.AlmacenamientoImagenes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * Gestión de una propiedad ya publicada: editarla, marcarla como alquilada y eliminarla.
 *
 * Solo puede hacerlo quien la publicó (o, si es de una inmobiliaria, sus miembros): mismo criterio
 * que {@link Propiedad#esPublicadaPor}. Los métodos devuelven el DTO armado dentro de la transacción
 * porque recorre la colección lazy de imágenes.
 */
@Service
public class GestionPropiedadService {

    private static final Logger log = LoggerFactory.getLogger(GestionPropiedadService.class);

    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;
    private final FavoritoPropiedadRepository favoritoPropiedadRepository;
    private final SolicitudVisitaRepository solicitudVisitaRepository;
    private final AlmacenamientoImagenes almacenamiento;

    public GestionPropiedadService(PropiedadRepository propiedadRepository,
                                   UsuarioRepository usuarioRepository,
                                   FavoritoPropiedadRepository favoritoPropiedadRepository,
                                   SolicitudVisitaRepository solicitudVisitaRepository,
                                   AlmacenamientoImagenes almacenamiento) {
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
        this.favoritoPropiedadRepository = favoritoPropiedadRepository;
        this.solicitudVisitaRepository = solicitudVisitaRepository;
        this.almacenamiento = almacenamiento;
    }

    /** Reemplaza los datos de la propiedad por los del formulario. No cambia el estado ni las fotos. */
    @Transactional
    public PropiedadDetalleResponse editar(Long id, PropiedadRequest request, String emailUsuario) {
        Propiedad propiedad = buscarGestionable(id, emailUsuario, "editar");

        // Se acepta la fecha que ya tenía aunque haya pasado (issue #154)
        request.validarDisponibleDesde(propiedad.getDisponibleDesde(), Propiedad.hoy());
        request.aplicarA(propiedad);
        Propiedad guardada = propiedadRepository.save(propiedad);
        log.info("Propiedad editada: id={}", id);

        return PropiedadDetalleResponse.paraPublicador(guardada);
    }

    /**
     * Marca la propiedad como alquilada (deja de listarse y no recibe más pedidos de visita) o la
     * vuelve a poner disponible.
     */
    @Transactional
    public PropiedadDetalleResponse cambiarEstado(Long id, EstadoPropiedad estado, String emailUsuario) {
        if (estado == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado es obligatorio");
        }

        Propiedad propiedad = buscarGestionable(id, emailUsuario, "cambiar el estado de");

        EstadoPropiedad anterior = propiedad.getEstado();
        propiedad.setEstado(estado);
        Propiedad guardada = propiedadRepository.save(propiedad);
        log.info("Estado de propiedad actualizado: id={}, {} -> {}", id, anterior, estado);

        return PropiedadDetalleResponse.paraPublicador(guardada);
    }

    /**
     * Elimina la propiedad junto con sus fotos, los favoritos que la guardaban y sus pedidos de visita.
     * Las fotos se borran del almacenamiento recién cuando la base confirmó la eliminación: si algo
     * falla antes, la propiedad sigue con sus fotos.
     */
    @Transactional
    public void eliminar(Long id, String emailUsuario) {
        Propiedad propiedad = buscarGestionable(id, emailUsuario, "eliminar");

        List<String> fotos = propiedad.getImagenes() == null ? List.of() : propiedad.getImagenes().stream()
                .map(ImagenPropiedad::getIdentificadorExterno)
                .filter(Objects::nonNull)
                .toList();

        int favoritos = favoritoPropiedadRepository.deleteByPropiedadId(id);
        int visitas = solicitudVisitaRepository.deleteByPropiedadId(id);
        // Las filas de imagen_propiedad se borran en cascada con la propiedad
        propiedadRepository.delete(propiedad);
        log.info("Propiedad eliminada: id={}, favoritos={}, visitas={}, fotos={}", id, favoritos, visitas, fotos.size());

        despuesDeConfirmar(() -> fotos.forEach(this::eliminarDelAlmacenamiento));
    }

    private Propiedad buscarGestionable(Long id, String emailUsuario, String accion) {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Propiedad no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!propiedad.esPublicadaPor(usuario)) {
            log.warn("Gestión de propiedad denegada: accion={}, propiedadId={}, usuarioId={}", accion, id, usuario.getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo quien publicó la propiedad puede " + accion + " la propiedad");
        }
        return propiedad;
    }

    /** Si no se pudo borrar una foto del almacenamiento, queda huérfana allá pero no afecta a la app. */
    private void eliminarDelAlmacenamiento(String identificador) {
        try {
            almacenamiento.eliminar(identificador);
        } catch (RuntimeException e) {
            log.error("No se pudo eliminar la foto del almacenamiento: identificador={}", identificador, e);
        }
    }

    /** Corre la acción cuando se confirma la transacción actual, o en el momento si no hay transacción. */
    private static void despuesDeConfirmar(Runnable accion) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    accion.run();
                }
            });
        } else {
            accion.run();
        }
    }
}
