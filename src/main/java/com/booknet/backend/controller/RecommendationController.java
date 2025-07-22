package com.booknet.backend.controller;

import com.booknet.backend.dto.RecommendationResponse;
import com.booknet.backend.service.JwtService;
import com.booknet.backend.service.RecommendationService;
import com.booknet.backend.util.ResponseUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final JwtService jwtService;

    public RecommendationController(RecommendationService recommendationService, JwtService jwtService) {
        this.recommendationService = recommendationService;
        this.jwtService = jwtService;
    }

    @GetMapping("/personalized")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getPersonalizedRecommendations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.createErrorResponse("Token de autorización requerido", 401);
            }

            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);

            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getPersonalizedRecommendations(userId, limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones personalizadas obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones personalizadas: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/collaborative")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCollaborativeRecommendations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.createErrorResponse("Token de autorización requerido", 401);
            }

            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);

            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getCollaborativeRecommendations(userId, limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones colaborativas obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones colaborativas: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/content-based")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getContentBasedRecommendations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.createErrorResponse("Token de autorización requerido", 401);
            }

            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);

            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getContentBasedRecommendations(userId, limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones basadas en contenido obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones basadas en contenido: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<Map<String, Object>> getPopularRecommendations(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getPopularRecommendations(limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones populares obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones populares: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/new-books")
    public ResponseEntity<Map<String, Object>> getNewBooksRecommendations(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getNewBooksRecommendations(limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones de libros nuevos obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones de libros nuevos: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingRecommendations(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getTrendingRecommendations(limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones de tendencia obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones de tendencia: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/hybrid")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getHybridRecommendations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "15") Integer limit) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseUtil.createErrorResponse("Token de autorización requerido", 401);
            }

            String token = authHeader.substring(7);
            String userId = jwtService.extractUserId(token);

            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getHybridRecommendations(userId, limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones híbridas obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones híbridas: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/genre/{genreName}")
    public ResponseEntity<Map<String, Object>> getRecommendationsByGenre(
            @PathVariable String genreName,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (genreName == null || genreName.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El nombre del género es requerido", 400);
            }

            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getRecommendationsByGenre(genreName.trim(), limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones por género obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones por género: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/author/{authorName}")
    public ResponseEntity<Map<String, Object>> getRecommendationsByAuthor(
            @PathVariable String authorName,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (authorName == null || authorName.trim().isEmpty()) {
                return ResponseUtil.createErrorResponse("El nombre del autor es requerido", 400);
            }

            if (limit <= 0 || limit > 50) {
                return ResponseUtil.createErrorResponse("El límite debe estar entre 1 y 50", 400);
            }

            List<RecommendationResponse> recommendations = 
                    recommendationService.getRecommendationsByAuthor(authorName.trim(), limit);
            
            return ResponseUtil.createSuccessResponse(recommendations, "Recomendaciones por autor obtenidas exitosamente");
        } catch (Exception e) {
            return ResponseUtil.createErrorResponse("Error al obtener recomendaciones por autor: " + e.getMessage(), 500);
        }
    }
}