package com.booknet.backend.controller;

import com.booknet.backend.dto.BookResponse;
import com.booknet.backend.dto.BulkCreateBookRequest;
import com.booknet.backend.dto.CreateBookRequest;
import com.booknet.backend.dto.EnhancedBulkCreateBookRequest;
import com.booknet.backend.dto.PaginatedBooksResponse;
import com.booknet.backend.dto.UpdateBookRequest;
import com.booknet.backend.model.Book;
import com.booknet.backend.service.BookService;
import com.booknet.backend.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // POST /api/books - Crear un libro individual
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBook(@RequestBody CreateBookRequest request) {
        try {
            if (request == null) {
                return ResponseUtil.createErrorResponse("Los datos del libro son requeridos", 400);
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El título del libro es requerido", 400);
            }

            if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty()) {
                return ResponseUtil.createErrorResponse("Al menos un autor es requerido", 400);
            }

            // Validar ISBN si se proporciona
            if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
                if (!isValidISBN(request.getIsbn().trim())) {
                    return ResponseUtil.createErrorResponse("Formato de ISBN inválido", 400);
                }
            }

            BookResponse book = bookService.createBook(request);
            return ResponseUtil.createSuccessResponse(book, "Libro creado exitosamente");
        } catch (Exception e) {
            if (e.getMessage().contains("ya existe") || e.getMessage().contains("duplicado")) {
                return ResponseUtil.createErrorResponse("El libro con este ISBN ya existe", 409);
            }
            return ResponseUtil.createErrorResponse("Error al crear libro: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/bulk - Crear múltiples libros (carga masiva)
    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBooksBulk(@RequestBody List<BulkCreateBookRequest> requests) {
        try {
            if (requests == null || requests.isEmpty()) {
                return ResponseUtil.createErrorResponse("La lista de libros no puede estar vacía", 400);
            }

            if (requests.size() > 1000) {
                return ResponseUtil.createErrorResponse("No se pueden procesar más de 1000 libros a la vez", 400);
            }

            // Validar cada libro en el lote
            for (int i = 0; i < requests.size(); i++) {
                BulkCreateBookRequest request = requests.get(i);
                if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                    return ResponseUtil.createErrorResponse("El título es requerido para el libro en posición " + (i + 1), 400);
                }
                if (request.getAuthorNames() == null || request.getAuthorNames().isEmpty()) {
                    return ResponseUtil.createErrorResponse("Al menos un autor es requerido para el libro en posición " + (i + 1), 400);
                }
                if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() && !isValidISBN(request.getIsbn().trim())) {
                    return ResponseUtil.createErrorResponse("Formato de ISBN inválido para el libro en posición " + (i + 1), 400);
                }
            }

            List<BookResponse> createdBooks = bookService.createBooksBulk(requests);
            
            Map<String, Object> result = Map.of(
                "booksCreated", createdBooks.size(),
                "totalRequested", requests.size(),
                "books", createdBooks
            );
            
            return ResponseUtil.createSuccessResponse(result, "Libros creados exitosamente en lote");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al crear libros en lote: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/enhanced-bulk - Crear múltiples libros con datos completos de autores, géneros y tags
    @PostMapping("/enhanced-bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBooksEnhancedBulk(@RequestBody List<EnhancedBulkCreateBookRequest> requests) {
        // Debug logs para diagnosticar problema 403
        System.out.println("=== DEBUG: enhanced-bulk endpoint called ===");
        System.out.println("Requests size: " + (requests != null ? requests.size() : "null"));
        
        try {
            if (requests == null || requests.isEmpty()) {
                return ResponseUtil.createErrorResponse("La lista de libros no puede estar vacía", 400);
            }

            if (requests.size() > 1000) {
                return ResponseUtil.createErrorResponse("No se pueden procesar más de 1000 libros a la vez", 400);
            }

            // Validar cada libro en el lote
            for (int i = 0; i < requests.size(); i++) {
                EnhancedBulkCreateBookRequest request = requests.get(i);
                if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                    return ResponseUtil.createErrorResponse("El título es requerido para el libro en posición " + (i + 1), 400);
                }
                if (request.getAuthors() == null || request.getAuthors().isEmpty()) {
                    return ResponseUtil.createErrorResponse("Al menos un autor es requerido para el libro en posición " + (i + 1), 400);
                }
                if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() && !isValidISBN(request.getIsbn().trim())) {
                    return ResponseUtil.createErrorResponse("Formato de ISBN inválido para el libro en posición " + (i + 1), 400);
                }
                
                // Validar datos de autores
                for (int j = 0; j < request.getAuthors().size(); j++) {
                    EnhancedBulkCreateBookRequest.AuthorData author = request.getAuthors().get(j);
                    if (author.getNombre() == null || author.getNombre().trim().isEmpty()) {
                        return ResponseUtil.createErrorResponse("El nombre del autor es requerido en posición " + (j + 1) + " del libro " + (i + 1), 400);
                    }
                }
                
                // Validar datos de géneros
                if (request.getGenres() != null) {
                    for (int j = 0; j < request.getGenres().size(); j++) {
                        EnhancedBulkCreateBookRequest.GenreData genre = request.getGenres().get(j);
                        if (genre.getNombre() == null || genre.getNombre().trim().isEmpty()) {
                            return ResponseUtil.createErrorResponse("El nombre del género es requerido en posición " + (j + 1) + " del libro " + (i + 1), 400);
                        }
                    }
                }
                
                // Validar datos de tags
                if (request.getTags() != null) {
                    for (int j = 0; j < request.getTags().size(); j++) {
                        EnhancedBulkCreateBookRequest.TagData tag = request.getTags().get(j);
                        if (tag.getNombre() == null || tag.getNombre().trim().isEmpty()) {
                            return ResponseUtil.createErrorResponse("El nombre del tag es requerido en posición " + (j + 1) + " del libro " + (i + 1), 400);
                        }
                    }
                }
            }

            List<BookResponse> createdBooks = bookService.createBooksEnhancedBulk(requests);
            
            Map<String, Object> result = Map.of(
                "booksCreated", createdBooks.size(),
                "totalRequested", requests.size(),
                "books", createdBooks
            );
            
            return ResponseUtil.createSuccessResponse(result, "Libros creados exitosamente con datos completos");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al crear libros con datos completos: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/enhanced-bulk-file - Crear múltiples libros desde archivo JSON
    @PostMapping(value = "/enhanced-bulk-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBooksEnhancedBulkFromFile(@RequestParam("file") MultipartFile file) {
        System.out.println("=== DEBUG: enhanced-bulk-file endpoint called ===");
        System.out.println("File name: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("File size: " + (file != null ? file.getSize() : "null"));
        
        try {
            if (file == null || file.isEmpty()) {
                System.out.println("ERROR: Archivo vacío o nulo");
                return ResponseUtil.createErrorResponse("El archivo JSON es requerido", 400);
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".json")) {
                System.out.println("ERROR: Archivo no es JSON: " + file.getOriginalFilename());
                return ResponseUtil.createErrorResponse("El archivo debe ser un JSON válido", 400);
            }
            
            // Leer contenido del archivo
            System.out.println("Leyendo contenido del archivo...");
            String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            System.out.println("Contenido leído, primeros 200 caracteres: " + jsonContent.substring(0, Math.min(200, jsonContent.length())));
            
            // Parsear JSON a lista de requests
            System.out.println("Parseando JSON...");
            ObjectMapper objectMapper = new ObjectMapper();
            // Configurar para manejar fechas Java 8
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            
            List<EnhancedBulkCreateBookRequest> requests;
            
            try {
                requests = objectMapper.readValue(
                    jsonContent, 
                    new TypeReference<List<EnhancedBulkCreateBookRequest>>() {}
                );
                System.out.println("JSON parseado exitosamente");
            } catch (JsonProcessingException e) {
                System.out.println("ERROR parseando JSON: " + e.getMessage());
                System.out.println("Línea del error: " + e.getLocation().getLineNr());
                System.out.println("Columna del error: " + e.getLocation().getColumnNr());
                return ResponseUtil.createErrorResponse("Error al parsear JSON: " + e.getMessage() + " en línea " + e.getLocation().getLineNr(), 400);
            }
            
            System.out.println("Parsed requests size: " + requests.size());
            
            if (requests.isEmpty()) {
                System.out.println("ERROR: Lista de libros vacía");
                return ResponseUtil.createErrorResponse("La lista de libros no puede estar vacía", 400);
            }
            
            if (requests.size() > 1000) {
                System.out.println("ERROR: Demasiados libros: " + requests.size());
                return ResponseUtil.createErrorResponse("Máximo 1000 libros por carga", 400);
            }
            
            // Validar primer libro para debug
            if (!requests.isEmpty()) {
                EnhancedBulkCreateBookRequest firstBook = requests.get(0);
                System.out.println("Primer libro - Título: " + firstBook.getTitle());
                System.out.println("Primer libro - Autores: " + (firstBook.getAuthors() != null ? firstBook.getAuthors().size() : "null"));
                System.out.println("Primer libro - Géneros: " + (firstBook.getGenres() != null ? firstBook.getGenres().size() : "null"));
                System.out.println("Primer libro - Tags: " + (firstBook.getTags() != null ? firstBook.getTags().size() : "null"));
            }
            
            // Procesar libros
            System.out.println("Iniciando procesamiento de libros...");
            List<BookResponse> createdBooks = bookService.createBooksEnhancedBulk(requests);
            System.out.println("Procesamiento completado. Libros creados: " + createdBooks.size());
            
            return ResponseUtil.createSuccessResponse(
                createdBooks, 
                "Libros creados exitosamente desde archivo: " + createdBooks.size() + " de " + requests.size()
            );
            
        } catch (JsonProcessingException e) {
            System.out.println("ERROR JSON Processing: " + e.getMessage());
            e.printStackTrace();
            return ResponseUtil.createErrorResponse("Error al parsear JSON: " + e.getMessage(), 400);
        } catch (Exception e) {
            System.err.println("ERROR GENERAL en enhanced-bulk-file: " + e.getMessage());
            e.printStackTrace();
            return ResponseUtil.createErrorResponse("Error al crear libros desde archivo: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/{id} - Obtener libro por ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookById(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del libro es requerido", 400);
            }

            Optional<BookResponse> book = bookService.getBookById(id.trim());
            if (book.isPresent()) {
                return ResponseUtil.createSuccessResponse(book.get(), "Libro encontrado");
            } else {
                return ResponseUtil.createErrorResponse("Libro no encontrado", 404);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libro: " + e.getMessage(), 500);
        }
    }

    // GET /api/books - Listar libros con paginación opcional
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBooks(
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {
        try {
            // Si no se especifican parámetros de paginación, devolver todos los libros
            if (offset == null && limit == null) {
                List<BookResponse> allBooks = bookService.getAllBooksWithoutPagination();
                return ResponseUtil.createSuccessResponse(allBooks, "Libros obtenidos exitosamente");
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

            // Obtener libros paginados
            List<BookResponse> bookResponses = bookService.getAllBooksWithPagination(offset, limit);

            // Obtener total de libros para metadatos de paginación
            long totalBooks = bookService.getTotalBooksCount();

            // Crear respuesta paginada
            PaginatedBooksResponse paginatedResponse = new PaginatedBooksResponse(
                    bookResponses, totalBooks, offset, limit);

            return ResponseUtil.createSuccessResponse(paginatedResponse, "Libros obtenidos exitosamente");

        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros: " + e.getMessage(), 500);
        }
    }

    // PUT /api/books/{id} - Actualizar libro completo
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBook(@PathVariable String id, @RequestBody CreateBookRequest request) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del libro es requerido", 400);
            }

            if (request == null) {
                return ResponseUtil.createErrorResponse("Los datos del libro son requeridos", 400);
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El título del libro es requerido", 400);
            }

            if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty()) {
                return ResponseUtil.createErrorResponse("Al menos un autor es requerido", 400);
            }

            if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() && !isValidISBN(request.getIsbn().trim())) {
                return ResponseUtil.createErrorResponse("Formato de ISBN inválido", 400);
            }

            BookResponse book = bookService.updateBook(id.trim(), request);
            if (book != null) {
                return ResponseUtil.createSuccessResponse(book, "Libro actualizado exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Libro no encontrado", 404);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al actualizar libro: " + e.getMessage(), 500);
        }
    }

    // PATCH /api/books/{id} - Actualizar libro parcialmente
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBookPartially(@PathVariable String id, @RequestBody UpdateBookRequest request) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del libro es requerido", 400);
            }

            if (request == null) {
                return ResponseUtil.createErrorResponse("Los datos de actualización son requeridos", 400);
            }

            // Validar que al menos un campo esté presente
            if (isAllFieldsNull(request)) {
                return ResponseUtil.createErrorResponse("Debe proporcionar al menos un campo para actualizar", 400);
            }

            // Validar ISBN si se proporciona
            if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() && !isValidISBN(request.getIsbn().trim())) {
                return ResponseUtil.createErrorResponse("Formato de ISBN inválido", 400);
            }

            // Validar título si se proporciona
            if (request.getTitle() != null && request.getTitle().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El título no puede estar vacío", 400);
            }

            // Validar calificación si se proporciona
            if (request.getAverageRating() != null && (request.getAverageRating() < 0.0 || request.getAverageRating() > 5.0)) {
                return ResponseUtil.createErrorResponse("La calificación promedio debe estar entre 0.0 y 5.0", 400);
            }

            BookResponse book = bookService.updateBookPartially(id.trim(), request);
            if (book != null) {
                return ResponseUtil.createSuccessResponse(book, "Libro actualizado exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Libro no encontrado", 404);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al actualizar libro: " + e.getMessage(), 500);
        }
    }

    // DELETE /api/books/{id} - Eliminar libro
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteBook(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del libro es requerido", 400);
            }

            boolean deleted = bookService.deleteBook(id.trim());
            if (deleted) {
                return ResponseUtil.createSuccessResponse(null, "Libro eliminado exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Libro no encontrado", 404);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al eliminar libro: " + e.getMessage(), 500);
        }
    }

    // PATCH /api/books/{id}/cover-image - Actualizar solo la imagen de portada
    @PatchMapping("/{id}/cover-image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBookCoverImage(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del libro es requerido", 400);
            }

            if (request == null || !request.containsKey("coverImage")) {
                return ResponseUtil.createErrorResponse("La nueva URL de la imagen es requerida", 400);
            }

            String newCoverImage = request.get("coverImage");
            if (newCoverImage == null || newCoverImage.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("La URL de la imagen no puede estar vacía", 400);
            }

            BookResponse book = bookService.updateBookCoverImage(id.trim(), newCoverImage.trim());
            if (book != null) {
                return ResponseUtil.createSuccessResponse(book, "Imagen de portada actualizada exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Libro no encontrado", 404);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al actualizar imagen de portada: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/search - Buscar libros
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchBooks(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            if (q == null || q.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El término de búsqueda es requerido", 400);
            }

            if (q.trim().length() < 2) {
                return ResponseUtil.createErrorResponse("El término de búsqueda debe tener al menos 2 caracteres", 400);
            }

            if (page < 0) {
                return ResponseUtil.createErrorResponse("El número de página debe ser mayor o igual a 0", 400);
            }

            if (size <= 0 || size > 100) {
                return ResponseUtil.createErrorResponse("El tamaño de página debe estar entre 1 y 100", 400);
            }

            Map<String, Object> result = bookService.searchBooksWithPagination(q.trim(), page, size);
            return ResponseUtil.createSuccessResponse(result, "Búsqueda completada exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al buscar libros: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/genre/{genreName} - Obtener libros por género
    @GetMapping("/genre/{genreName}")
    public ResponseEntity<Map<String, Object>> getBooksByGenre(
            @PathVariable String genreName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            if (genreName == null || genreName.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El nombre del género es requerido", 400);
            }

            if (page < 0) {
                return ResponseUtil.createErrorResponse("El número de página debe ser mayor o igual a 0", 400);
            }

            if (size <= 0 || size > 100) {
                return ResponseUtil.createErrorResponse("El tamaño de página debe estar entre 1 y 100", 400);
            }

            Map<String, Object> result = bookService.getBooksByGenreWithPagination(genreName.trim(), page, size);
            return ResponseUtil.createSuccessResponse(result, "Libros por género obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros por género: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/author/{authorName} - Obtener libros por autor
    @GetMapping("/author/{authorName}")
    public ResponseEntity<Map<String, Object>> getBooksByAuthor(
            @PathVariable String authorName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            if (authorName == null || authorName.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El nombre del autor es requerido", 400);
            }

            if (page < 0) {
                return ResponseUtil.createErrorResponse("El número de página debe ser mayor o igual a 0", 400);
            }

            if (size <= 0 || size > 100) {
                return ResponseUtil.createErrorResponse("El tamaño de página debe estar entre 1 y 100", 400);
            }

            Map<String, Object> result = bookService.getBooksByAuthorWithPagination(authorName.trim(), page, size);
            return ResponseUtil.createSuccessResponse(result, "Libros por autor obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros por autor: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/top-rated - Obtener libros mejor calificados
    @GetMapping("/top-rated")
    public ResponseEntity<Map<String, Object>> getTopRatedBooks(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (limit <= 0 || limit > 100) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 100", 400);
            }

            List<BookResponse> books = bookService.getTopRatedBooks(limit);
            return ResponseUtil.createSuccessResponse(books, "Libros mejor calificados obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros mejor calificados: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/recent - Obtener libros recientes
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentBooks(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (limit <= 0 || limit > 100) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 100", 400);
            }

            List<BookResponse> books = bookService.getRecentBooks(limit);
            return ResponseUtil.createSuccessResponse(books, "Libros recientes obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros recientes: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/safe - Crear libro con validación estricta de referencias
    @PostMapping("/safe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createBookSafe(@RequestBody CreateBookRequest request) {
        try {
            if (request == null) {
                return ResponseUtil.createErrorResponse("Los datos del libro son requeridos", 400);
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El título es requerido", 400);
            }

            if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty()) {
                return ResponseUtil.createErrorResponse("Al menos un autor es requerido", 400);
            }

            if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() && !isValidISBN(request.getIsbn().trim())) {
                return ResponseUtil.createErrorResponse("Formato de ISBN inválido", 400);
            }

            BookResponse book = bookService.createBookWithValidation(request);
            return ResponseUtil.createSuccessResponse(book, "Libro creado exitosamente con validación estricta");
        } catch (RuntimeException e) {
            return ResponseUtil.createErrorResponse(e.getMessage(), 400);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al crear libro: " + e.getMessage(), 500);
        }
    }

    // PUT /api/books/{id}/safe - Actualizar libro con validación estricta de referencias
    @PutMapping("/{id}/safe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateBookSafe(@PathVariable String id, @RequestBody CreateBookRequest request) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del libro es requerido", 400);
            }

            if (request == null) {
                return ResponseUtil.createErrorResponse("Los datos del libro son requeridos", 400);
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El título es requerido", 400);
            }

            if (request.getAuthorIds() == null || request.getAuthorIds().isEmpty()) {
                return ResponseUtil.createErrorResponse("Al menos un autor es requerido", 400);
            }

            if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty() && !isValidISBN(request.getIsbn().trim())) {
                return ResponseUtil.createErrorResponse("Formato de ISBN inválido", 400);
            }

            BookResponse book = bookService.updateBookWithValidation(id.trim(), request);
            if (book != null) {
                return ResponseUtil.createSuccessResponse(book, "Libro actualizado exitosamente con validación estricta");
            } else {
                return ResponseUtil.createErrorResponse("Libro no encontrado", 404);
            }
        } catch (RuntimeException e) {
            return ResponseUtil.createErrorResponse(e.getMessage(), 400);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al actualizar libro: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/{id}/clean-references - Limpiar referencias huérfanas de un libro específico
    @PostMapping("/{id}/clean-references")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanBookReferences(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El ID del libro es requerido", 400);
            }

            BookResponse cleanedBook = bookService.cleanOrphanedReferences(id.trim());
            return ResponseUtil.createSuccessResponse(cleanedBook, "Referencias huérfanas limpiadas exitosamente");
        } catch (RuntimeException e) {
            return ResponseUtil.createErrorResponse(e.getMessage(), 400);
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al limpiar referencias: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/clean-all-references - Limpiar referencias huérfanas de todos los libros
    @PostMapping("/clean-all-references")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> cleanAllBooksReferences() {
        try {
            int cleanedBooks = bookService.cleanAllOrphanedReferences();
            return ResponseUtil.createSuccessResponse(
                Map.of("libros_limpiados", cleanedBooks), 
                "Referencias huérfanas limpiadas en " + cleanedBooks + " libros"
            );
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al limpiar todas las referencias: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/{bookId}/ratings - Obtener todas las calificaciones y reseñas de un libro específico
    @GetMapping("/{bookId}/ratings")
    public ResponseEntity<Map<String, Object>> getBookRatings(@PathVariable String bookId,
                                                             @RequestParam(defaultValue = "0") int offset,
                                                             @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> ratings = bookService.getBookRatings(bookId, offset, limit);
            return ResponseUtil.createSuccessResponse(ratings, "Calificaciones obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener calificaciones: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/recalculate-ratings - Recalcular promedios de calificación de todos los libros
    @PostMapping("/recalculate-ratings")
    // @PreAuthorize("hasRole('ADMIN')") // Temporalmente deshabilitado para pruebas
    public ResponseEntity<Map<String, Object>> recalculateAllBookRatings() {
        try {
            bookService.recalculateAllBookRatings();
            return ResponseUtil.createSuccessResponse(null, "Promedios de calificación recalculados exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al recalcular promedios: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/clean-database - Limpiar completamente la base de datos Neo4j
    @PostMapping("/clean-database")
    public ResponseEntity<Map<String, Object>> cleanDatabase() {
        try {
            bookService.cleanDatabase();
            return ResponseUtil.createSuccessResponse(null, "Base de datos Neo4j limpiada completamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al limpiar base de datos: " + e.getMessage(), 500);
        }
    }

    // ENDPOINTS PARA RECOMENDACIONES Y DESTACADOS

    // GET /api/books/featured - Obtener libros destacados (top 10 con mayor promedio de calificaciones)
    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedBooks() {
        try {
            List<BookResponse> featuredBooks = bookService.getFeaturedBooks();
            return ResponseUtil.createSuccessResponse(featuredBooks, "Libros destacados obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros destacados: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/recommendations - Obtener recomendaciones para el usuario autenticado
    @GetMapping("/recommendations")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getRecommendations(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<BookResponse> recommendations = bookService.getRecommendationsForUser(userId);
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/continue-reading - Obtener libros para continuar leyendo del usuario autenticado
    @GetMapping("/continue-reading")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getContinueReading(Authentication authentication) {
        try {
            String userId = authentication.getName();
            List<BookResponse> continueReadingBooks = bookService.getContinueReadingForUser(userId);
            return ResponseUtil.createSuccessResponse(continueReadingBooks, "Libros para continuar leyendo obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros para continuar leyendo: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/top10 - Obtener top 10 libros con mayor promedio (con relleno inteligente)
    @GetMapping("/top10")
    public ResponseEntity<Map<String, Object>> getTop10Books() {
        try {
            List<BookResponse> top10Books = bookService.getTop10Books();
            return ResponseUtil.createSuccessResponse(top10Books, "Top 10 libros obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener top 10 libros: " + e.getMessage(), 500);
        }
    }

    // GET /api/books/check-ratings-status - Verificar estado de calificaciones (temporal para pruebas)
    @GetMapping("/check-ratings-status")
    public ResponseEntity<Map<String, Object>> checkRatingsStatus() {
        try {
            Map<String, Object> status = bookService.checkRatingsStatus();
            return ResponseUtil.createSuccessResponse(status, "Estado de calificaciones obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al verificar estado: " + e.getMessage(), 500);
        }
    }

    // POST /api/books/force-recalculate - Forzar recálculo sin autenticación (temporal para pruebas)
    @PostMapping("/force-recalculate")
    public ResponseEntity<Map<String, Object>> forceRecalculateRatings() {
        try {
            bookService.recalculateAllBookRatings();
            return ResponseUtil.createSuccessResponse(null, "Recálculo forzado completado exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error en recálculo forzado: " + e.getMessage(), 500);
        }
    }

    // Métodos auxiliares privados
    private boolean isValidISBN(String isbn) {
        if (isbn == null) return false;
        // Remover guiones y espacios
        String cleanIsbn = isbn.replaceAll("[\\s\\-]", "");
        // Validar ISBN-10 o ISBN-13
        return cleanIsbn.matches("^\\d{10}$") || cleanIsbn.matches("^\\d{13}$");
    }

    private boolean isAllFieldsNull(UpdateBookRequest request) {
        return request.getTitle() == null &&
               request.getIsbn() == null &&
               request.getDescription() == null &&
               request.getPublicationYear() == null &&
               request.getPageCount() == null &&
               request.getLanguage() == null &&
               request.getCoverImage() == null &&
               request.getAgeRating() == null &&
               request.getAverageRating() == null &&
               request.getTotalRatings() == null &&
               request.getReadingDifficulty() == null &&
               request.getAuthorIds() == null &&
               request.getGenreIds() == null &&
               request.getTagIds() == null &&
               request.getSeriesId() == null &&
               request.getOrderInSeries() == null;
    }
}