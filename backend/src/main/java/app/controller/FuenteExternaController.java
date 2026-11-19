package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.ImportacionResponse;
import app.service.ImportacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fuentes-externas")
public class FuenteExternaController {

    private final ImportacionService importacionService;

    public FuenteExternaController(ImportacionService importacionService) {
        this.importacionService = importacionService;
    }

    @PostMapping("/{id}/importar")
    public ResponseEntity<ApiResponse<ImportacionResponse>> importar(@PathVariable Long id) {
        return ApiResponse.ok("Importación finalizada", importacionService.importar(id));
    }
}
