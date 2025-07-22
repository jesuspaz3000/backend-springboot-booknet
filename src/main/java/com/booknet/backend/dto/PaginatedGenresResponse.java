package com.booknet.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PaginatedGenresResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private Data data;

    public PaginatedGenresResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public PaginatedGenresResponse(boolean success, String message, List<GenreResponse> generos, int total, int limit, int offset) {
        this();
        this.success = success;
        this.message = message;
        this.data = new Data(generos, total, limit, offset);
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private List<GenreResponse> generos;
        private int total;
        private int limit;
        private int offset;

        public Data() {}

        public Data(List<GenreResponse> generos, int total, int limit, int offset) {
            this.generos = generos;
            this.total = total;
            this.limit = limit;
            this.offset = offset;
        }

        // Getters and Setters
        public List<GenreResponse> getGeneros() {
            return generos;
        }

        public void setGeneros(List<GenreResponse> generos) {
            this.generos = generos;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }
    }
}
