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
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
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
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        try {
            // Validaciones básicas
            if (request == null) {
                return ResponseUtil.createErrorResponse("Datos de registro son requeridos", 400);
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El nombre de usuario es requerido", 400);
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El email es requerido", 400);
            }

            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseUtil.createErrorResponse("Formato de email inválido", 400);
            }

            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseUtil.createErrorResponse("La contraseña debe tener al menos 6 caracteres", 400);
            }

            // Registrar usuario
            LoginResponse response = authService.register(request);

            return ResponseEntity.status(201)
                    .body(ResponseUtil.createSuccessResponseMap("Usuario registrado exitosamente", response));

        } catch (Exception e) {
            if (e.getMessage().contains("ya existe") || e.getMessage().contains("duplicado")) {
                return ResponseUtil.createErrorResponse("El usuario o email ya existe", 409);
            }
            return ResponseUtil.createErrorResponse("Error al registrar usuario: " + e.getMessage(), 400);
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            // Validaciones básicas
            if (request == null) {
                return ResponseUtil.createErrorResponse("Datos de login son requeridos", 400);
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El nombre de usuario es requerido", 400);
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("La contraseña es requerida", 400);
            }

            // Hacer login
            LoginResponse response = authService.login(request);

            return ResponseUtil.createSuccessResponse(response, "Login exitoso");

        } catch (Exception e) {
            if (e.getMessage().contains("credenciales") || e.getMessage().contains("password") || e.getMessage().contains("usuario")) {
                return ResponseUtil.createErrorResponse("Credenciales inválidas", 401);
            }
            return ResponseUtil.createErrorResponse("Error al iniciar sesión: " + e.getMessage(), 500);
        }
    }

    // POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            if (request == null) {
                return ResponseUtil.createErrorResponse("Datos de refresh son requeridos", 400);
            }

            String refreshToken = request.get("refresh_token");

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("Refresh token es requerido", 400);
            }

            // Renovar token
            LoginResponse response = authService.refreshToken(refreshToken.trim());

            return ResponseUtil.createSuccessResponse(response, "Token renovado exitosamente");

        } catch (Exception e) {
            if (e.getMessage().contains("expirado") || e.getMessage().contains("inválido")) {
                return ResponseUtil.createErrorResponse("Refresh token inválido o expirado", 401);
            }
            return ResponseUtil.createErrorResponse("Error al renovar token: " + e.getMessage(), 500);
        }
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> request) {
        try {
            if (request == null) {
                return ResponseUtil.createErrorResponse("Datos de logout son requeridos", 400);
            }

            String refreshToken = request.get("refresh_token");

            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("Refresh token es requerido", 400);
            }

            // Invalidar refresh token
            authService.invalidateRefreshToken(refreshToken.trim());

            return ResponseUtil.createSuccessResponse(null, "Logout exitoso");

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al cerrar sesión: " + e.getMessage(), 500);
        }
    }

    // POST /api/auth/verify-token
    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestBody Map<String, String> request) {
        try {
            if (request == null) {
                return ResponseUtil.createErrorResponse("Datos de verificación son requeridos", 400);
            }

            String accessToken = request.get("access_token");

            if (accessToken == null || accessToken.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("Access token es requerido", 400);
            }

            // Verificar si el token es válido y no ha expirado
            boolean isTokenAlive = false;
            
            try {
                // Verificar que sea un access token
                if (jwtService.isAccessToken(accessToken.trim())) {
                    // Extraer username del token
                    String username = jwtService.extractUsername(accessToken.trim());
                    // Validar el token (verifica firma y expiración)
                    isTokenAlive = jwtService.validateToken(accessToken.trim(), username);
                }
            } catch (Exception e) {
                // Si hay cualquier error al procesar el token, está muerto
                isTokenAlive = false;
            }

            Map<String, Object> responseData = Map.of("isAlive", isTokenAlive);
            
            return ResponseUtil.createSuccessResponse(responseData, "Verificación de token completada");

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al verificar token: " + e.getMessage(), 500);
        }
    }
}