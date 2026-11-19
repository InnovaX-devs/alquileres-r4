package app.controller;

import app.controller.dto.ApiResponse;
import app.controller.dto.AuthResponse;
import app.controller.dto.LoginRequest;
import app.controller.dto.RegisterRequest;
import app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.created("Usuario registrado correctamente", null);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ApiResponse.ok("Login exitoso", response);
    }
}