package com.booknet.backend.dto;

public class UpdateGenreRequest {
    private String nombre;
    private String descripcion;
    private String genero_padre;

    public UpdateGenreRequest() {}

    // Getters and Setters
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
