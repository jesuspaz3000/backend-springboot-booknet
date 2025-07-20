package com.booknet.backend.controller;

import com.booknet.backend.dto.CreateUserRequest;
import com.booknet.backend.dto.PaginatedUsersResponse;
import com.booknet.backend.dto.UpdateUserRequest;
import com.booknet.backend.dto.UserResponse;
import com.booknet.backend.model.User;
import com.booknet.backend.service.UserService;
import com.booknet.backend.util.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users - Listar usuarios con paginación
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // Validar parámetros
            if (offset < 0) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("El offset debe ser mayor o igual a 0"));
            }
            
            if (limit <= 0) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("El limit debe ser mayor a 0"));
            }
            
            if (limit > 100) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("El limit máximo es 100"));
            }

            // Obtener usuarios paginados
            List<User> users = userService.getAllUsersWithPagination(offset, limit);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            // Obtener total de usuarios para metadatos de paginación
            long totalUsers = userService.getTotalUsersCount();

            // Crear respuesta paginada
            PaginatedUsersResponse paginatedResponse = new PaginatedUsersResponse(
                    userResponses, totalUsers, offset, limit);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Usuarios obtenidos exitosamente", paginatedResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse("Error al obtener usuarios: " + e.getMessage()));
        }
    }

    // GET /api/users/{userId} - Obtener usuario por ID
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            User user = userService.getUserById(userId);
            UserResponse userResponse = new UserResponse(user);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Usuario obtenido exitosamente", userResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseUtil.createErrorResponse(e.getMessage()));
        }
    }

    // POST /api/users - Crear nuevo usuario
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
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

            // Validar rol si se proporciona
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                if (!request.getRole().equals("USER") && !request.getRole().equals("ADMIN")) {
                    return ResponseEntity.badRequest()
                            .body(ResponseUtil.createErrorResponse("Rol debe ser USER o ADMIN"));
                }
            }

            User createdUser = userService.createUser(
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole()
            );

            UserResponse userResponse = new UserResponse(createdUser);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ResponseUtil.createSuccessResponse("Usuario creado exitosamente", userResponse));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse(e.getMessage()));
        }
    }

    // PATCH /api/users/{userId} - Actualizar usuario parcialmente
    @PatchMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody UpdateUserRequest request) {
        try {
            // Validar email si se proporciona
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    return ResponseEntity.badRequest()
                            .body(ResponseUtil.createErrorResponse("Formato de email inválido"));
                }
            }

            // Validar password si se proporciona
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                if (request.getPassword().length() < 6) {
                    return ResponseEntity.badRequest()
                            .body(ResponseUtil.createErrorResponse("La contraseña debe tener al menos 6 caracteres"));
                }
            }

            // Validar rol si se proporciona
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                if (!request.getRole().equals("USER") && !request.getRole().equals("ADMIN")) {
                    return ResponseEntity.badRequest()
                            .body(ResponseUtil.createErrorResponse("Rol debe ser USER o ADMIN"));
                }
            }

            User updatedUser = userService.updateUser(
                    userId,
                    request.getEmail(),
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole()
            );

            UserResponse userResponse = new UserResponse(updatedUser);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Usuario actualizado exitosamente", userResponse));

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseUtil.createErrorResponse(e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse(e.getMessage()));
        }
    }

    // DELETE /api/users/{userId} - Eliminar usuario
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        try {
            userService.deleteUser(userId);

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Usuario eliminado exitosamente", null));

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseUtil.createErrorResponse(e.getMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse("Error al eliminar usuario: " + e.getMessage()));
        }
    }

    // GET /api/users/role/{role} - Obtener usuarios por rol
    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role) {
        try {
            if (!role.equals("USER") && !role.equals("ADMIN")) {
                return ResponseEntity.badRequest()
                        .body(ResponseUtil.createErrorResponse("Rol debe ser USER o ADMIN"));
            }

            List<User> users = userService.getUsersByRole(role);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok()
                    .body(ResponseUtil.createSuccessResponse("Usuarios obtenidos exitosamente", userResponses));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseUtil.createErrorResponse("Error al obtener usuarios: " + e.getMessage()));
        }
    }
}