package com.booknet.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PaginatedTagsResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private Data data;

    public PaginatedTagsResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public PaginatedTagsResponse(boolean success, String message, List<TagResponse> tags, int total, int limit, int offset) {
        this();
        this.success = success;
        this.message = message;
        this.data = new Data(tags, total, limit, offset);
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
        private List<TagResponse> tags;
        private int total;
        private int limit;
        private int offset;

        public Data() {}

        public Data(List<TagResponse> tags, int total, int limit, int offset) {
            this.tags = tags;
            this.total = total;
            this.limit = limit;
            this.offset = offset;
        }

        // Getters and Setters
        public List<TagResponse> getTags() {
            return tags;
        }

        public void setTags(List<TagResponse> tags) {
            this.tags = tags;
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
