package com.booknet.backend.service;

import com.booknet.backend.model.User;
import com.booknet.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Listar todos los usuarios
    public List<User> getAllUsers() {
        return userRepository.findAllOrderByCreatedAtDesc();
    }

    // Listar usuarios con paginación
    public List<User> getAllUsersWithPagination(int offset, int limit) {
        // Validar parámetros
        if (offset < 0) {
            offset = 0;
        }
        if (limit <= 0 || limit > 100) { // Máximo 100 usuarios por página
            limit = 10; // Por defecto 10
        }
        
        return userRepository.findAllOrderByCreatedAtDescWithPagination(offset, limit);
    }

    // Contar total de usuarios
    public long getTotalUsersCount() {
        return userRepository.countAllUsers();
    }

    // Obtener usuario por ID
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Crear nuevo usuario
    public User createUser(String email, String username, String password, String role) {
        // Validar que no exista el email
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Validar que no exista el username
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // Crear nuevo usuario
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? role : "USER"); // Rol por defecto
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Actualizar usuario (PATCH)
    public User updateUser(String userId, String email, String username, String password, String role) {
        User user = getUserById(userId);

        boolean updated = false;

        // Actualizar email si se proporciona
        if (email != null && !email.trim().isEmpty() && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("El email ya está registrado");
            }
            user.setEmail(email);
            updated = true;
        }

        // Actualizar username si se proporciona
        if (username != null && !username.trim().isEmpty() && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("El nombre de usuario ya está en uso");
            }
            user.setUsername(username);
            updated = true;
        }

        // Actualizar password si se proporciona
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
            updated = true;
        }

        // Actualizar role si se proporciona
        if (role != null && !role.trim().isEmpty() && !role.equals(user.getRole())) {
            if (!role.equals("USER") && !role.equals("ADMIN")) {
                throw new RuntimeException("Rol debe ser USER o ADMIN");
            }
            user.setRole(role);
            updated = true;
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        return user;
    }

    // Eliminar usuario
    public void deleteUser(String userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    // Buscar usuarios por rol
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    // Verificar si existe usuario por email
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Verificar si existe usuario por username
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // Actualizar perfil de usuario (requiere contraseña actual)
    public User updateUserProfile(String userId, String currentPassword, String newEmail, String newUsername, String newPassword) {
        User user = getUserById(userId);

        // Verificar contraseña actual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        boolean updated = false;

        // Actualizar email si se proporciona
        if (newEmail != null && !newEmail.trim().isEmpty() && !newEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("El email ya está registrado");
            }
            user.setEmail(newEmail);
            updated = true;
        }

        // Actualizar username si se proporciona
        if (newUsername != null && !newUsername.trim().isEmpty() && !newUsername.equals(user.getUsername())) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new RuntimeException("El nombre de usuario ya está en uso");
            }
            user.setUsername(newUsername);
            updated = true;
        }

        // Actualizar password si se proporciona
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 6) {
                throw new RuntimeException("La nueva contraseña debe tener al menos 6 caracteres");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            updated = true;
        }

        if (updated) {
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        return user;
    }
}
