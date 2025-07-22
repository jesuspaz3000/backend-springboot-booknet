package com.booknet.backend.service;

import com.booknet.backend.dto.CreateGenreRequest;
import com.booknet.backend.dto.GenreResponse;
import com.booknet.backend.dto.UpdateGenreRequest;
import com.booknet.backend.model.Genre;
import com.booknet.backend.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    public GenreResponse createGenre(CreateGenreRequest request) {
        Genre genre = new Genre();
        genre.setName(request.getNombre());
        genre.setDescription(request.getDescripcion());
        genre.setParentGenre(request.getGenero_padre());
        
        Genre savedGenre = genreRepository.save(genre);
        return convertToGenreResponse(savedGenre);
    }

    public Optional<GenreResponse> getGenreById(String id) {
        return genreRepository.findById(id)
                .map(this::convertToGenreResponse);
    }

    public List<GenreResponse> getAllGenresWithPagination(int offset, int limit) {
        List<Genre> genres = genreRepository.findAllOrderByCreatedAtDescWithPagination(offset, limit);
        return genres.stream()
                .map(this::convertToGenreResponse)
                .collect(Collectors.toList());
    }

    public long getTotalGenresCount() {
        return genreRepository.countAllGenres();
    }

    // Obtener todos los géneros sin paginación
    public List<GenreResponse> getAllGenres() {
        List<Genre> genres = genreRepository.findAll();
        return genres.stream()
                .map(this::convertToGenreResponse)
                .collect(Collectors.toList());
    }

    public Optional<GenreResponse> updateGenre(String id, UpdateGenreRequest request) {
        Optional<Genre> genreOptional = genreRepository.findById(id);
        
        if (genreOptional.isPresent()) {
            Genre genre = genreOptional.get();
            
            // Actualizar solo los campos que no son null
            if (request.getNombre() != null) {
                genre.setName(request.getNombre());
            }
            if (request.getDescripcion() != null) {
                genre.setDescription(request.getDescripcion());
            }
            if (request.getGenero_padre() != null) {
                genre.setParentGenre(request.getGenero_padre());
            }
            
            genre.setUpdatedAt(LocalDateTime.now());
            Genre updatedGenre = genreRepository.save(genre);
            return Optional.of(convertToGenreResponse(updatedGenre));
        }
        
        return Optional.empty();
    }

    public boolean deleteGenre(String id) {
        Optional<Genre> genreOptional = genreRepository.findById(id);
        
        if (genreOptional.isPresent()) {
            Genre genre = genreOptional.get();
            
            // Verificar si el género tiene libros asociados
            if (genre.getBooks() != null && !genre.getBooks().isEmpty()) {
                throw new RuntimeException("No se puede eliminar el género porque tiene libros asociados");
            }
            
            genreRepository.deleteById(id);
            return true;
        }
        
        return false;
    }

    public GenreResponse convertToGenreResponse(Genre genre) {
        GenreResponse response = new GenreResponse();
        response.setId(genre.getId());
        response.setNombre(genre.getName());
        response.setDescripcion(genre.getDescription());
        response.setGenero_padre(genre.getParentGenre());
        return response;
    }
}
