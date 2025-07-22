package com.booknet.backend.controller;

import com.booknet.backend.dto.RateBookRequest;
import com.booknet.backend.dto.ReadingProgressRequest;
import com.booknet.backend.model.Book;
import com.booknet.backend.model.User;
import com.booknet.backend.service.JwtService;
import com.booknet.backend.service.UserInteractionService;
import com.booknet.backend.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-interactions")
@CrossOrigin(origins = "*")
public class UserInteractionController {

    private final UserInteractionService userInteractionService;
    private final JwtService jwtService;

    public UserInteractionController(UserInteractionService userInteractionService, 
                                   JwtService jwtService) {
        this.userInteractionService = userInteractionService;
        this.jwtService = jwtService;
    }

    @PostMapping("/rate")
    public ResponseEntity<Map<String, Object>> rateBook(@RequestHeader("Authorization") String authHeader,
                                                       @RequestBody RateBookRequest request) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            boolean success = userInteractionService.rateBook(userId, request);
            if (success) {
                return ResponseUtil.createSuccessResponse(null, "Libro calificado exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Error al calificar el libro", 400);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al calificar libro: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/reading-progress")
    public ResponseEntity<Map<String, Object>> updateReadingProgress(@RequestHeader("Authorization") String authHeader,
                                                                   @RequestBody ReadingProgressRequest request) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            boolean success = userInteractionService.updateReadingProgress(userId, request);
            if (success) {
                return ResponseUtil.createSuccessResponse(null, "Progreso de lectura actualizado exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Error al actualizar progreso de lectura", 400);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al actualizar progreso de lectura: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/favorites/{bookId}")
    public ResponseEntity<Map<String, Object>> addToFavorites(@RequestHeader("Authorization") String authHeader,
                                                             @PathVariable String bookId) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            boolean success = userInteractionService.addToFavorites(userId, bookId);
            if (success) {
                return ResponseUtil.createSuccessResponse(null, "Libro agregado a favoritos exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Error al agregar libro a favoritos", 400);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al agregar libro a favoritos: " + e.getMessage(), 500);
        }
    }

    @DeleteMapping("/favorites/{bookId}")
    public ResponseEntity<Map<String, Object>> removeFromFavorites(@RequestHeader("Authorization") String authHeader,
                                                                 @PathVariable String bookId) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            boolean success = userInteractionService.removeFromFavorites(userId, bookId);
            if (success) {
                return ResponseUtil.createSuccessResponse(null, "Libro removido de favoritos exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Error al remover libro de favoritos", 400);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al remover libro de favoritos: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/follow/{followeeId}")
    public ResponseEntity<Map<String, Object>> followUser(@RequestHeader("Authorization") String authHeader,
                                                         @PathVariable String followeeId) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String followerId = jwtService.extractUserId(token);
            
            if (followerId.equals(followeeId)) {
                return ResponseUtil.createErrorResponse("No puedes seguirte a ti mismo", 400);
            }
            
            boolean success = userInteractionService.followUser(followerId, followeeId);
            if (success) {
                return ResponseUtil.createSuccessResponse(null, "Usuario seguido exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Error al seguir usuario", 400);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al seguir usuario: " + e.getMessage(), 500);
        }
    }

    @DeleteMapping("/follow/{followeeId}")
    public ResponseEntity<Map<String, Object>> unfollowUser(@RequestHeader("Authorization") String authHeader,
                                                           @PathVariable String followeeId) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String followerId = jwtService.extractUserId(token);
            
            boolean success = userInteractionService.unfollowUser(followerId, followeeId);
            if (success) {
                return ResponseUtil.createSuccessResponse(null, "Usuario no seguido exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Error al dejar de seguir usuario", 400);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al dejar de seguir usuario: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/rated-books")
    public ResponseEntity<Map<String, Object>> getUserRatedBooks(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            List<Book> books = userInteractionService.getUserRatedBooks(userId);
            return ResponseUtil.createSuccessResponse(books, "Libros calificados obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros calificados: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/books-by-status/{status}")
    public ResponseEntity<Map<String, Object>> getUserBooksByStatus(@RequestHeader("Authorization") String authHeader,
                                                                   @PathVariable String status) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            List<Book> books = userInteractionService.getUserBooksByStatus(userId, status);
            return ResponseUtil.createSuccessResponse(books, "Libros por estado obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros por estado: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/favorite-books")
    public ResponseEntity<Map<String, Object>> getUserFavoriteBooks(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            List<Book> books = userInteractionService.getUserFavoriteBooks(userId);
            return ResponseUtil.createSuccessResponse(books, "Libros favoritos obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener libros favoritos: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/following")
    public ResponseEntity<Map<String, Object>> getUserFollowing(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            List<User> users = userInteractionService.getUserFollowing(userId);
            return ResponseUtil.createSuccessResponse(users, "Usuarios seguidos obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener usuarios seguidos: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/followers")
    public ResponseEntity<Map<String, Object>> getUserFollowers(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            List<User> users = userInteractionService.getUserFollowers(userId);
            return ResponseUtil.createSuccessResponse(users, "Seguidores obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener seguidores: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/similar-users")
    public ResponseEntity<Map<String, Object>> getUsersWithSimilarTaste(@RequestHeader("Authorization") String authHeader,
                                                                       @RequestParam(defaultValue = "10") Integer limit) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            List<User> users = userInteractionService.getUsersWithSimilarTaste(userId, limit);
            return ResponseUtil.createSuccessResponse(users, "Usuarios con gustos similares obtenidos exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener usuarios con gustos similares: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/stats/average-rating")
    public ResponseEntity<Map<String, Object>> getUserAverageRating(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            Double averageRating = userInteractionService.getUserAverageRating(userId);
            return ResponseUtil.createSuccessResponse(averageRating, "Calificación promedio obtenida exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener calificación promedio: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/stats/completed-books")
    public ResponseEntity<Map<String, Object>> getUserCompletedBooksCount(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            Long completedBooks = userInteractionService.getUserCompletedBooksCount(userId);
            return ResponseUtil.createSuccessResponse(completedBooks, "Conteo de libros completados obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener conteo de libros completados: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/stats/reading-time")
    public ResponseEntity<Map<String, Object>> getUserTotalReadingTime(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            Long totalReadingTime = userInteractionService.getUserTotalReadingTime(userId);
            return ResponseUtil.createSuccessResponse(totalReadingTime, "Tiempo total de lectura obtenido exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener tiempo total de lectura: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/rating/{bookId}")
    public ResponseEntity<Map<String, Object>> getUserRatingForBook(@RequestHeader("Authorization") String authHeader,
                                                                   @PathVariable String bookId) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            Map<String, Object> rating = userInteractionService.getUserRatingForBook(userId, bookId);
            if (rating != null) {
                return ResponseUtil.createSuccessResponse(rating, "Calificación obtenida exitosamente");
            } else {
                return ResponseUtil.createSuccessResponse(null, "El usuario no ha calificado este libro");
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener calificación: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/recommendation-feedback/{bookId}/clicked")
    public ResponseEntity<Map<String, Object>> markRecommendationAsClicked(@RequestHeader("Authorization") String authHeader,
                                                                          @PathVariable String bookId) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            boolean success = userInteractionService.markRecommendationAsClicked(userId, bookId);
            if (success) {
                return ResponseUtil.createSuccessResponse(null, "Recomendación marcada como clickeada exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Error al marcar recomendación como clickeada", 400);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al marcar recomendación como clickeada: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/recommendation-feedback/{bookId}/liked")
    public ResponseEntity<Map<String, Object>> markRecommendationAsLiked(@RequestHeader("Authorization") String authHeader,
                                                                        @PathVariable String bookId,
                                                                        @RequestParam boolean liked) {
        try {
            String token = authHeader.substring(7); // Remover "Bearer "
            String userId = jwtService.extractUserId(token);
            
            boolean success = userInteractionService.markRecommendationAsLiked(userId, bookId, liked);
            if (success) {
                return ResponseUtil.createSuccessResponse(null, "Feedback de recomendación registrado exitosamente");
            } else {
                return ResponseUtil.createErrorResponse("Error al registrar feedback de recomendación", 400);
            }
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al registrar feedback de recomendación: " + e.getMessage(), 500);
        }
    }
}