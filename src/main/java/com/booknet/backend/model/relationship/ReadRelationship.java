package com.booknet.backend.model.relationship;

import com.booknet.backend.model.Book;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import java.time.LocalDateTime;

@RelationshipProperties
public class ReadRelationship {

    @Id
    @GeneratedValue
    private Long id;

    @Property("status")
    private String status; // "reading", "completed", "abandoned", "want_to_read"

    @Property("progress_percentage")
    private Double progressPercentage;

    @Property("current_chapter")
    private String currentChapter;

    @Property("start_date")
    private LocalDateTime startDate;

    @Property("finish_date")
    private LocalDateTime finishDate;

    @Property("reading_sessions")
    private Integer readingSessions;

    @Property("total_reading_time_minutes")
    private Integer totalReadingTimeMinutes;

    @TargetNode
    private Book book;

    public ReadRelationship() {
        this.progressPercentage = 0.0;
        this.readingSessions = 0;
        this.totalReadingTimeMinutes = 0;
        this.status = "want_to_read";
    }

    public ReadRelationship(String status) {
        this();
        this.status = status;
        if ("reading".equals(status)) {
            this.startDate = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("reading".equals(status) && this.startDate == null) {
            this.startDate = LocalDateTime.now();
        } else if ("completed".equals(status)) {
            this.finishDate = LocalDateTime.now();
            this.progressPercentage = 100.0;
        }
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
        if (progressPercentage != null && progressPercentage >= 100.0) {
            this.status = "completed";
            this.finishDate = LocalDateTime.now();
        }
    }

    public String getCurrentChapter() {
        return currentChapter;
    }

    public void setCurrentChapter(String currentChapter) {
        this.currentChapter = currentChapter;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }

    public Integer getReadingSessions() {
        return readingSessions;
    }

    public void setReadingSessions(Integer readingSessions) {
        this.readingSessions = readingSessions;
    }

    public Integer getTotalReadingTimeMinutes() {
        return totalReadingTimeMinutes;
    }

    public void setTotalReadingTimeMinutes(Integer totalReadingTimeMinutes) {
        this.totalReadingTimeMinutes = totalReadingTimeMinutes;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "ReadRelationship{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", progressPercentage=" + progressPercentage +
                ", readingSessions=" + readingSessions +
                ", totalReadingTimeMinutes=" + totalReadingTimeMinutes +
                '}';
    }
}