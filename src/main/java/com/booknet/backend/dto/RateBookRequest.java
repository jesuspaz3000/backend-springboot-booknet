package com.booknet.backend.dto;

public class RateBookRequest {
    private String bookId;
    private Double rating; // 1.0 - 5.0
    private String review;
    private String reviewTitle;

    public RateBookRequest() {}

    public RateBookRequest(String bookId, Double rating, String review, String reviewTitle) {
        this.bookId = bookId;
        this.rating = rating;
        this.review = review;
        this.reviewTitle = reviewTitle;
    }

    // Getters and Setters
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
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
}