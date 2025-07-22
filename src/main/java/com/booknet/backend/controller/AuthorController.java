package com.booknet.backend.controller;

import com.booknet.backend.dto.AuthorResponse;
import com.booknet.backend.dto.CreateAuthorRequest;
import com.booknet.backend.dto.PaginatedAuthorsResponse;
import com.booknet.backend.dto.UpdateAuthorRequest;
import com.booknet.backend.model.Author;
import com.booknet.backend.service.AuthorService;
import com.booknet.backend.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/authors")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // POST /api/authors - Crear un autor
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createAuthor(@RequestBody CreateAuthorRequest request) {
        try {
            if (request == null) {
                return ResponseUtil.createErrorResponse("Los datos del autor son requeridos", 400);
            }

            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El nombre del autor es requerido", 400);
            }

            AuthorResponse author = authorService.createAuthor(request);
            return ResponseEntity.status(201)
                    .body(ResponseUtil.createSuccessResponseMap("Autor creado exitosamente", author));

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al crear autor: " + e.getMessage(), 500);
        }
    }

    // GET /api/authors/{id} - Obtener autor por ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAuthorById(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del autor es requerido", 400);
            }

            Optional<AuthorResponse> author = authorService.getAuthorById(id.trim());
            if (author.isPresent()) {
                return ResponseUtil.createSuccessResponse(author.get(), "Autor encontrado");
            } else {
                return ResponseUtil.createErrorResponse("Autor no encontrado", 404);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener autor: " + e.getMessage(), 500);
        }
    }

    // GET /api/authors - Listar autores con paginación opcional
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAuthors(
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        try {
            // Si no se especifican parámetros de paginación, devolver todos los autores
            if (offset == null && limit == null) {
                List<Author> allAuthors = authorService.getAllAuthors();
                List<AuthorResponse> authorResponses = allAuthors.stream()
                        .map(authorService::convertToAuthorResponse)
                        .collect(Collectors.toList());
                
                return ResponseUtil.createSuccessResponse(authorResponses, "Autores obtenidos exitosamente");
            }
            
            // Si se especifica paginación, aplicar valores por defecto y validaciones
            if (offset == null) offset = 0;
            if (limit == null) limit = 20;
            
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

            // Obtener autores paginados
            List<Author> authors = authorService.getAllAuthorsWithSimplePagination(offset, limit);
            List<AuthorResponse> authorResponses = authors.stream()
                    .map(authorService::convertToAuthorResponse)
                    .collect(Collectors.toList());

            // Obtener total de autores para metadatos de paginación
            long totalAuthors = authorService.getTotalAuthorsCount();

            // Crear respuesta paginada
            PaginatedAuthorsResponse paginatedResponse = new PaginatedAuthorsResponse(
                    authorResponses, totalAuthors, offset, limit);

            return ResponseUtil.createSuccessResponse(paginatedResponse, "Autores obtenidos exitosamente");

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener autores: " + e.getMessage(), 500);
        }
    }

    // PATCH /api/authors/{id} - Actualizar autor parcialmente
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateAuthor(@PathVariable String id, @RequestBody UpdateAuthorRequest request) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del autor es requerido", 400);
            }

            if (request == null) {
                return ResponseUtil.createErrorResponse("Los datos de actualización son requeridos", 400);
            }

            AuthorResponse author = authorService.updateAuthor(id.trim(), request);
            return ResponseUtil.createSuccessResponse(author, "Autor actualizado exitosamente");

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseUtil.createErrorResponse(e.getMessage(), 404);
            }
            return ResponseUtil.createErrorResponse("Error al actualizar autor: " + e.getMessage(), 500);
        }
    }

    // DELETE /api/authors/{id} - Eliminar autor
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAuthor(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del autor es requerido", 400);
            }

            authorService.deleteAuthor(id.trim());
            return ResponseUtil.createSuccessResponse(null, "Autor eliminado exitosamente");

        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseUtil.createErrorResponse(e.getMessage(), 404);
            }
            if (e.getMessage().contains("libros asociados")) {
                return ResponseUtil.createErrorResponse(e.getMessage(), 409);
            }
            return ResponseUtil.createErrorResponse("Error al eliminar autor: " + e.getMessage(), 500);
        }
    }
}
