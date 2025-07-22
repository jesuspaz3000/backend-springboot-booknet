package com.booknet.backend.controller;

import com.booknet.backend.dto.CreateGenreRequest;
import com.booknet.backend.dto.GenreResponse;
import com.booknet.backend.dto.PaginatedGenresResponse;
import com.booknet.backend.dto.UpdateGenreRequest;
import com.booknet.backend.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/genres")
@CrossOrigin(origins = "*")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @GetMapping
    public ResponseEntity<?> getAllGenres(
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        
        try {
            // Si no se especifican parámetros de paginación, devolver todos los géneros
            if (offset == null && limit == null) {
                List<GenreResponse> allGenres = genreService.getAllGenres();
                return ResponseEntity.ok(new ApiResponse(true, "Géneros obtenidos exitosamente", allGenres));
            }
            
            // Si se especifica paginación, aplicar valores por defecto y validaciones
            if (offset == null) offset = 0;
            if (limit == null) limit = 10;
            
            // Validar parámetros de paginación
            if (offset < 0) {
                offset = 0;
            }
            if (limit <= 0 || limit > 100) {
                limit = 10;
            }

            List<GenreResponse> genres = genreService.getAllGenresWithPagination(offset, limit);
            long totalGenres = genreService.getTotalGenresCount();

            PaginatedGenresResponse response = new PaginatedGenresResponse(
                    true,
                    "Géneros obtenidos exitosamente",
                    genres,
                    (int) totalGenres,
                    limit,
                    offset
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al obtener los géneros: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGenreById(@PathVariable String id) {
        try {
            Optional<GenreResponse> genre = genreService.getGenreById(id);
            
            if (genre.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "Género encontrado", genre.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Género no encontrado", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al obtener el género: " + e.getMessage(), null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createGenre(@RequestBody CreateGenreRequest request) {
        try {
            // Validaciones básicas
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "El nombre del género es requerido", null));
            }

            GenreResponse createdGenre = genreService.createGenre(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Género creado exitosamente", createdGenre));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al crear el género: " + e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateGenre(@PathVariable String id, @RequestBody UpdateGenreRequest request) {
        try {
            Optional<GenreResponse> updatedGenre = genreService.updateGenre(id, request);
            
            if (updatedGenre.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "Género actualizado exitosamente", updatedGenre.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Género no encontrado", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al actualizar el género: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteGenre(@PathVariable String id) {
        try {
            boolean deleted = genreService.deleteGenre(id);
            
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse(true, "Género eliminado exitosamente", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Género no encontrado", null));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al eliminar el género: " + e.getMessage(), null));
        }
    }

    // Clase interna para respuestas de API
    public static class ApiResponse {
        private boolean success;
        private String message;
        private LocalDateTime timestamp;
        private Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
