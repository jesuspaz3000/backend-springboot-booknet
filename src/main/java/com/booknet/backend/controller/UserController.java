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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users - Listar usuarios con paginación
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // Validar parámetros
            if (offset < 0) {
                return ResponseUtil.createErrorResponse("El offset debe ser mayor o igual a 0", 400);
            }
            
            if (limit <= 0) {
                return ResponseUtil.createErrorResponse("El limit debe ser mayor a 0", 400);
            }
            
            if (limit > 100) {
                return ResponseUtil.createErrorResponse("El limit máximo es 100", 400);
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

            return ResponseUtil.createSuccessResponse(paginatedResponse, "Usuarios obtenidos exitosamente");

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener usuarios: " + e.getMessage(), 500);
        }
    }

    // GET /api/users/{userId} - Obtener usuario por ID
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("ID del usuario es requerido", 400);
            }

            User user = userService.getUserById(userId.trim());
            UserResponse userResponse = new UserResponse(user);

            return ResponseUtil.createSuccessResponse(userResponse, "Usuario obtenido exitosamente");

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseUtil.createErrorResponse(e.getMessage(), 404);
            }
            return ResponseUtil.createErrorResponse("Error al obtener usuario: " + e.getMessage(), 500);
        }
    }

    // POST /api/users - Crear nuevo usuario
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        try {
            // Validaciones básicas
            if (request == null) {
                return ResponseUtil.createErrorResponse("Datos del usuario son requeridos", 400);
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

            // Validar rol si se proporciona
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                if (!request.getRole().equals("USER") && !request.getRole().equals("ADMIN")) {
                    return ResponseUtil.createErrorResponse("Rol debe ser USER o ADMIN", 400);
                }
            }

            User createdUser = userService.createUser(
                    request.getEmail().trim(),
                    request.getUsername().trim(),
                    request.getPassword(),
                    request.getRole()
            );

            UserResponse userResponse = new UserResponse(createdUser);

            return ResponseEntity.status(201)
                    .body(ResponseUtil.createSuccessResponseMap("Usuario creado exitosamente", userResponse));

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al crear usuario: " + e.getMessage(), 400);
        }
    }

    // PATCH /api/users/{userId} - Actualizar usuario parcialmente
    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable String userId, @RequestBody UpdateUserRequest request) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("ID del usuario es requerido", 400);
            }

            if (request == null) {
                return ResponseUtil.createErrorResponse("Datos de actualización son requeridos", 400);
            }

            // Validar email si se proporciona
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    return ResponseUtil.createErrorResponse("Formato de email inválido", 400);
                }
            }

            // Validar password si se proporciona
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                if (request.getPassword().length() < 6) {
                    return ResponseUtil.createErrorResponse("La contraseña debe tener al menos 6 caracteres", 400);
                }
            }

            // Validar rol si se proporciona
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                if (!request.getRole().equals("USER") && !request.getRole().equals("ADMIN")) {
                    return ResponseUtil.createErrorResponse("Rol debe ser USER o ADMIN", 400);
                }
            }

            User updatedUser = userService.updateUser(
                    userId.trim(),
                    request.getEmail() != null ? request.getEmail().trim() : null,
                    request.getUsername() != null ? request.getUsername().trim() : null,
                    request.getPassword(),
                    request.getRole()
            );

            UserResponse userResponse = new UserResponse(updatedUser);

            return ResponseUtil.createSuccessResponse(userResponse, "Usuario actualizado exitosamente");

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseUtil.createErrorResponse(e.getMessage(), 404);
            }
            return ResponseUtil.createErrorResponse("Error al actualizar usuario: " + e.getMessage(), 400);
        }
    }

    // DELETE /api/users/{userId} - Eliminar usuario
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String userId) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("ID del usuario es requerido", 400);
            }

            userService.deleteUser(userId.trim());

            return ResponseUtil.createSuccessResponse(null, "Usuario eliminado exitosamente");

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseUtil.createErrorResponse(e.getMessage(), 404);
            }
            return ResponseUtil.createErrorResponse("Error al eliminar usuario: " + e.getMessage(), 500);
        }
    }

    // GET /api/users/role/{role} - Obtener usuarios por rol
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsersByRole(@PathVariable String role) {
        try {
            if (role == null || role.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("Rol es requerido", 400);
            }

            String cleanRole = role.trim().toUpperCase();
            if (!cleanRole.equals("USER") && !cleanRole.equals("ADMIN")) {
                return ResponseUtil.createErrorResponse("Rol debe ser USER o ADMIN", 400);
            }

            List<User> users = userService.getUsersByRole(cleanRole);
            List<UserResponse> userResponses = users.stream()
                    .map(UserResponse::new)
                    .collect(Collectors.toList());

            return ResponseUtil.createSuccessResponse(userResponses, "Usuarios obtenidos exitosamente");

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener usuarios: " + e.getMessage(), 500);
        }
    }

    // PUT /api/dev/users/{userId}/role - Cambiar rol de usuario (desarrollo - sin autenticación)
    @PutMapping("/dev/users/{userId}/role")
    public ResponseEntity<Map<String, Object>> changeUserRole(
            @PathVariable String userId, 
            @RequestBody Map<String, String> request) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("ID del usuario es requerido", 400);
            }

            if (request == null || !request.containsKey("role")) {
                return ResponseUtil.createErrorResponse("El campo 'role' es requerido", 400);
            }

            String newRole = request.get("role");
            if (newRole == null || newRole.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El rol no puede estar vacío", 400);
            }

            // Validar que el rol sea válido
            String cleanRole = newRole.trim().toUpperCase();
            if (!cleanRole.equals("USER") && !cleanRole.equals("ADMIN")) {
                return ResponseUtil.createErrorResponse("Rol debe ser USER o ADMIN", 400);
            }

            // Actualizar solo el rol del usuario
            User updatedUser = userService.updateUser(
                    userId.trim(),
                    null, // email
                    null, // username  
                    null, // password
                    cleanRole // role
            );

            UserResponse userResponse = new UserResponse(updatedUser);

            return ResponseUtil.createSuccessResponse(userResponse, 
                    "Rol de usuario cambiado exitosamente a " + cleanRole);

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseUtil.createErrorResponse(e.getMessage(), 404);
            }
            return ResponseUtil.createErrorResponse("Error al cambiar rol: " + e.getMessage(), 500);
        }
    }

    // PUT /api/public/change-user-role/{userId} - Cambiar rol de usuario (público - sin autenticación)
    @PutMapping("/api/public/change-user-role/{userId}")
    public ResponseEntity<Map<String, Object>> changeUserRolePublic(
            @PathVariable String userId, 
            @RequestBody Map<String, String> request) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("ID del usuario es requerido", 400);
            }

            if (request == null || !request.containsKey("role")) {
                return ResponseUtil.createErrorResponse("El campo 'role' es requerido", 400);
            }

            String newRole = request.get("role");
            if (newRole == null || newRole.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El rol no puede estar vacío", 400);
            }

            // Validar que el rol sea válido
            String cleanRole = newRole.trim().toUpperCase();
            if (!cleanRole.equals("USER") && !cleanRole.equals("ADMIN")) {
                return ResponseUtil.createErrorResponse("Rol debe ser USER o ADMIN", 400);
            }

            // Actualizar solo el rol del usuario
            User updatedUser = userService.updateUser(
                    userId.trim(),
                    null, // email
                    null, // username  
                    null, // password
                    cleanRole // role
            );

            UserResponse userResponse = new UserResponse(updatedUser);

            return ResponseUtil.createSuccessResponse(userResponse, 
                    "Rol de usuario cambiado exitosamente a " + cleanRole + " (endpoint público)");

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseUtil.createErrorResponse(e.getMessage(), 404);
            }
            return ResponseUtil.createErrorResponse("Error al cambiar rol: " + e.getMessage(), 500);
        }
    }
}