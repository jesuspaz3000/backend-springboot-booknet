package com.booknet.backend.controller;

import com.booknet.backend.dto.UpdateProfileRequest;
import com.booknet.backend.dto.UserResponse;
import com.booknet.backend.model.User;
import com.booknet.backend.service.JwtService;
import com.booknet.backend.service.UserService;
import com.booknet.backend.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.PATCH, RequestMethod.OPTIONS})
public class ProfileController {

    private final UserService userService;
    private final JwtService jwtService;

    public ProfileController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // GET /api/profile - Obtener perfil del usuario autenticado
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.createErrorResponse("Token de autorización requerido", 401);
            }

            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            User user = userService.getUserById(userId);
            UserResponse userResponse = new UserResponse(user);

            return ResponseUtil.createSuccessResponse(userResponse, "Perfil obtenido exitosamente");

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener perfil: " + e.getMessage(), 500);
        }
    }

    // PATCH /api/profile - Actualizar perfil del usuario autenticado
    @PatchMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody UpdateProfileRequest request) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.createErrorResponse("Token de autorización requerido", 401);
            }

            if (request == null) {
                return ResponseUtil.createErrorResponse("Datos de actualización son requeridos", 400);
            }

            // Validar que se proporcione la contraseña actual
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("La contraseña actual es requerida", 400);
            }

            // Validar que al menos un campo nuevo se proporcione
            if ((request.getNewEmail() == null || request.getNewEmail().trim().isEmpty()) &&
                (request.getNewUsername() == null || request.getNewUsername().trim().isEmpty()) &&
                (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty())) {
                return ResponseUtil.createErrorResponse("Debe proporcionar al menos un campo para actualizar", 400);
            }

            // Validar formato de email si se proporciona
            if (request.getNewEmail() != null && !request.getNewEmail().trim().isEmpty()) {
                if (!request.getNewEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    return ResponseUtil.createErrorResponse("Formato de email inválido", 400);
                }
            }

            // Validar nueva contraseña si se proporciona
            if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
                if (request.getNewPassword().length() < 6) {
                    return ResponseUtil.createErrorResponse("La nueva contraseña debe tener al menos 6 caracteres", 400);
                }
            }

            // Obtener ID del usuario del token
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);

            // Actualizar perfil
            User updatedUser = userService.updateUserProfile(
                    userId,
                    request.getCurrentPassword(),
                    request.getNewEmail() != null ? request.getNewEmail().trim() : null,
                    request.getNewUsername() != null ? request.getNewUsername().trim() : null,
                    request.getNewPassword()
            );

            UserResponse userResponse = new UserResponse(updatedUser);

            return ResponseUtil.createSuccessResponse(userResponse, "Perfil actualizado exitosamente");

        } catch (Exception e) {
            if (e.getMessage().contains("contraseña incorrecta") || e.getMessage().contains("password")) {
                return ResponseUtil.createErrorResponse("Contraseña actual incorrecta", 401);
            }
            if (e.getMessage().contains("no encontrado")) {
                return ResponseUtil.createErrorResponse("Usuario no encontrado", 404);
            }
            return ResponseUtil.createErrorResponse("Error al actualizar perfil: " + e.getMessage(), 400);
        }
    }
}
