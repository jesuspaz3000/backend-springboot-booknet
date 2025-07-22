package com.booknet.backend.dto;

import java.time.LocalDate;
import java.util.List;

public class EnhancedBulkCreateBookRequest {
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
    private List<AuthorData> authors;
    private List<GenreData> genres;
    private List<TagData> tags;
    private String seriesName;
    private Integer orderInSeries;

    public EnhancedBulkCreateBookRequest() {}

    // Clase interna para datos de autor
    public static class AuthorData {
        private String nombre;
        private String acerca_de;
        private LocalDate fechaNacimiento;
        private LocalDate fechaMuerte;
        private String nacionalidad;
        private String foto;

        public AuthorData() {}

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getAcerca_de() { return acerca_de; }
        public void setAcerca_de(String acerca_de) { this.acerca_de = acerca_de; }
        
        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        
        public LocalDate getFechaMuerte() { return fechaMuerte; }
        public void setFechaMuerte(LocalDate fechaMuerte) { this.fechaMuerte = fechaMuerte; }
        
        public String getNacionalidad() { return nacionalidad; }
        public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }
        
        public String getFoto() { return foto; }
        public void setFoto(String foto) { this.foto = foto; }
    }

    // Clase interna para datos de género
    public static class GenreData {
        private String nombre;
        private String descripcion;
        private String genero_padre;

        public GenreData() {}

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        
        public String getGenero_padre() { return genero_padre; }
        public void setGenero_padre(String genero_padre) { this.genero_padre = genero_padre; }
    }

    // Clase interna para datos de tag
    public static class TagData {
        private String nombre;
        private String categoria;

        public TagData() {}

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        
        public String getCategoria() { return categoria; }
        public void setCategoria(String categoria) { this.categoria = categoria; }
    }

    // Getters and Setters principales
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getTotalRatings() { return totalRatings; }
    public void setTotalRatings(Integer totalRatings) { this.totalRatings = totalRatings; }

    public String getReadingDifficulty() { return readingDifficulty; }
    public void setReadingDifficulty(String readingDifficulty) { this.readingDifficulty = readingDifficulty; }

    public List<AuthorData> getAuthors() { return authors; }
    public void setAuthors(List<AuthorData> authors) { this.authors = authors; }

    public List<GenreData> getGenres() { return genres; }
    public void setGenres(List<GenreData> genres) { this.genres = genres; }

    public List<TagData> getTags() { return tags; }
    public void setTags(List<TagData> tags) { this.tags = tags; }

    public String getSeriesName() { return seriesName; }
    public void setSeriesName(String seriesName) { this.seriesName = seriesName; }

    public Integer getOrderInSeries() { return orderInSeries; }
    public void setOrderInSeries(Integer orderInSeries) { this.orderInSeries = orderInSeries; }
}
