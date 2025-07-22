package com.booknet.backend.model;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Node("Book")
public class Book {

    @Id
    private String id;

    @Property("title")
    private String title;

    @Property("isbn")
    private String isbn;

    @Property("description")
    private String description;

    @Property("publication_year")
    private Integer publicationYear;

    @Property("page_count")
    private Integer pageCount;

    @Property("language")
    private String language;

    @Property("cover_image")
    private String coverImage;

    @Property("age_rating")
    private String ageRating;

    @Property("average_rating")
    private Double averageRating;

    @Property("total_ratings")
    private Integer totalRatings;

    @Property("reading_difficulty")
    private String readingDifficulty;

    @Property("created_at")
    private LocalDateTime createdAt;

    @Property("updated_at")
    private LocalDateTime updatedAt;

    @Property("order_in_series")
    private Integer orderInSeries;

    @Relationship(type = "WRITTEN_BY", direction = Relationship.Direction.OUTGOING)
    private Set<Author> authors = new HashSet<>();

    @Relationship(type = "BELONGS_TO_GENRE", direction = Relationship.Direction.OUTGOING)
    private Set<Genre> genres = new HashSet<>();

    @Relationship(type = "HAS_TAG", direction = Relationship.Direction.OUTGOING)
    private Set<Tag> tags = new HashSet<>();

    @Relationship(type = "HAS_CHAPTER", direction = Relationship.Direction.OUTGOING)
    private Set<Chapter> chapters = new HashSet<>();

    @Relationship(type = "PART_OF_SERIES", direction = Relationship.Direction.OUTGOING)
    private Series series;

    public Book() {
        this.id = UUID.randomUUID().toString();
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Book(String title, String isbn, String description, Integer publicationYear, 
                Integer pageCount, String language, String ageRating, String readingDifficulty) {
        this();
        this.title = title;
        this.isbn = isbn;
        this.description = description;
        this.publicationYear = publicationYear;
        this.pageCount = pageCount;
        this.language = language;
        this.ageRating = ageRating;
        this.readingDifficulty = readingDifficulty;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getOrderInSeries() {
        return orderInSeries;
    }

    public void setOrderInSeries(Integer orderInSeries) {
        this.orderInSeries = orderInSeries;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public Set<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(Set<Chapter> chapters) {
        this.chapters = chapters;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publicationYear=" + publicationYear +
                ", averageRating=" + averageRating +
                ", totalRatings=" + totalRatings +
                '}';
    }
}