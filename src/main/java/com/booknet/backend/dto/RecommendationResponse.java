package com.booknet.backend.dto;

import java.time.LocalDateTime;

public class RecommendationResponse {
    private BookResponse book;
    private String algorithmUsed;
    private Double confidenceScore;
    private String recommendationReason;
    private LocalDateTime recommendedDate;

    public RecommendationResponse() {}

    public RecommendationResponse(BookResponse book, String algorithmUsed, Double confidenceScore, 
                                 String recommendationReason, LocalDateTime recommendedDate) {
        this.book = book;
        this.algorithmUsed = algorithmUsed;
        this.confidenceScore = confidenceScore;
        this.recommendationReason = recommendationReason;
        this.recommendedDate = recommendedDate;
    }

    // Getters and Setters
    public BookResponse getBook() {
        return book;
    }

    public void setBook(BookResponse book) {
        this.book = book;
    }

    public String getAlgorithmUsed() {
        return algorithmUsed;
    }

    public void setAlgorithmUsed(String algorithmUsed) {
        this.algorithmUsed = algorithmUsed;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getRecommendationReason() {
        return recommendationReason;
    }

    public void setRecommendationReason(String recommendationReason) {
        this.recommendationReason = recommendationReason;
    }

    public LocalDateTime getRecommendedDate() {
        return recommendedDate;
    }

    public void setRecommendedDate(LocalDateTime recommendedDate) {
        this.recommendedDate = recommendedDate;
    }
}