package com.booknet.backend.model.relationship;

import com.booknet.backend.model.Book;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
public class SimilarToRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property("similarity_score")
    private Double similarityScore; // 0.0 - 1.0

    @Property("similarity_type")
    private String similarityType; // "genre", "author", "theme", "style", "user_behavior"

    @Property("calculated_date")
    private LocalDateTime calculatedDate;

    @TargetNode
    private Book similarBook;

    public SimilarToRelationship() {
        this.calculatedDate = LocalDateTime.now();
    }

    public SimilarToRelationship(Double similarityScore, String similarityType) {
        this();
        this.similarityScore = similarityScore;
        this.similarityType = similarityType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public String getSimilarityType() {
        return similarityType;
    }

    public void setSimilarityType(String similarityType) {
        this.similarityType = similarityType;
    }

    public LocalDateTime getCalculatedDate() {
        return calculatedDate;
    }

    public void setCalculatedDate(LocalDateTime calculatedDate) {
        this.calculatedDate = calculatedDate;
    }

    public Book getSimilarBook() {
        return similarBook;
    }

    public void setSimilarBook(Book similarBook) {
        this.similarBook = similarBook;
    }

    @Override
    public String toString() {
        return "SimilarToRelationship{" +
                "id=" + id +
                ", similarityScore=" + similarityScore +
                ", similarityType='" + similarityType + '\'' +
                ", calculatedDate=" + calculatedDate +
                '}';
    }
}