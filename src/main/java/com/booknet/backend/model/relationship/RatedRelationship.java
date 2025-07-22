package com.booknet.backend.model.relationship;

import com.booknet.backend.model.Book;
import com.booknet.backend.model.User;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
public class RatedRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property("rating")
    private Double rating; // 1.0 - 5.0

    @Property("review")
    private String review;

    @Property("review_title")
    private String reviewTitle;

    @Property("helpful_votes")
    private Integer helpfulVotes;

    @Property("created_at")
    private LocalDateTime createdAt;

    @Property("updated_at")
    private LocalDateTime updatedAt;

    @TargetNode
    private Book book;

    public RatedRelationship() {
        this.helpfulVotes = 0;
        this.createdAt = LocalDateTime.now();
    }

    public RatedRelationship(Double rating, String review, String reviewTitle) {
        this();
        this.rating = rating;
        this.review = review;
        this.reviewTitle = reviewTitle;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "RatedRelationship{" +
                "id=" + id +
                ", rating=" + rating +
                ", reviewTitle='" + reviewTitle + '\'' +
                ", helpfulVotes=" + helpfulVotes +
                '}';
    }
}