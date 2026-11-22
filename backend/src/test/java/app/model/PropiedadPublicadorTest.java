package app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Issue #61: quién gestiona una propiedad.
 * Las publicadas a nombre de una inmobiliaria son de la inmobiliaria; las de un particular, de quien las creó.
 */
class PropiedadPublicadorTest {

    private static Inmobiliaria inmobiliaria(Long id) {
        Inmobiliaria i = new Inmobiliaria();
        i.setId(id);
        return i;
    }

    private static Usuario usuario(Long id, Inmobiliaria inmobiliaria) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setInmobiliaria(inmobiliaria);
        u.setRolInmobiliaria(inmobiliaria != null ? RolInmobiliaria.AGENTE : null);
        return u;
    }

    private static Propiedad propiedad(Usuario creador, Inmobiliaria inmobiliaria) {
        Propiedad p = new Propiedad();
        p.setUsuario(creador);
        p.setInmobiliaria(inmobiliaria);
        return p;
    }

    @Test
    void propiedadDeParticularLaGestionaSoloQuienLaCreo() {
        Usuario creador = usuario(1L, null);
        Propiedad p = propiedad(creador, null);

        assertTrue(p.esPublicadaPor(creador));
        assertFalse(p.esPublicadaPor(usuario(2L, null)));
    }

    @Test
    void propiedadDeParticularSigueSiendoDelCreadorAunqueDespuesSeUnaAUnaInmobiliaria() {
        Usuario creador = usuario(1L, null);
        Propiedad p = propiedad(creador, null);
        Inmobiliaria inmo = inmobiliaria(7L);

        creador.setInmobiliaria(inmo);

        assertTrue(p.esPublicadaPor(creador));
        assertFalse(p.esPublicadaPor(usuario(2L, inmo)), "los compañeros no ven sus propiedades personales");
    }

    @Test
    void propiedadDeInmobiliariaLaGestionaCualquierMiembro() {
        Inmobiliaria inmo = inmobiliaria(7L);
        Propiedad p = propiedad(usuario(1L, inmo), inmo);

        assertTrue(p.esPublicadaPor(usuario(2L, inmo)));
        assertFalse(p.esPublicadaPor(usuario(3L, inmobiliaria(9L))));
    }

    @Test
    void siDesvinculanAlCreadorPierdeAccesoALasPropiedadesDeLaInmobiliaria() {
        Inmobiliaria inmo = inmobiliaria(7L);
        Usuario creador = usuario(1L, inmo);
        Propiedad p = propiedad(creador, inmo);

        creador.setInmobiliaria(null);

        assertFalse(p.esPublicadaPor(creador));
    }
}
