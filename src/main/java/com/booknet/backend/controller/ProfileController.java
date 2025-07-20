package com.booknet.backend.controller;

import com.booknet.backend.dto.UpdateProfileRequest;
import com.booknet.backend.dto.UserResponse;
import com.booknet.backend.model.User;
import com.booknet.backend.service.JwtService;
import com.booknet.backend.service.UserService;
import com.booknet.backend.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final UserService userService;
    private final JwtService jwtService;

    public ProfileController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // GET /api/profile - Obtener perfil del usuario autenticado
    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            User user = userService.getUserById(userId);
            UserResponse userResponse = new UserResponse(user);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Perfil obtenido exitosamente", userResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse("Error al obtener perfil: " + e.getMessage()));
        }
    }

    // PATCH /api/profile - Actualizar perfil del usuario autenticado
    @PatchMapping
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
                                         @RequestBody UpdateProfileRequest request) {
        try {
            // Validar que se proporcione la contraseña actual
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("La contraseña actual es requerida"));
            }

            // Validar que al menos un campo nuevo se proporcione
            if ((request.getNewEmail() == null || request.getNewEmail().trim().isEmpty()) &&
                (request.getNewUsername() == null || request.getNewUsername().trim().isEmpty()) &&
                (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("Debe proporcionar al menos un campo para actualizar"));
            }

            // Validar formato de email si se proporciona
            if (request.getNewEmail() != null && !request.getNewEmail().trim().isEmpty()) {
                if (!request.getNewEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    return ResponseEntity.badRequest()
                            .body(ResponseUtil.createErrorResponse("Formato de email inválido"));
                }
            }

            // Validar nueva contraseña si se proporciona
            if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
                if (request.getNewPassword().length() < 6) {
                    return ResponseEntity.badRequest()
                            .body(ResponseUtil.createErrorResponse("La nueva contraseña debe tener al menos 6 caracteres"));
                }
            }

            // Obtener ID del usuario del token
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);

            // Actualizar perfil
            User updatedUser = userService.updateUserProfile(
                    userId,
                    request.getCurrentPassword(),
                    request.getNewEmail(),
                    request.getNewUsername(),
                    request.getNewPassword()
            );

            UserResponse userResponse = new UserResponse(updatedUser);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Perfil actualizado exitosamente", userResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse(e.getMessage()));
        }
    }
}
