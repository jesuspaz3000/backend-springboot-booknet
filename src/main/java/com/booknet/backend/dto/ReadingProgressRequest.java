package com.booknet.backend.dto;

public class ReadingProgressRequest {
    private String bookId;
    private String status; // "reading", "completed", "abandoned", "want_to_read"
    private Double progressPercentage;
    private String currentChapter;
    private Integer readingTimeMinutes;

    public ReadingProgressRequest() {}

    public ReadingProgressRequest(String bookId, String status, Double progressPercentage, 
                                 String currentChapter, Integer readingTimeMinutes) {
        this.bookId = bookId;
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.currentChapter = currentChapter;
        this.readingTimeMinutes = readingTimeMinutes;
    }

    // Getters and Setters
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getCurrentChapter() {
        return currentChapter;
    }

    public void setCurrentChapter(String currentChapter) {
        this.currentChapter = currentChapter;
    }

    public Integer getReadingTimeMinutes() {
        return readingTimeMinutes;
    }

    public void setReadingTimeMinutes(Integer readingTimeMinutes) {
        this.readingTimeMinutes = readingTimeMinutes;
    }
}