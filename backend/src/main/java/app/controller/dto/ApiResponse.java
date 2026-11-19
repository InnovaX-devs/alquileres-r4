package app.controller.dto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Formato común de todas las respuestas HTTP (issue #51): {"status", "mensaje", "data"}.
 * En los errores, "data" es null salvo en las validaciones, donde lleva el detalle por campo.
 */
public record ApiResponse<T>(int status, String mensaje, T data) {

    public static <T> ResponseEntity<ApiResponse<T>> ok(String mensaje, T data) {
        return respuesta(HttpStatus.OK, mensaje, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String mensaje, T data) {
        return respuesta(HttpStatus.CREATED, mensaje, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> respuesta(HttpStatus status, String mensaje, T data) {
        return ResponseEntity.status(status).body(new ApiResponse<>(status.value(), mensaje, data));
    }
}
