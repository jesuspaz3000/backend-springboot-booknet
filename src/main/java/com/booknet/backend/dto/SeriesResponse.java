package com.booknet.backend.dto;

public class SeriesResponse {
    private String id;
    private String name;
    private String description;
    private Integer totalBooks;
    private Boolean isCompleted;

    public SeriesResponse() {}

    public SeriesResponse(String id, String name, String description, Integer totalBooks, Boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalBooks = totalBooks;
        this.isCompleted = isCompleted;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(Integer totalBooks) {
        this.totalBooks = totalBooks;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }

    public void setIsCompleted(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}