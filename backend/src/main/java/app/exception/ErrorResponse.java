package app.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Cuerpo estándar de error. Mantiene el mismo formato que usa SecurityConfig para 401/403:
 * {"status": 404, "error": "mensaje"}. El campo "detalles" solo aparece en errores de validación.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(int status, String error, List<String> detalles) {

    public ErrorResponse(int status, String error) {
        this(status, error, null);
    }
}
