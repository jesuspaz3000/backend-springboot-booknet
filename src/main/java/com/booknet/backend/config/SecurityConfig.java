package com.booknet.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        
                        // Endpoints de desarrollo sin autenticación (DEBE IR ANTES que /api/users/**)
                        .requestMatchers("/api/dev/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/dev/users/*/role").permitAll()

                        // Endpoints de libros públicos (lectura)
                        .requestMatchers(HttpMethod.GET, "/api/books").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        
                        // Endpoints temporales para pruebas (sin autenticación)
                        .requestMatchers("/api/books/force-recalculate").permitAll()
                        .requestMatchers("/api/books/check-ratings-status").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/books/clean-database").permitAll()
                        
                        // Endpoints que requieren autenticación y rol ADMIN
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // Endpoints que requieren autenticación (cualquier usuario autenticado)
                        .requestMatchers("/api/profile/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
                // Agregar el filtro JWT antes del filtro de autenticación por username/password
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Crear lista dinámica de orígenes permitidos
        List<String> allowedOrigins = new ArrayList<>();
        // Orígenes locales
        allowedOrigins.add("http://localhost:3000");
        allowedOrigins.add("http://127.0.0.1:3000");

        try {
            // Obtener IP pública automáticamente desde AWS EC2
            String publicIp = getPublicIp();
            if (publicIp != null && !publicIp.isEmpty()) {
                allowedOrigins.add("http://" + publicIp + ":3000");
                allowedOrigins.add("https://" + publicIp + ":3000");
                System.out.println("IP pública detectada: " + publicIp);
            }

            // Obtener IP privada de la instancia EC2 (por si acaso)
            String privateIp = getPrivateIp();
            if (privateIp != null && !privateIp.isEmpty()) {
                allowedOrigins.add("http://" + privateIp + ":3000");
                System.out.println("IP privada detectada: " + privateIp);
            }
        } catch (Exception e) {
            System.err.println("No se pudo obtener la IP automáticamente: " + e.getMessage());
            // Fallback a IPs conocidas
            allowedOrigins.add("http://18.229.124.33:3000");
            allowedOrigins.add("http://54.232.248.38:3000");
        }

        // Mostrar todas las IPs permitidas en los logs
        System.out.println("Orígenes CORS permitidos: " + allowedOrigins);

        configuration.setAllowedOriginPatterns(allowedOrigins);

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers permitidos (específicos para mejor seguridad)
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Headers expuestos al cliente
        configuration.setExposedHeaders(List.of("Access-Control-Allow-Origin"));

        // Permitir credenciales
        configuration.setAllowCredentials(true);

        // Tiempo de cache para preflight requests (en segundos)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String getPublicIp() throws Exception {
        // Usar el servicio de metadatos de AWS EC2
        URL url = new URL("http://169.254.169.254/latest/meta-data/public-ipv4");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.readLine();
        }
    }

    private String getPrivateIp() throws Exception {
        URL url = new URL("http://169.254.169.254/latest/meta-data/local-ipv4");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.readLine();
        }
    }
}
