package app.service;

import app.controller.dto.ContactoPublicadorResponse;
import app.controller.dto.PropiedadDetalleResponse;
import app.controller.dto.PropiedadRequest;
import app.model.Fuente;
import app.model.Propiedad;
import app.model.Usuario;
import app.repository.PropiedadRepository;
import app.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class PropiedadService {

    private final PropiedadRepository propiedadRepository;
    private final UsuarioRepository usuarioRepository;

    public PropiedadService(PropiedadRepository propiedadRepository,
                            UsuarioRepository usuarioRepository) {
        this.propiedadRepository = propiedadRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Propiedad publicar(PropiedadRequest request, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Propiedad propiedad = new Propiedad();
        propiedad.setTitulo(request.getTitulo());
        propiedad.setDescripcion(request.getDescripcion());
        propiedad.setPrecio(request.getPrecio());
        propiedad.setExpensas(request.getExpensas());
        propiedad.setMoneda(request.getMoneda());
        propiedad.setCalle(request.getCalle());
        propiedad.setAltura(request.getAltura());
        propiedad.setPiso(request.getPiso());
        propiedad.setHabitaciones(request.getHabitaciones());
        propiedad.setMetrosCuadrados(request.getMetrosCuadrados());
        propiedad.setIncluyeImpuestos(request.getIncluyeImpuestos());
        propiedad.setPlazoActualizacionMeses(request.getPlazoActualizacionMeses());
        propiedad.setRequisitosIngreso(request.getRequisitosIngreso());
        propiedad.setFuente(Fuente.MANUAL);
        propiedad.setUsuario(usuario);

        return propiedadRepository.save(propiedad);
    }

    // ── Detalle de propiedad ──────────────────────────────────────────────────

    public PropiedadDetalleResponse getDetalle(Long id, String emailAutenticado) {
        Propiedad propiedad = propiedadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));

        ContactoPublicadorResponse contacto = resolverContacto(propiedad, emailAutenticado);

        return PropiedadDetalleResponse.from(propiedad, contacto);
    }

    private ContactoPublicadorResponse resolverContacto(Propiedad propiedad, String emailAutenticado) {
        if (emailAutenticado == null) return null;

        Usuario solicitante = usuarioRepository.findByEmail(emailAutenticado).orElse(null);

        if (solicitante == null || !solicitante.isVerificado()) return null;

        if (propiedad.getInmobiliaria() != null) {
            return ContactoPublicadorResponse.fromInmobiliaria(propiedad.getInmobiliaria());
        }

        if (propiedad.getUsuario() != null) {
            return ContactoPublicadorResponse.fromUsuario(propiedad.getUsuario());
        }

        return null;
    }
}