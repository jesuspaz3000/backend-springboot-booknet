package com.booknet.backend.dto;

public class BookRatingDTO {
    private String userId;
    private String username;
    private Double rating;
    private String review;
    private String reviewTitle;
    private Integer helpfulVotes;
    private String createdAt;
    private String updatedAt;

    // Constructor vacío
    public BookRatingDTO() {}

    // Constructor con parámetros básicos
    public BookRatingDTO(String userId, String username, Double rating, String review) {
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.review = review;
        this.reviewTitle = null;
        this.helpfulVotes = 0;
        this.createdAt = null;
        this.updatedAt = null;
    }

    // Getters y Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getReviewTitle() {
        return reviewTitle;
    }

    public void setReviewTitle(String reviewTitle) {
        this.reviewTitle = reviewTitle;
    }

    public Integer getHelpfulVotes() {
        return helpfulVotes;
    }

    public void setHelpfulVotes(Integer helpfulVotes) {
        this.helpfulVotes = helpfulVotes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
