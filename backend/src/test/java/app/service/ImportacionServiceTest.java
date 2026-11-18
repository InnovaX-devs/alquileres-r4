package app.service;

import app.controller.dto.ImportacionResponse;
import app.model.Fuente;
import app.model.FuenteExterna;
import app.model.Propiedad;
import app.repository.FuenteExternaRepository;
import app.repository.PropiedadRepository;
import app.scraping.ExtractorSimulado;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ImportacionServiceTest {

    private final FuenteExternaRepository fuenteRepo = mock(FuenteExternaRepository.class);
    private final PropiedadRepository propiedadRepo = mock(PropiedadRepository.class);
    private final ImportacionService service = new ImportacionService(
            fuenteRepo, propiedadRepo, new ScrapingService(List.of(new ExtractorSimulado())));

    @Test
    @SuppressWarnings("unchecked")
    void guardaLasPropiedadesNuevasComoWebYOmiteLasYaImportadas() {
        when(fuenteRepo.findById(1L)).thenReturn(Optional.of(new FuenteExterna("Simulada", "simulado://portal")));
        when(propiedadRepo.existsByUrlOriginal(anyString())).thenReturn(false);
        when(propiedadRepo.existsByUrlOriginal("simulado://portal/propiedades/2")).thenReturn(true);

        ImportacionResponse res = service.importar(1L);

        assertEquals(2, res.importadas());
        assertEquals(1, res.omitidas());

        ArgumentCaptor<List<Propiedad>> captor = ArgumentCaptor.forClass(List.class);
        verify(propiedadRepo).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertTrue(captor.getValue().stream().allMatch(p -> p.getFuente() == Fuente.WEB));
    }

    @Test
    void fallaSiLaFuenteNoExiste() {
        when(fuenteRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> service.importar(99L));
        verify(propiedadRepo, never()).saveAll(any());
    }
}
