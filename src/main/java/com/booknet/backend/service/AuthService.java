package com.booknet.backend.service;

import com.booknet.backend.dto.LoginRequest;
import com.booknet.backend.dto.LoginResponse;
import com.booknet.backend.dto.RegisterRequest;
import com.booknet.backend.model.User;
import com.booknet.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Set<String> invalidatedRefreshTokens = new HashSet<>();

    // Constructor injection (recomendado)
    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    // Registrar nuevo usuario
    public LoginResponse register(RegisterRequest request) {

        // Validar que no exista el email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Validar que no exista el username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // Crear nuevo usuario
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // Rol por defecto
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Guardar en base de datos
        User savedUser = userRepository.save(user);

        // Crear login response
        return createLoginResponse(savedUser);
    }

    // Login de usuario
    public LoginResponse login(LoginRequest request) {

        // Buscar usuario por username
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        User user = userOptional.get();

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Usuario o contraseña incorrectos");
        }

        // Actualizar último login
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Crear login response
        return createLoginResponse(user);
    }

    // Renovar access token usando refresh token
    public LoginResponse refreshToken(String refreshToken) {

        try {
            // Validar que sea refresh token
            if (!jwtService.isRefreshToken(refreshToken)) {
                throw new RuntimeException("Token inválido");
            }

            if (invalidatedRefreshTokens.contains(refreshToken)) {
                throw new RuntimeException("Refresh token expirado");
            }

            // Extraer información
            String username = jwtService.extractUsername(refreshToken);
            String userId = jwtService.extractUserId(refreshToken);

            // Validar token
            if (jwtService.isTokenExpired(refreshToken)) {
                throw new RuntimeException("Refresh token expirado");
            }

            // Buscar usuario
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                throw new RuntimeException("Usuario no encontrado");
            }

            User user = userOptional.get();

            if(!user.getId().equals(userId)) {
                throw new RuntimeException("Token inválido - Usuario no coincide");
            }

            // Generar nuevo access token
            String newAccessToken = jwtService.generateAccessToken(
                    user.getUsername(),
                    user.getRole(),
                    user.getId()
            );

            // Crear respuesta (reutilizar el mismo refresh token)
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );

            return new LoginResponse(
                    newAccessToken,
                    refreshToken, // Mismo refresh token
                    jwtService.getExpirationTimeInSeconds(),
                    userInfo
            );

        } catch (Exception e) {
            throw new RuntimeException("Token inválido: " + e.getMessage());
        }
    }

    private LoginResponse createLoginResponse(User user) {

        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                user.getRole(),
                user.getId()
        );

        String refreshToken = jwtService.generateRefreshToken(
                user.getUsername(),
                user.getId()
        );

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                jwtService.getExpirationTimeInSeconds(),
                userInfo
        );
    }

    // Cambiar rol de usuario (solo para admins)
    public User updateUserRole(String userId, String newRole) {

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOptional.get();
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public void invalidateRefreshToken(String refreshToken) {
        invalidatedRefreshTokens.add(refreshToken);
    }

    // Obtener usuario por ID
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}