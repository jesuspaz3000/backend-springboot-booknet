package com.booknet.backend.service;

import com.booknet.backend.dto.AuthorResponse;
import com.booknet.backend.dto.CreateAuthorRequest;
import com.booknet.backend.dto.UpdateAuthorRequest;
import com.booknet.backend.model.Author;
import com.booknet.backend.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    // Crear autor
    public AuthorResponse createAuthor(CreateAuthorRequest request) {
        Author author = new Author();
        author.setName(request.getNombre());
        author.setBiography(request.getAcerca_de());
        author.setBirthDate(request.getFechaNacimiento());
        author.setDeathDate(request.getFechaMuerte());
        author.setNationality(request.getNacionalidad());
        author.setPhoto(request.getFoto());
        author.setTotalBooks(0);
        author.setCreatedAt(LocalDateTime.now());
        author.setUpdatedAt(LocalDateTime.now());

        Author savedAuthor = authorRepository.save(author);
        return convertToAuthorResponse(savedAuthor);
    }

    // Obtener autor por ID
    public Optional<AuthorResponse> getAuthorById(String id) {
        return authorRepository.findById(id)
                .map(this::convertToAuthorResponse);
    }

    // Listar autores con paginación simple
    public List<Author> getAllAuthorsWithSimplePagination(int offset, int limit) {
        // Validar parámetros
        if (offset < 0) {
            offset = 0;
        }
        if (limit <= 0 || limit > 100) { // Máximo 100 autores por página
            limit = 20; // Por defecto 20
        }
        
        return authorRepository.findAllOrderByCreatedAtDescWithPagination(offset, limit);
    }

    // Contar total de autores
    public long getTotalAuthorsCount() {
        return authorRepository.countAllAuthors();
    }

    // Obtener todos los autores sin paginación
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    // Actualizar autor (PATCH)
    public AuthorResponse updateAuthor(String id, UpdateAuthorRequest request) {
        Optional<Author> authorOpt = authorRepository.findById(id);
        if (authorOpt.isEmpty()) {
            throw new RuntimeException("Autor no encontrado");
        }

        Author author = authorOpt.get();
        boolean updated = false;

        // Actualizar nombre si se proporciona
        if (request.getNombre() != null && !request.getNombre().trim().isEmpty()) {
            author.setName(request.getNombre().trim());
            updated = true;
        }

        // Actualizar biografía si se proporciona
        if (request.getAcerca_de() != null && !request.getAcerca_de().trim().isEmpty()) {
            author.setBiography(request.getAcerca_de().trim());
            updated = true;
        }

        // Actualizar fecha de nacimiento si se proporciona
        if (request.getFechaNacimiento() != null) {
            author.setBirthDate(request.getFechaNacimiento());
            updated = true;
        }

        // Actualizar fecha de muerte si se proporciona
        if (request.getFechaMuerte() != null) {
            author.setDeathDate(request.getFechaMuerte());
            updated = true;
        }

        // Actualizar nacionalidad si se proporciona
        if (request.getNacionalidad() != null && !request.getNacionalidad().trim().isEmpty()) {
            author.setNationality(request.getNacionalidad().trim());
            updated = true;
        }

        // Actualizar foto si se proporciona
        if (request.getFoto() != null && !request.getFoto().trim().isEmpty()) {
            author.setPhoto(request.getFoto().trim());
            updated = true;
        }

        if (updated) {
            author.setUpdatedAt(LocalDateTime.now());
            Author savedAuthor = authorRepository.save(author);
            return convertToAuthorResponse(savedAuthor);
        }

        return convertToAuthorResponse(author);
    }

    // Eliminar autor
    public void deleteAuthor(String id) {
        Optional<Author> authorOpt = authorRepository.findById(id);
        if (authorOpt.isEmpty()) {
            throw new RuntimeException("Autor no encontrado");
        }

        Author author = authorOpt.get();
        
        // Verificar si el autor tiene libros asociados
        if (author.getTotalBooks() != null && author.getTotalBooks() > 0) {
            throw new RuntimeException("No se puede eliminar el autor porque tiene libros asociados");
        }

        authorRepository.delete(author);
    }

    // Convertir Author a AuthorResponse
    public AuthorResponse convertToAuthorResponse(Author author) {
        AuthorResponse response = new AuthorResponse();
        response.setId(author.getId());
        response.setNombre(author.getName());
        response.setAcerca_de(author.getBiography());
        response.setFechaNacimiento(author.getBirthDate());
        response.setFechaMuerte(author.getDeathDate());
        response.setNacionalidad(author.getNationality());
        response.setFoto(author.getPhoto());
        
        // Calcular dinámicamente la cantidad de libros
        int actualBookCount = 0;
        
        // Método 1: Intentar usar las relaciones cargadas
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            actualBookCount = author.getBooks().size();
        } else {
            // Método 2: Consulta directa a la base de datos como respaldo
            actualBookCount = (int) authorRepository.countBooksByAuthorId(author.getId());
        }
        
        response.setCantidad_de_libros(actualBookCount);
        
        return response;
    }
}
