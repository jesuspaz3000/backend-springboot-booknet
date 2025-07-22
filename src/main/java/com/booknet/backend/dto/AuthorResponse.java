package com.booknet.backend.dto;

import java.time.LocalDate;

public class AuthorResponse {
    private String id;
    private String nombre;
    private String acerca_de;
    private LocalDate fechaNacimiento;
    private LocalDate fechaMuerte;
    private String nacionalidad;
    private String foto;
    private Integer cantidad_de_libros;

    public AuthorResponse() {}

    public AuthorResponse(String id, String nombre, String acerca_de, LocalDate fechaNacimiento, 
                         LocalDate fechaMuerte, String nacionalidad, String foto, Integer cantidad_de_libros) {
        this.id = id;
        this.nombre = nombre;
        this.acerca_de = acerca_de;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaMuerte = fechaMuerte;
        this.nacionalidad = nacionalidad;
        this.foto = foto;
        this.cantidad_de_libros = cantidad_de_libros;
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

    public String getAcerca_de() {
        return acerca_de;
    }

    public void setAcerca_de(String acerca_de) {
        this.acerca_de = acerca_de;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaMuerte() {
        return fechaMuerte;
    }

    public void setFechaMuerte(LocalDate fechaMuerte) {
        this.fechaMuerte = fechaMuerte;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Integer getCantidad_de_libros() {
        return cantidad_de_libros;
    }

    public void setCantidad_de_libros(Integer cantidad_de_libros) {
        this.cantidad_de_libros = cantidad_de_libros;
    }
}