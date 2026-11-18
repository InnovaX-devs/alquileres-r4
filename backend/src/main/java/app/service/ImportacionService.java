package app.service;

import app.controller.dto.ImportacionResponse;
import app.model.Fuente;
import app.model.FuenteExterna;
import app.model.Propiedad;
import app.repository.FuenteExternaRepository;
import app.repository.PropiedadRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ImportacionService {

    private final FuenteExternaRepository fuenteExternaRepository;
    private final PropiedadRepository propiedadRepository;
    private final ScrapingService scrapingService;

    public ImportacionService(FuenteExternaRepository fuenteExternaRepository,
                              PropiedadRepository propiedadRepository,
                              ScrapingService scrapingService) {
        this.fuenteExternaRepository = fuenteExternaRepository;
        this.propiedadRepository = propiedadRepository;
        this.scrapingService = scrapingService;
    }

    @Transactional
    public ImportacionResponse importar(Long fuenteId) {
        FuenteExterna fuente = fuenteExternaRepository.findById(fuenteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fuente externa no encontrada"));

        List<Propiedad> obtenidas;
        try {
            obtenidas = scrapingService.obtenerPropiedades(fuente);
        } catch (IllegalStateException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        // Se omiten las que ya fueron importadas (misma URL original), también dentro del mismo lote
        Set<String> urlsVistas = new HashSet<>();
        List<Propiedad> nuevas = obtenidas.stream()
                .filter(p -> p.getUrlOriginal() != null && !p.getUrlOriginal().isBlank())
                .filter(p -> urlsVistas.add(p.getUrlOriginal()))
                .filter(p -> !propiedadRepository.existsByUrlOriginal(p.getUrlOriginal()))
                .toList();

        nuevas.forEach(p -> p.setFuente(Fuente.WEB));
        propiedadRepository.saveAll(nuevas);

        return new ImportacionResponse(fuente.getNombre(), nuevas.size(), obtenidas.size() - nuevas.size());
    }
}
