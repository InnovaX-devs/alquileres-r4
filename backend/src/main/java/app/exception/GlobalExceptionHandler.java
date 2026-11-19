package app.exception;

import app.controller.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Manejo global de excepciones (issue #50).
 * Todas las respuestas de error salen como {@link ApiResponse} con data null,
 * salvo las validaciones, que llevan en data el detalle por campo (issue #51).
 *
 * Extiende ResponseEntityExceptionHandler para cubrir también las excepciones estándar de
 * Spring MVC (405, 415, 404 de ruta inexistente, tipos de parámetro inválidos, etc.)
 * y evitar que caigan en el handler genérico como 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Punto común: ResponseStatusException y las excepciones estándar de Spring MVC. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        return ResponseEntity.status(statusCode)
                .headers(headers)
                .body(new ApiResponse<>(statusCode.value(), mensajePara(ex, statusCode), null));
    }

    /** @Valid sobre @RequestBody: devuelve el detalle por campo (lo usarán las validaciones de la Fase 10). */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), "Datos inválidos", detalles));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(status.value(), "El cuerpo de la solicitud es inválido o está mal formado", null));
    }

    /** @Validated sobre parámetros de path/query. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<List<String>>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> detalles = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .toList();
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, "Datos inválidos", detalles));
    }

    /** Violación de unique / FK que no fue atrapada en el service. No se filtra el detalle de la base. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(409, "La operación viola una restricción de integridad de datos", null));
    }

    /**
     * Las excepciones de seguridad se relanzan para que las resuelva Spring Security
     * (authenticationEntryPoint -> 401, accessDeniedHandler -> 403, definidos en SecurityConfig).
     * Sin esto, el handler genérico de abajo las convertiría en un 500.
     */
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    public void relanzarExcepcionDeSeguridad(RuntimeException ex) {
        throw ex;
    }

    /** Último recurso: error no previsto. Se loguea con stacktrace y no se expone el detalle al cliente. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(500, "Error interno del servidor", null));
    }

    private static String mensajePara(Exception ex, HttpStatusCode status) {
        if (ex instanceof ResponseStatusException rse && rse.getReason() != null) {
            return rse.getReason();
        }
        return switch (status.value()) {
            case 400 -> "Solicitud inválida";
            case 404 -> "Recurso no encontrado";
            case 405 -> "Método HTTP no permitido";
            case 406 -> "Formato de respuesta no aceptable";
            case 415 -> "Tipo de contenido no soportado";
            default -> status.is5xxServerError() ? "Error interno del servidor" : "Error en la solicitud";
        };
    }
}
