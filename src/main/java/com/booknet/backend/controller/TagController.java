package com.booknet.backend.controller;

import com.booknet.backend.dto.CreateTagRequest;
import com.booknet.backend.dto.PaginatedTagsResponse;
import com.booknet.backend.dto.TagResponse;
import com.booknet.backend.dto.UpdateTagRequest;
import com.booknet.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping
    public ResponseEntity<?> getAllTags(
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        
        try {
            // Si no se especifican parámetros de paginación, devolver todos los tags
            if (offset == null && limit == null) {
                List<TagResponse> allTags = tagService.getAllTags();
                return ResponseEntity.ok(new ApiResponse(true, "Tags obtenidos exitosamente", allTags));
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

            List<TagResponse> tags = tagService.getAllTagsWithPagination(offset, limit);
            long totalTags = tagService.getTotalTagsCount();

            PaginatedTagsResponse response = new PaginatedTagsResponse(
                    true,
                    "Tags obtenidos exitosamente",
                    tags,
                    (int) totalTags,
                    limit,
                    offset
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PaginatedTagsResponse errorResponse = new PaginatedTagsResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("Error al obtener los tags: " + e.getMessage());
            errorResponse.setTimestamp(LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTagById(@PathVariable String id) {
        try {
            Optional<TagResponse> tag = tagService.getTagById(id);
            
            if (tag.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "Tag encontrado", tag.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Tag no encontrado", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al obtener el tag: " + e.getMessage(), null));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTag(@RequestBody CreateTagRequest request) {
        try {
            // Validaciones básicas
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "El nombre del tag es requerido", null));
            }

            TagResponse createdTag = tagService.createTag(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Tag creado exitosamente", createdTag));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al crear el tag: " + e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTag(@PathVariable String id, @RequestBody UpdateTagRequest request) {
        try {
            Optional<TagResponse> updatedTag = tagService.updateTag(id, request);
            
            if (updatedTag.isPresent()) {
                return ResponseEntity.ok(new ApiResponse(true, "Tag actualizado exitosamente", updatedTag.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Tag no encontrado", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al actualizar el tag: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTag(@PathVariable String id) {
        try {
            boolean deleted = tagService.deleteTag(id);
            
            if (deleted) {
                return ResponseEntity.ok(new ApiResponse(true, "Tag eliminado exitosamente", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Tag no encontrado", null));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al eliminar el tag: " + e.getMessage(), null));
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
