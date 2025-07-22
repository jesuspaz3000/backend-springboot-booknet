package com.booknet.backend.dto;

public class GenreResponse {
    private String id;
    private String nombre;
    private String descripcion;
    private String genero_padre;

    public GenreResponse() {}

    public GenreResponse(String id, String nombre, String descripcion, String genero_padre) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.genero_padre = genero_padre;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getGenero_padre() {
        return genero_padre;
    }

    public void setGenero_padre(String genero_padre) {
        this.genero_padre = genero_padre;
    }
}