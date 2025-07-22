package com.booknet.backend.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ResponseUtil {

    // Métodos que retornan Map (para uso interno)
    public static Map<String, Object> createSuccessResponseMap(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    public static Map<String, Object> createErrorResponseMap(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // Métodos que retornan ResponseEntity con códigos de estado HTTP (para controladores)
    public static ResponseEntity<Map<String, Object>> createSuccessResponse(Object data, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<Map<String, Object>> createErrorResponse(String message, int statusCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(statusCode).body(response);
    }

    // Métodos legacy para compatibilidad con otros controladores
    public static ResponseEntity<?> success(Object data, String message) {
        return ResponseEntity.ok(createSuccessResponseMap(message, data));
    }

    public static ResponseEntity<?> error(String message) {
        return ResponseEntity.badRequest().body(createErrorResponseMap(message));
    }
}