package com.booknet.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BookResponse {
    private String id;
    private String title;
    private String isbn;
    private String description;
    private Integer publicationYear;
    private Integer pageCount;
    private String language;
    private String coverImage;
    private String ageRating;
    private Double averageRating;
    private Integer totalRatings;
    private String readingDifficulty;
    private LocalDateTime createdAt;
    private List<AuthorResponse> authors;
    private List<GenreResponse> genres;
    private List<TagResponse> tags;
    private SeriesResponse series;
    private Integer orderInSeries;
    private Integer totalChapters;

    public BookResponse() {}

    public BookResponse(String id, String title, String isbn, String description, 
                       Integer publicationYear, Integer pageCount, String language, 
                       String coverImage, String ageRating, Double averageRating, 
                       Integer totalRatings, String readingDifficulty, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.isbn = isbn;
        this.description = description;
        this.publicationYear = publicationYear;
        this.pageCount = pageCount;
        this.language = language;
        this.coverImage = coverImage;
        this.ageRating = ageRating;
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
        this.readingDifficulty = readingDifficulty;
        this.createdAt = createdAt;
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

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getAgeRating() {
        return ageRating;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(Integer totalRatings) {
        this.totalRatings = totalRatings;
    }

    public String getReadingDifficulty() {
        return readingDifficulty;
    }

    public void setReadingDifficulty(String readingDifficulty) {
        this.readingDifficulty = readingDifficulty;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<AuthorResponse> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AuthorResponse> authors) {
        this.authors = authors;
    }

    public List<GenreResponse> getGenres() {
        return genres;
    }

    public void setGenres(List<GenreResponse> genres) {
        this.genres = genres;
    }

    public List<TagResponse> getTags() {
        return tags;
    }

    public void setTags(List<TagResponse> tags) {
        this.tags = tags;
    }

    public SeriesResponse getSeries() {
        return series;
    }

    public void setSeries(SeriesResponse series) {
        this.series = series;
    }

    public Integer getOrderInSeries() {
        return orderInSeries;
    }

    public void setOrderInSeries(Integer orderInSeries) {
        this.orderInSeries = orderInSeries;
    }

    public Integer getTotalChapters() {
        return totalChapters;
    }

    public void setTotalChapters(Integer totalChapters) {
        this.totalChapters = totalChapters;
    }
}