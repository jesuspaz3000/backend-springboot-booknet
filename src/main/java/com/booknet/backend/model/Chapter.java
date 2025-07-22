package com.booknet.backend.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.UUID;

@Node("Chapter")
public class Chapter {

    @Id
    private String id;

    @Property("title")
    private String title;

    @Property("content")
    private String content;

    @Property("chapter_number")
    private Integer chapterNumber;

    @Property("word_count")
    private Integer wordCount;

    @Property("reading_time_minutes")
    private Integer readingTimeMinutes;

    @Property("created_at")
    private LocalDateTime createdAt;

    @Relationship(type = "HAS_CHAPTER", direction = Relationship.Direction.INCOMING)
    private Book book;

    public Chapter() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    public Chapter(String title, String content, Integer chapterNumber) {
        this();
        this.title = title;
        this.content = content;
        this.chapterNumber = chapterNumber;
        this.wordCount = calculateWordCount(content);
        this.readingTimeMinutes = calculateReadingTime(this.wordCount);
    }

    private Integer calculateWordCount(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }
        return content.trim().split("\\s+").length;
    }

    private Integer calculateReadingTime(Integer wordCount) {
        if (wordCount == null || wordCount == 0) {
            return 0;
        }
        // Assuming average reading speed of 200 words per minute
        return Math.max(1, (int) Math.ceil(wordCount / 200.0));
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.wordCount = calculateWordCount(content);
        this.readingTimeMinutes = calculateReadingTime(this.wordCount);
    }

    public Integer getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(Integer chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public Integer getReadingTimeMinutes() {
        return readingTimeMinutes;
    }

    public void setReadingTimeMinutes(Integer readingTimeMinutes) {
        this.readingTimeMinutes = readingTimeMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", chapterNumber=" + chapterNumber +
                ", wordCount=" + wordCount +
                ", readingTimeMinutes=" + readingTimeMinutes +
                '}';
    }
}