package com.booknet.backend.model.relationship;

import com.booknet.backend.model.Book;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
public class RecommendedRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property("algorithm_used")
    private String algorithmUsed; // "content_based", "collaborative", "hybrid"

    @Property("confidence_score")
    private Double confidenceScore; // 0.0 - 1.0

    @Property("recommendation_reason")
    private String recommendationReason;

    @Property("recommended_date")
    private LocalDateTime recommendedDate;

    @Property("was_clicked")
    private Boolean wasClicked;

    @Property("was_liked")
    private Boolean wasLiked;

    @TargetNode
    private Book book;

    public RecommendedRelationship() {
        this.recommendedDate = LocalDateTime.now();
        this.wasClicked = false;
        this.wasLiked = null;
    }

    public RecommendedRelationship(String algorithmUsed, Double confidenceScore, String recommendationReason) {
        this();
        this.algorithmUsed = algorithmUsed;
        this.confidenceScore = confidenceScore;
        this.recommendationReason = recommendationReason;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getWasClicked() {
        return wasClicked;
    }

    public void setWasClicked(Boolean wasClicked) {
        this.wasClicked = wasClicked;
    }

    public Boolean getWasLiked() {
        return wasLiked;
    }

    public void setWasLiked(Boolean wasLiked) {
        this.wasLiked = wasLiked;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "RecommendedRelationship{" +
                "id=" + id +
                ", algorithmUsed='" + algorithmUsed + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", recommendationReason='" + recommendationReason + '\'' +
                ", wasClicked=" + wasClicked +
                ", wasLiked=" + wasLiked +
                '}';
    }
}