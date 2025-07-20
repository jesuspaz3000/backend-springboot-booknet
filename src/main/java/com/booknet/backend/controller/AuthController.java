package com.booknet.backend.controller;

import com.booknet.backend.dto.LoginRequest;
import com.booknet.backend.dto.LoginResponse;
import com.booknet.backend.dto.RegisterRequest;
import com.booknet.backend.service.AuthService;
import com.booknet.backend.service.JwtService;
import com.booknet.backend.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    // Constructor injection (recomendado)
    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {

            // Validaciones básicas
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("El nombre de usuario es requerido"));
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("El email es requerido"));
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("La contraseña debe tener al menos 6 caracteres"));
            }

            // Registrar usuario
            LoginResponse response = authService.register(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseUtil.createSuccessResponse("Usuario registrado exitosamente", response));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse(e.getMessage()));
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {

            // Validaciones básicas
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("El nombre de usuario es requerido"));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("La contraseña es requerida"));
            }

            // Hacer login
            LoginResponse response = authService.login(request);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Login exitoso", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseUtil.createErrorResponse(e.getMessage()));
        }
    }

    // POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {

            String refreshToken = request.get("refresh_token");

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("Refresh token es requerido"));
            }

            // Renovar token
            LoginResponse response = authService.refreshToken(refreshToken);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Token renovado exitosamente", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ResponseUtil.createErrorResponse(e.getMessage()));
        }
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("Refresh token es requerido"));
            }

            // Invalidar refresh token
            authService.invalidateRefreshToken(refreshToken);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Logout exitoso", null));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse("Error al cerrar sesión: " + e.getMessage()));
        }
    }

    // POST /api/auth/verify-token
    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> request) {
        try {
            String accessToken = request.get("access_token");

            if (accessToken == null || accessToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("Access token es requerido"));
            }

            // Verificar si el token es válido y no ha expirado
            boolean isTokenAlive = false;
            
            try {
                // Verificar que sea un access token
                if (jwtService.isAccessToken(accessToken)) {
                    // Extraer username del token
                    String username = jwtService.extractUsername(accessToken);
                    // Validar el token (verifica firma y expiración)
                    isTokenAlive = jwtService.validateToken(accessToken, username);
                }
            } catch (Exception e) {
                // Si hay cualquier error al procesar el token, está muerto
                isTokenAlive = false;
            }

            Map<String, Object> responseData = Map.of("isAlive", isTokenAlive);
            
            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Verificación de token completada", responseData));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse("Error al verificar token: " + e.getMessage()));
        }
    }
}