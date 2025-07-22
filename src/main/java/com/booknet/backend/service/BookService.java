package com.booknet.backend.service;

import com.booknet.backend.dto.*;
import com.booknet.backend.model.*;
import com.booknet.backend.model.relationship.RatedRelationship;
import com.booknet.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final TagRepository tagRepository;
    private final SeriesRepository seriesRepository;
    private final ChapterRepository chapterRepository;
    private final UserRepository userRepository;
    private final Neo4jClient neo4jClient;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, 
                      GenreRepository genreRepository, TagRepository tagRepository, 
                      SeriesRepository seriesRepository, ChapterRepository chapterRepository,
                      UserRepository userRepository, Neo4jClient neo4jClient) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.tagRepository = tagRepository;
        this.seriesRepository = seriesRepository;
        this.chapterRepository = chapterRepository;
        this.userRepository = userRepository;
        this.neo4jClient = neo4jClient;
    }

    public BookResponse createBook(CreateBookRequest request) {
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setPageCount(request.getPageCount());
        book.setLanguage(request.getLanguage());
        book.setCoverImage(request.getCoverImage());
        book.setAgeRating(request.getAgeRating());
        book.setReadingDifficulty(request.getReadingDifficulty());
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());

        // Add authors
        if (request.getAuthorIds() != null) {
            for (String authorId : request.getAuthorIds()) {
                authorRepository.findById(authorId).ifPresent(author -> {
                    book.getAuthors().add(author);
                });
            }
        }

        // Add genres
        if (request.getGenreIds() != null) {
            for (String genreId : request.getGenreIds()) {
                genreRepository.findById(genreId).ifPresent(genre -> {
                    book.getGenres().add(genre);
                });
            }
        }

        // Add tags
        if (request.getTagIds() != null) {
            for (String tagId : request.getTagIds()) {
                tagRepository.findById(tagId).ifPresent(tag -> {
                    book.getTags().add(tag);
                });
            }
        }

        // Add series
        if (request.getSeriesId() != null) {
            seriesRepository.findById(request.getSeriesId()).ifPresent(series -> {
                book.setSeries(series);
                book.setOrderInSeries(request.getOrderInSeries());
            });
        }

        Book savedBook = bookRepository.save(book);
        return convertToBookResponse(savedBook);
    }

    // Método para carga masiva de libros
    public List<BookResponse> createBooksBulk(List<BulkCreateBookRequest> requests) {
        List<BookResponse> createdBooks = new ArrayList<>();
        
        for (BulkCreateBookRequest request : requests) {
            try {
                Book book = new Book();
                book.setTitle(request.getTitle());
                book.setIsbn(request.getIsbn());
                book.setDescription(request.getDescription());
                book.setPublicationYear(request.getPublicationYear());
                book.setPageCount(request.getPageCount());
                book.setLanguage(request.getLanguage());
                book.setCoverImage(request.getCoverImage());
                book.setAgeRating(request.getAgeRating());
                book.setAverageRating(request.getAverageRating());
                book.setTotalRatings(request.getTotalRatings());
                book.setReadingDifficulty(request.getReadingDifficulty());
                book.setCreatedAt(LocalDateTime.now());
                book.setUpdatedAt(LocalDateTime.now());

                // Crear o encontrar autores por nombre
                if (request.getAuthorNames() != null) {
                    for (String authorName : request.getAuthorNames()) {
                        Author author = findOrCreateAuthor(authorName.trim());
                        book.getAuthors().add(author);
                    }
                }

                // Crear o encontrar géneros por nombre
                if (request.getGenreNames() != null) {
                    for (String genreName : request.getGenreNames()) {
                        Genre genre = findOrCreateGenre(genreName.trim());
                        book.getGenres().add(genre);
                    }
                }

                // Crear o encontrar etiquetas por nombre
                if (request.getTagNames() != null) {
                    for (String tagName : request.getTagNames()) {
                        Tag tag = findOrCreateTag(tagName.trim());
                        book.getTags().add(tag);
                    }
                }

                // Crear o encontrar serie por nombre
                if (request.getSeriesName() != null && !request.getSeriesName().trim().isEmpty()) {
                    Series series = findOrCreateSeries(request.getSeriesName().trim());
                    book.setSeries(series);
                    book.setOrderInSeries(request.getOrderInSeries());
                }

                Book savedBook = bookRepository.save(book);
                createdBooks.add(convertToBookResponse(savedBook));
            } catch (Exception e) {
                // Log error but continue with other books
                System.err.println("Error creating book: " + request.getTitle() + " - " + e.getMessage());
            }
        }
        
        return createdBooks;
    }

    // Método para carga masiva de libros con datos completos de autores, géneros y tags
    public List<BookResponse> createBooksEnhancedBulk(List<EnhancedBulkCreateBookRequest> requests) {
        List<BookResponse> createdBooks = new ArrayList<>();
        int skippedBooks = 0;
        
        for (EnhancedBulkCreateBookRequest request : requests) {
            try {
                // Verificar si el libro ya existe por ISBN o título
                Book existingBook = null;
                
                // Buscar por ISBN si está disponible
                if (request.getIsbn() != null && !request.getIsbn().trim().isEmpty()) {
                    existingBook = bookRepository.findByIsbn(request.getIsbn().trim()).orElse(null);
                }
                
                // Si no se encontró por ISBN, buscar por título exacto
                if (existingBook == null && request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
                    List<Book> booksByTitle = bookRepository.findByTitleContainingIgnoreCase(request.getTitle().trim());
                    for (Book book : booksByTitle) {
                        if (book.getTitle().equalsIgnoreCase(request.getTitle().trim())) {
                            existingBook = book;
                            break;
                        }
                    }
                }
                
                // Si el libro ya existe, saltarlo
                if (existingBook != null) {
                    System.out.println("LIBRO YA EXISTE - Saltando: " + request.getTitle() + " (ISBN: " + request.getIsbn() + ")");
                    skippedBooks++;
                    continue;
                }
                
                System.out.println("CREANDO NUEVO LIBRO: " + request.getTitle());
                
                Book book = new Book();
                book.setTitle(request.getTitle());
                book.setIsbn(request.getIsbn());
                book.setDescription(request.getDescription());
                book.setPublicationYear(request.getPublicationYear());
                book.setPageCount(request.getPageCount());
                book.setLanguage(request.getLanguage());
                book.setCoverImage(request.getCoverImage());
                book.setAgeRating(request.getAgeRating());
                book.setAverageRating(request.getAverageRating());
                book.setTotalRatings(request.getTotalRatings());
                book.setReadingDifficulty(request.getReadingDifficulty());
                book.setCreatedAt(LocalDateTime.now());
                book.setUpdatedAt(LocalDateTime.now());

                // Crear o encontrar autores con datos completos
                if (request.getAuthors() != null) {
                    for (EnhancedBulkCreateBookRequest.AuthorData authorData : request.getAuthors()) {
                        Author author = findOrCreateAuthorWithFullData(authorData);
                        book.getAuthors().add(author);
                    }
                }

                // Crear o encontrar géneros con datos completos
                if (request.getGenres() != null) {
                    for (EnhancedBulkCreateBookRequest.GenreData genreData : request.getGenres()) {
                        Genre genre = findOrCreateGenreWithFullData(genreData);
                        book.getGenres().add(genre);
                    }
                }

                // Crear o encontrar etiquetas con datos completos
                if (request.getTags() != null) {
                    for (EnhancedBulkCreateBookRequest.TagData tagData : request.getTags()) {
                        Tag tag = findOrCreateTagWithFullData(tagData);
                        book.getTags().add(tag);
                    }
                }

                // Crear o encontrar serie por nombre
                if (request.getSeriesName() != null && !request.getSeriesName().trim().isEmpty()) {
                    Series series = findOrCreateSeries(request.getSeriesName().trim());
                    book.setSeries(series);
                    book.setOrderInSeries(request.getOrderInSeries());
                }

                Book savedBook = bookRepository.save(book);
                createdBooks.add(convertToBookResponse(savedBook));
            } catch (Exception e) {
                // Log error but continue with other books
                System.err.println("Error creating book: " + request.getTitle() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("=== RESUMEN CARGA MASIVA ===");
        System.out.println("Libros procesados: " + requests.size());
        System.out.println("Libros creados: " + createdBooks.size());
        System.out.println("Libros saltados (duplicados): " + skippedBooks);
        
        return createdBooks;
    }

    public Optional<BookResponse> getBookById(String id) {
        return bookRepository.findById(id)
                .map(this::convertToBookResponse);
    }

    // Método mejorado para obtener todos los libros con paginación y ordenamiento
    public Map<String, Object> getAllBooksWithPagination(Integer page, Integer size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<Book> bookPage = bookRepository.findAll(pageable);
        
        List<BookResponse> books = bookPage.getContent().stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("currentPage", page);
        result.put("totalPages", bookPage.getTotalPages());
        result.put("totalElements", bookPage.getTotalElements());
        result.put("size", size);
        result.put("hasNext", bookPage.hasNext());
        result.put("hasPrevious", bookPage.hasPrevious());
        
        return result;
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    // Contar total de libros
    public long getTotalBooksCount() {
        return bookRepository.countAllBooks();
    }

    // Listar libros con paginación simple (similar a UserService)
    public List<Book> getAllBooksWithSimplePagination(int offset, int limit) {
        // Validar parámetros
        if (offset < 0) {
            offset = 0;
        }
        if (limit <= 0 || limit > 100) { // Máximo 100 libros por página
            limit = 20; // Por defecto 20
        }
        
        return bookRepository.findAllOrderByCreatedAtDescWithPagination(offset, limit);
    }

    // Método mejorado para búsqueda con paginación
    public Map<String, Object> searchBooksWithPagination(String searchTerm, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.searchBooksWithPagination(searchTerm, pageable);
        
        List<BookResponse> books = bookPage.getContent().stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("currentPage", page);
        result.put("totalPages", bookPage.getTotalPages());
        result.put("totalElements", bookPage.getTotalElements());
        result.put("size", size);
        result.put("hasNext", bookPage.hasNext());
        result.put("hasPrevious", bookPage.hasPrevious());
        result.put("searchTerm", searchTerm);
        
        return result;
    }

    public List<BookResponse> searchBooks(String searchTerm) {
        return bookRepository.searchBooks(searchTerm).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    // Método mejorado para obtener libros por género con paginación
    public Map<String, Object> getBooksByGenreWithPagination(String genreName, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findBooksByGenreWithPagination(genreName, pageable);
        
        List<BookResponse> books = bookPage.getContent().stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("currentPage", page);
        result.put("totalPages", bookPage.getTotalPages());
        result.put("totalElements", bookPage.getTotalElements());
        result.put("size", size);
        result.put("hasNext", bookPage.hasNext());
        result.put("hasPrevious", bookPage.hasPrevious());
        result.put("genreName", genreName);
        
        return result;
    }

    public List<BookResponse> getBooksByGenre(String genreName) {
        return bookRepository.findBooksByGenre(genreName).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    // Método mejorado para obtener libros por autor con paginación
    public Map<String, Object> getBooksByAuthorWithPagination(String authorName, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> bookPage = bookRepository.findBooksByAuthorWithPagination(authorName, pageable);
        
        List<BookResponse> books = bookPage.getContent().stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("books", books);
        result.put("currentPage", page);
        result.put("totalPages", bookPage.getTotalPages());
        result.put("totalElements", bookPage.getTotalElements());
        result.put("size", size);
        result.put("hasNext", bookPage.hasNext());
        result.put("hasPrevious", bookPage.hasPrevious());
        result.put("authorName", authorName);
        
        return result;
    }

    public List<BookResponse> getBooksByAuthor(String authorName) {
        return bookRepository.findBooksByAuthor(authorName).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getTopRatedBooks(Integer limit) {
        return bookRepository.findTopRatedBooks(limit).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getMostReviewedBooks(Integer limit) {
        return bookRepository.findMostReviewedBooks(limit).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    // Método mejorado para obtener libros recientes por límite en lugar de días
    public List<BookResponse> getRecentBooks(Integer limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Book> bookPage = bookRepository.findAll(pageable);
        
        return bookPage.getContent().stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksBySeries(String seriesName) {
        return bookRepository.findBooksBySeries(seriesName).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByTag(String tagName) {
        return bookRepository.findBooksByTag(tagName).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByMinimumRating(Double minRating) {
        return bookRepository.findBooksByMinimumRating(minRating).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByLanguage(String language) {
        return bookRepository.findByLanguage(language).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByAgeRating(String ageRating) {
        return bookRepository.findByAgeRating(ageRating).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getBooksByReadingDifficulty(String readingDifficulty) {
        return bookRepository.findByReadingDifficulty(readingDifficulty).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getSimilarBooks(String userId, Double rating, Integer limit) {
        return bookRepository.findSimilarBooksByUserRatings(userId, rating, limit).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public List<BookResponse> getRecommendationsByGenres(String userId, Double minRating, Integer limit) {
        return bookRepository.findRecommendationsByPreferredGenres(userId, minRating, limit).stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    public BookResponse updateBook(String id, CreateBookRequest request) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setTitle(request.getTitle());
            book.setIsbn(request.getIsbn());
            book.setDescription(request.getDescription());
            book.setPublicationYear(request.getPublicationYear());
            book.setPageCount(request.getPageCount());
            book.setLanguage(request.getLanguage());
            book.setCoverImage(request.getCoverImage());
            book.setAgeRating(request.getAgeRating());
            book.setReadingDifficulty(request.getReadingDifficulty());
            book.setUpdatedAt(LocalDateTime.now());

            // Actualizar autores
            book.getAuthors().clear();
            if (request.getAuthorIds() != null) {
                for (String authorId : request.getAuthorIds()) {
                    authorRepository.findById(authorId).ifPresent(author -> {
                        book.getAuthors().add(author);
                    });
                }
            }

            // Actualizar géneros
            book.getGenres().clear();
            if (request.getGenreIds() != null) {
                for (String genreId : request.getGenreIds()) {
                    genreRepository.findById(genreId).ifPresent(genre -> {
                        book.getGenres().add(genre);
                    });
                }
            }

            // Actualizar etiquetas
            book.getTags().clear();
            if (request.getTagIds() != null) {
                for (String tagId : request.getTagIds()) {
                    tagRepository.findById(tagId).ifPresent(tag -> {
                        book.getTags().add(tag);
                    });
                }
            }

            // Actualizar serie
            if (request.getSeriesId() != null) {
                seriesRepository.findById(request.getSeriesId()).ifPresent(series -> {
                    book.setSeries(series);
                    book.setOrderInSeries(request.getOrderInSeries());
                });
            } else {
                book.setSeries(null);
                book.setOrderInSeries(null);
            }

            Book savedBook = bookRepository.save(book);
            return convertToBookResponse(savedBook);
        }
        return null;
    }

    // Método para actualización parcial de libros (PATCH)
    public BookResponse updateBookPartially(String id, UpdateBookRequest request) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            
            // Actualizar solo los campos que no son null
            if (request.getTitle() != null) {
                book.setTitle(request.getTitle());
            }
            if (request.getIsbn() != null) {
                book.setIsbn(request.getIsbn());
            }
            if (request.getDescription() != null) {
                book.setDescription(request.getDescription());
            }
            if (request.getPublicationYear() != null) {
                book.setPublicationYear(request.getPublicationYear());
            }
            if (request.getPageCount() != null) {
                book.setPageCount(request.getPageCount());
            }
            if (request.getLanguage() != null) {
                book.setLanguage(request.getLanguage());
            }
            if (request.getCoverImage() != null) {
                book.setCoverImage(request.getCoverImage());
            }
            if (request.getAgeRating() != null) {
                book.setAgeRating(request.getAgeRating());
            }
            if (request.getAverageRating() != null) {
                book.setAverageRating(request.getAverageRating());
            }
            if (request.getTotalRatings() != null) {
                book.setTotalRatings(request.getTotalRatings());
            }
            if (request.getReadingDifficulty() != null) {
                book.setReadingDifficulty(request.getReadingDifficulty());
            }

            // Actualizar autores si se proporcionan
            if (request.getAuthorIds() != null) {
                book.getAuthors().clear();
                for (String authorId : request.getAuthorIds()) {
                    authorRepository.findById(authorId).ifPresent(author -> {
                        book.getAuthors().add(author);
                    });
                }
            }

            // Actualizar géneros si se proporcionan
            if (request.getGenreIds() != null) {
                book.getGenres().clear();
                for (String genreId : request.getGenreIds()) {
                    genreRepository.findById(genreId).ifPresent(genre -> {
                        book.getGenres().add(genre);
                    });
                }
            }

            // Actualizar etiquetas si se proporcionan
            if (request.getTagIds() != null) {
                book.getTags().clear();
                for (String tagId : request.getTagIds()) {
                    tagRepository.findById(tagId).ifPresent(tag -> {
                        book.getTags().add(tag);
                    });
                }
            }

            // Actualizar serie si se proporciona
            if (request.getSeriesId() != null) {
                if (request.getSeriesId().isEmpty()) {
                    book.setSeries(null);
                    book.setOrderInSeries(null);
                } else {
                    seriesRepository.findById(request.getSeriesId()).ifPresent(series -> {
                        book.setSeries(series);
                        if (request.getOrderInSeries() != null) {
                            book.setOrderInSeries(request.getOrderInSeries());
                        }
                    });
                }
            }

            book.setUpdatedAt(LocalDateTime.now());
            Book savedBook = bookRepository.save(book);
            return convertToBookResponse(savedBook);
        }
        return null;
    }

    // Método específico para actualizar solo la imagen de portada
    public BookResponse updateBookCoverImage(String id, String newCoverImage) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setCoverImage(newCoverImage);
            book.setUpdatedAt(LocalDateTime.now());
            
            Book savedBook = bookRepository.save(book);
            return convertToBookResponse(savedBook);
        }
        return null;
    }

    public boolean deleteBook(String id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void updateBookRating(String bookId, Double rating, boolean isNewRating) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            
            // Recalculate average rating
            Double currentAvg = book.getAverageRating();
            Integer currentTotal = book.getTotalRatings();
            
            if (currentTotal == null || currentTotal == 0) {
                // Primera calificación del libro
                book.setAverageRating(rating);
                book.setTotalRatings(1);
            } else if (isNewRating) {
                // Nueva calificación - incrementar contador
                Double newAverage = ((currentAvg * currentTotal) + rating) / (currentTotal + 1);
                book.setAverageRating(newAverage);
                book.setTotalRatings(currentTotal + 1);
            } else {
                // Actualización de calificación existente - NO incrementar contador
                // Mantenemos el promedio y contador actuales (se recalculará en el próximo rating nuevo)
                // Para mayor precisión, se podría implementar un recálculo completo aquí
            }
            
            book.setUpdatedAt(LocalDateTime.now());
            bookRepository.save(book);
        }
    }
    
    // Métodos auxiliares para crear entidades si no existen (para carga masiva)
    private Author findOrCreateAuthor(String name) {
        Optional<Author> existingAuthor = authorRepository.findByName(name);
        if (existingAuthor.isPresent()) {
            return existingAuthor.get();
        }
        
        Author newAuthor = new Author();
        newAuthor.setName(name);
        newAuthor.setCreatedAt(LocalDateTime.now());
        newAuthor.setUpdatedAt(LocalDateTime.now());
        return authorRepository.save(newAuthor);
    }

    private Genre findOrCreateGenre(String name) {
        Optional<Genre> existingGenre = genreRepository.findByName(name);
        if (existingGenre.isPresent()) {
            return existingGenre.get();
        }
        
        Genre newGenre = new Genre();
        newGenre.setName(name);
        newGenre.setCreatedAt(LocalDateTime.now());
        newGenre.setUpdatedAt(LocalDateTime.now());
        return genreRepository.save(newGenre);
    }

    private Tag findOrCreateTag(String name) {
        Optional<Tag> existingTag = tagRepository.findByName(name);
        if (existingTag.isPresent()) {
            return existingTag.get();
        }
        
        Tag newTag = new Tag();
        newTag.setName(name);
        newTag.setCreatedAt(LocalDateTime.now());
        newTag.setUpdatedAt(LocalDateTime.now());
        return tagRepository.save(newTag);
    }

    private Series findOrCreateSeries(String name) {
        Optional<Series> existingSeries = seriesRepository.findByName(name);
        if (existingSeries.isPresent()) {
            return existingSeries.get();
        }
        
        Series newSeries = new Series();
        newSeries.setName(name);
        newSeries.setIsCompleted(false);
        newSeries.setCreatedAt(LocalDateTime.now());
        newSeries.setUpdatedAt(LocalDateTime.now());
        return seriesRepository.save(newSeries);
    }

    // Métodos auxiliares para crear entidades con datos completos (para enhanced bulk)
    private Author findOrCreateAuthorWithFullData(EnhancedBulkCreateBookRequest.AuthorData authorData) {
        // Buscar autor existente por nombre
        List<Author> existingAuthors = authorRepository.findByNameContainingIgnoreCase(authorData.getNombre().trim());
        
        for (Author existing : existingAuthors) {
            if (existing.getName().equalsIgnoreCase(authorData.getNombre().trim())) {
                // Actualizar datos si el autor existe pero tiene información incompleta
                boolean needsUpdate = false;
                
                if (existing.getBiography() == null && authorData.getAcerca_de() != null) {
                    existing.setBiography(authorData.getAcerca_de());
                    needsUpdate = true;
                }
                if (existing.getBirthDate() == null && authorData.getFechaNacimiento() != null) {
                    existing.setBirthDate(authorData.getFechaNacimiento());
                    needsUpdate = true;
                }
                if (existing.getDeathDate() == null && authorData.getFechaMuerte() != null) {
                    existing.setDeathDate(authorData.getFechaMuerte());
                    needsUpdate = true;
                }
                if (existing.getNationality() == null && authorData.getNacionalidad() != null) {
                    existing.setNationality(authorData.getNacionalidad());
                    needsUpdate = true;
                }
                if (existing.getPhoto() == null && authorData.getFoto() != null) {
                    existing.setPhoto(authorData.getFoto());
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    existing.setUpdatedAt(LocalDateTime.now());
                    return authorRepository.save(existing);
                }
                
                return existing;
            }
        }
        
        // Crear nuevo autor con datos completos
        Author newAuthor = new Author();
        newAuthor.setName(authorData.getNombre().trim());
        newAuthor.setBiography(authorData.getAcerca_de());
        newAuthor.setBirthDate(authorData.getFechaNacimiento());
        newAuthor.setDeathDate(authorData.getFechaMuerte());
        newAuthor.setNationality(authorData.getNacionalidad());
        newAuthor.setPhoto(authorData.getFoto());
        newAuthor.setTotalBooks(0);
        newAuthor.setCreatedAt(LocalDateTime.now());
        newAuthor.setUpdatedAt(LocalDateTime.now());
        
        return authorRepository.save(newAuthor);
    }

    private Genre findOrCreateGenreWithFullData(EnhancedBulkCreateBookRequest.GenreData genreData) {
        // Buscar género existente por nombre
        List<Genre> existingGenres = genreRepository.findByNameContainingIgnoreCase(genreData.getNombre().trim());
        
        for (Genre existing : existingGenres) {
            if (existing.getName().equalsIgnoreCase(genreData.getNombre().trim())) {
                // Actualizar datos si el género existe pero tiene información incompleta
                boolean needsUpdate = false;
                
                if (existing.getDescription() == null && genreData.getDescripcion() != null) {
                    existing.setDescription(genreData.getDescripcion());
                    needsUpdate = true;
                }
                if (existing.getParentGenre() == null && genreData.getGenero_padre() != null) {
                    existing.setParentGenre(genreData.getGenero_padre());
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    existing.setUpdatedAt(LocalDateTime.now());
                    return genreRepository.save(existing);
                }
                
                return existing;
            }
        }
        
        // Crear nuevo género con datos completos
        Genre newGenre = new Genre();
        newGenre.setName(genreData.getNombre().trim());
        newGenre.setDescription(genreData.getDescripcion());
        newGenre.setParentGenre(genreData.getGenero_padre());
        newGenre.setCreatedAt(LocalDateTime.now());
        newGenre.setUpdatedAt(LocalDateTime.now());
        
        return genreRepository.save(newGenre);
    }

    private Tag findOrCreateTagWithFullData(EnhancedBulkCreateBookRequest.TagData tagData) {
        // Buscar tag existente por nombre
        List<Tag> existingTags = tagRepository.findByNameContainingIgnoreCase(tagData.getNombre().trim());
        
        for (Tag existing : existingTags) {
            if (existing.getName().equalsIgnoreCase(tagData.getNombre().trim())) {
                // Actualizar datos si el tag existe pero tiene información incompleta
                boolean needsUpdate = false;
                
                if (existing.getCategory() == null && tagData.getCategoria() != null) {
                    existing.setCategory(tagData.getCategoria());
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    existing.setUpdatedAt(LocalDateTime.now());
                    return tagRepository.save(existing);
                }
                
                return existing;
            }
        }
        
        // Crear nuevo tag con datos completos
        Tag newTag = new Tag();
        newTag.setName(tagData.getNombre().trim());
        newTag.setCategory(tagData.getCategoria());
        newTag.setCreatedAt(LocalDateTime.now());
        newTag.setUpdatedAt(LocalDateTime.now());
        
        return tagRepository.save(newTag);
    }

    public BookResponse convertToBookResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setIsbn(book.getIsbn());
        response.setDescription(book.getDescription());
        response.setPublicationYear(book.getPublicationYear());
        response.setPageCount(book.getPageCount());
        response.setLanguage(book.getLanguage());
        response.setCoverImage(book.getCoverImage());
        response.setAgeRating(book.getAgeRating());
        response.setAverageRating(book.getAverageRating());
        response.setTotalRatings(book.getTotalRatings());
        response.setReadingDifficulty(book.getReadingDifficulty());
        response.setCreatedAt(book.getCreatedAt());

        // Convert authors
        if (book.getAuthors() != null) {
            response.setAuthors(book.getAuthors().stream()
                    .map(this::convertToAuthorResponse)
                    .collect(Collectors.toList()));
        }

        // Convert genres
        if (book.getGenres() != null) {
            response.setGenres(book.getGenres().stream()
                    .map(this::convertToGenreResponse)
                    .collect(Collectors.toList()));
        }

        // Convert tags
        if (book.getTags() != null) {
            response.setTags(book.getTags().stream()
                    .map(this::convertToTagResponse)
                    .collect(Collectors.toList()));
        }

        // Convert series
        if (book.getSeries() != null) {
            response.setSeries(convertToSeriesResponse(book.getSeries()));
        }

        // Set order in series
        response.setOrderInSeries(book.getOrderInSeries());

        // Get total chapters
        response.setTotalChapters(chapterRepository.countChaptersByBookId(book.getId()));

        return response;
    }

    private AuthorResponse convertToAuthorResponse(Author author) {
        // Calcular dinámicamente la cantidad de libros
        int actualBookCount = 0;
        
        // Método 1: Intentar usar las relaciones cargadas
        if (author.getBooks() != null && !author.getBooks().isEmpty()) {
            actualBookCount = author.getBooks().size();
        } else {
            // Método 2: Consulta directa a la base de datos como respaldo
            actualBookCount = (int) authorRepository.countBooksByAuthorId(author.getId());
        }
        
        return new AuthorResponse(author.getId(), author.getName(), author.getBiography(),
                author.getBirthDate(), author.getDeathDate(), author.getNationality(),
                author.getPhoto(), actualBookCount);
    }

    private GenreResponse convertToGenreResponse(Genre genre) {
        return new GenreResponse(genre.getId(), genre.getName(), 
                genre.getDescription(), genre.getParentGenre());
    }

    private TagResponse convertToTagResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getCategory());
    }

    private SeriesResponse convertToSeriesResponse(Series series) {
        return new SeriesResponse(series.getId(), series.getName(), 
                series.getDescription(), series.getTotalBooks(), series.getIsCompleted());
    }

    // Métodos para manejar integridad referencial y limpiar referencias huérfanas
    
    /**
     * Limpia todas las referencias huérfanas de un libro específico
     */
    @Transactional
    public BookResponse cleanOrphanedReferences(String bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            throw new RuntimeException("Libro no encontrado");
        }

        Book book = bookOpt.get();
        boolean wasModified = false;

        // Limpiar autores huérfanos
        Set<Author> validAuthors = new HashSet<>();
        for (Author author : book.getAuthors()) {
            if (authorRepository.existsById(author.getId())) {
                validAuthors.add(author);
            } else {
                wasModified = true;
            }
        }
        book.setAuthors(validAuthors);

        // Limpiar géneros huérfanos
        Set<Genre> validGenres = new HashSet<>();
        for (Genre genre : book.getGenres()) {
            if (genreRepository.existsById(genre.getId())) {
                validGenres.add(genre);
            } else {
                wasModified = true;
            }
        }
        book.setGenres(validGenres);

        // Limpiar tags huérfanos
        Set<Tag> validTags = new HashSet<>();
        for (Tag tag : book.getTags()) {
            if (tagRepository.existsById(tag.getId())) {
                validTags.add(tag);
            } else {
                wasModified = true;
            }
        }
        book.setTags(validTags);

        // Limpiar serie huérfana
        if (book.getSeries() != null && !seriesRepository.existsById(book.getSeries().getId())) {
            book.setSeries(null);
            book.setOrderInSeries(null);
            wasModified = true;
        }

        if (wasModified) {
            book.setUpdatedAt(LocalDateTime.now());
            book = bookRepository.save(book);
        }

        return convertToBookResponse(book);
    }

    /**
     * Limpia referencias huérfanas de todos los libros en la base de datos
     */
    @Transactional
    public int cleanAllOrphanedReferences() {
        List<Book> allBooks = bookRepository.findAll();
        int cleanedBooks = 0;

        for (Book book : allBooks) {
            try {
                cleanOrphanedReferences(book.getId());
                cleanedBooks++;
            } catch (Exception e) {
                // Log error but continue with other books
                System.err.println("Error cleaning orphaned references for book " + book.getId() + ": " + e.getMessage());
            }
        }

        return cleanedBooks;
    }

    /**
     * Valida que todas las referencias de un libro existan antes de crear/actualizar
     */
    public void validateBookReferences(CreateBookRequest request) {
        // Validar autores
        if (request.getAuthorIds() != null) {
            for (String authorId : request.getAuthorIds()) {
                if (!authorRepository.existsById(authorId)) {
                    throw new RuntimeException("Autor con ID " + authorId + " no existe");
                }
            }
        }

        // Validar géneros
        if (request.getGenreIds() != null) {
            for (String genreId : request.getGenreIds()) {
                if (!genreRepository.existsById(genreId)) {
                    throw new RuntimeException("Género con ID " + genreId + " no existe");
                }
            }
        }

        // Validar tags
        if (request.getTagIds() != null) {
            for (String tagId : request.getTagIds()) {
                if (!tagRepository.existsById(tagId)) {
                    throw new RuntimeException("Tag con ID " + tagId + " no existe");
                }
            }
        }

        // Validar serie
        if (request.getSeriesId() != null && !seriesRepository.existsById(request.getSeriesId())) {
            throw new RuntimeException("Serie con ID " + request.getSeriesId() + " no existe");
        }
    }

    /**
     * Versión segura de createBook que valida referencias antes de crear
     */
    public BookResponse createBookWithValidation(CreateBookRequest request) {
        validateBookReferences(request);
        return createBook(request);
    }

    /**
     * Versión segura de updateBook que valida referencias antes de actualizar
     */
    public BookResponse updateBookWithValidation(String id, CreateBookRequest request) {
        validateBookReferences(request);
        return updateBook(id, request);
    }

    // Método que garantiza la carga de relaciones usando Pageable
    public List<BookResponse> getBooksWithRelationsUsingPageable(int offset, int limit) {
        // Validar parámetros
        if (offset < 0) {
            offset = 0;
        }
        if (limit <= 0 || limit > 100) {
            limit = 20;
        }
        
        // Convertir offset a página
        int page = offset / limit;
        
        // Crear Pageable con ordenamiento
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // Usar findAll que SÍ carga las relaciones automáticamente
        Page<Book> bookPage = bookRepository.findAll(pageable);
        
        // Convertir a BookResponse
        return bookPage.getContent().stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    // Método de depuración para investigar el problema de relaciones
    public List<BookResponse> debugBooksWithRelations(int offset, int limit) {
        System.out.println("=== DEBUG: Iniciando debugBooksWithRelations ===");
        
        // Validar y limitar los parámetros
        if (limit > 100) {
            limit = 100; // Máximo 100 registros por consulta
            System.out.println("Límite ajustado a: " + limit);
        }
        if (limit <= 0) {
            limit = 10; // Límite por defecto
        }
        if (offset < 0) {
            offset = 0; // Offset por defecto
        }
        
        Optional<Book> bookOpt = bookRepository.findById("12345");
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            System.out.println("Libro encontrado: " + book.getTitle());
            
            // Intentar cargar el libro por ID para comparar
            Optional<Book> bookById = bookRepository.findById(book.getId());
            if (bookById.isPresent()) {
                Book bookWithRelations = bookById.get();
                System.out.println("Mismo libro por ID - Autores size: " + 
                    (bookWithRelations.getAuthors() != null ? bookWithRelations.getAuthors().size() : "null"));
                System.out.println("Mismo libro por ID - Géneros size: " + 
                    (bookWithRelations.getGenres() != null ? bookWithRelations.getGenres().size() : "null"));
                System.out.println("Mismo libro por ID - Tags size: " + 
                    (bookWithRelations.getTags() != null ? bookWithRelations.getTags().size() : "null"));
                
                // Usar el libro cargado por ID que SÍ tiene relaciones
                return Arrays.asList(convertToBookResponse(bookWithRelations));
            } else {
                return Arrays.asList(convertToBookResponse(book));
            }
        }
        
        return new ArrayList<>();
    }

    // Método para obtener todos los libros sin paginación con relaciones completas
    public List<BookResponse> getAllBooksWithoutPagination() {
        System.out.println("=== Obteniendo todos los libros sin paginación ===");
        
        // Obtener todos los libros usando findAll (que carga relaciones automáticamente)
        List<Book> allBooks = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<BookResponse> responses = new ArrayList<>();
        
        for (Book book : allBooks) {
            System.out.println("Procesando libro: " + book.getTitle());
            
            // Para garantizar que las relaciones estén cargadas, recargar por ID si es necesario
            Optional<Book> bookById = bookRepository.findById(book.getId());
            if (bookById.isPresent()) {
                Book bookWithRelations = bookById.get();
                responses.add(convertToBookResponse(bookWithRelations));
            } else {
                responses.add(convertToBookResponse(book));
            }
        }
        
        System.out.println("=== Total de libros obtenidos: " + responses.size() + " ===");
        return responses;
    }

    public List<Map<String, Object>> getBookRatings(String bookId, int offset, int limit) {
        try {
            System.out.println("=== DEPURACIÓN GET BOOK RATINGS ===");
            System.out.println("BookId: " + bookId);
            System.out.println("Offset: " + offset + ", Limit: " + limit);
            
            // Validar y limitar los parámetros
            if (limit > 100) {
                limit = 100; // Máximo 100 registros por consulta
                System.out.println("Límite ajustado a: " + limit);
            }
            if (limit <= 0) {
                limit = 10; // Límite por defecto
            }
            if (offset < 0) {
                offset = 0; // Offset por defecto
            }
            
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                System.out.println("Libro encontrado: " + book.getTitle());
                
                // Usar Neo4jClient con paginación directa usando offset y limit
                String cypher = "MATCH (u:User)-[r:RATED]->(b:Book) " +
                               "WHERE b.id = $bookId " +
                               "RETURN u.id AS userId, u.username AS username, r.rating AS rating, r.review AS review " +
                               "ORDER BY r.createdAt DESC " +
                               "SKIP $offset LIMIT $limit";
                
                List<Map<String, Object>> ratings = neo4jClient
                    .query(cypher)
                    .bind(bookId).to("bookId")
                    .bind(offset).to("offset")
                    .bind(limit).to("limit")
                    .fetch()
                    .all()
                    .stream()
                    .map(record -> {
                        Map<String, Object> rating = new HashMap<>();
                        rating.put("userId", record.get("userId"));
                        rating.put("username", record.get("username"));
                        rating.put("rating", record.get("rating"));
                        rating.put("review", record.get("review"));
                        rating.put("reviewTitle", null);        // No disponible en BD
                        rating.put("helpfulVotes", 0);          // Default
                        rating.put("createdAt", null);          // No disponible en BD
                        rating.put("updatedAt", null);          // No disponible en BD
                        return rating;
                    })
                    .collect(Collectors.toList());
                
                System.out.println("Calificaciones obtenidas con paginación: " + ratings.size());
                
                // Mostrar detalles de cada calificación
                for (Map<String, Object> rating : ratings) {
                    System.out.println("Calificación: " + rating.get("username") + 
                                     " - Rating: " + rating.get("rating") + 
                                     " - Review: " + rating.get("review"));
                }
                
                System.out.println("Devolviendo " + ratings.size() + " calificaciones (offset " + offset + ", limit " + limit + ")");
                return ratings;
            } else {
                System.out.println("Libro no encontrado con ID: " + bookId);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.out.println("ERROR en getBookRatings: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener calificaciones del libro: " + e.getMessage());
        }
    }

    /**
     * Obtiene libros para "continuar leyendo" - libros calificados por el usuario ordenados por puntuación
     */
    public List<BookResponse> getContinueReadingForUser(String userId) {
        try {
            System.out.println("=== OBTENIENDO CONTINUAR LEYENDO PARA USUARIO: " + userId + " ===");
            
            // Consulta para obtener libros calificados por el usuario
            String cypher = "MATCH (u:User)-[r:RATED]->(b:Book) " +
                           "WHERE u.id = $userId " +
                           "RETURN b, r.rating AS userRating " +
                           "ORDER BY r.rating DESC, b.average_rating DESC " +
                           "LIMIT 10";
            
            List<Map<String, Object>> results = neo4jClient
                .query(cypher)
                .bind(userId).to("userId")
                .fetch()
                .all()
                .stream()
                .collect(Collectors.toList());
            
            System.out.println("Libros para continuar leyendo encontrados: " + results.size());
            
            return results.stream()
                .map(record -> {
                    Book book = (Book) record.get("b");
                    return convertToBookResponse(book);
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.out.println("ERROR en getContinueReadingForUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener libros para continuar leyendo: " + e.getMessage());
        }
    }

    // ENDPOINTS PARA RECOMENDACIONES Y DESTACADOS

    /**
     * Obtiene los 10 libros destacados con mayor promedio de calificaciones
     */
    public List<BookResponse> getFeaturedBooks() {
        try {
            System.out.println("=== OBTENIENDO LIBROS DESTACADOS ===");
            
            String cypher = "MATCH (b:Book) " +
                           "WHERE b.average_rating IS NOT NULL AND b.average_rating > 0 " +
                           "RETURN b.id as id, b.title as title, b.isbn as isbn, " +
                           "       b.description as description, b.publication_year as publicationYear, " +
                           "       b.page_count as pageCount, b.language as language, " +
                           "       b.cover_image as coverImage, b.age_rating as ageRating, " +
                           "       b.average_rating as averageRating, b.total_ratings as totalRatings, " +
                           "       b.reading_difficulty as readingDifficulty " +
                           "ORDER BY b.average_rating DESC, b.total_ratings DESC " +
                           "LIMIT 10";
            
            List<Map<String, Object>> results = neo4jClient
                .query(cypher)
                .fetch()
                .all()
                .stream()
                .collect(Collectors.toList());
            
            System.out.println("Libros destacados encontrados: " + results.size());
            
            List<BookResponse> featuredBooks = new ArrayList<>();
            for (Map<String, Object> result : results) {
                Book book = new Book();
                book.setId((String) result.get("id"));
                book.setTitle((String) result.get("title"));
                book.setIsbn((String) result.get("isbn"));
                book.setDescription((String) result.get("description"));
                
                // Convertir Long a Integer para campos numéricos
                Object publicationYear = result.get("publicationYear");
                book.setPublicationYear(publicationYear != null ? ((Long) publicationYear).intValue() : null);
                
                Object pageCount = result.get("pageCount");
                book.setPageCount(pageCount != null ? ((Long) pageCount).intValue() : null);
                
                book.setLanguage((String) result.get("language"));
                book.setCoverImage((String) result.get("coverImage"));
                book.setAgeRating((String) result.get("ageRating"));
                book.setAverageRating((Double) result.get("averageRating"));
                
                Object totalRatings = result.get("totalRatings");
                book.setTotalRatings(totalRatings != null ? ((Long) totalRatings).intValue() : null);
                
                book.setReadingDifficulty((String) result.get("readingDifficulty"));
                
                featuredBooks.add(convertToBookResponse(book));
            }
            
            return featuredBooks;
                
        } catch (Exception e) {
            System.out.println("ERROR en getFeaturedBooks: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener libros destacados: " + e.getMessage());
        }
    }

    /**
     * Obtiene recomendaciones basadas en los ratings más altos del usuario autenticado
     */
    public List<BookResponse> getRecommendationsForUser(String userId) {
        try {
            System.out.println("=== OBTENIENDO RECOMENDACIONES PARA USUARIO: " + userId + " ===");
            
            // Obtener géneros y tags de libros mejor calificados por el usuario (rating >= 4.0)
            String cypher = "MATCH (u:User)-[r:RATED]->(b:Book) " +
                           "WHERE u.id = $userId AND r.rating >= 4.0 " +
                           "MATCH (b)-[:BELONGS_TO_GENRE]->(g:Genre) " +
                           "MATCH (b)-[:HAS_TAG]->(t:Tag) " +
                           "WITH COLLECT(DISTINCT g.id) AS likedGenres, COLLECT(DISTINCT t.id) AS likedTags " +
                           "MATCH (rb:Book)-[:BELONGS_TO_GENRE]->(rg:Genre) " +
                           "WHERE rg.id IN likedGenres AND rb.average_rating IS NOT NULL " +
                           "AND NOT EXISTS((u:User)-[:RATED]->(rb) WHERE u.id = $userId) " +
                           "RETURN DISTINCT rb.id as id, rb.title as title, rb.isbn as isbn, " +
                           "       rb.description as description, rb.publication_year as publicationYear, " +
                           "       rb.page_count as pageCount, rb.language as language, " +
                           "       rb.cover_image as coverImage, rb.age_rating as ageRating, " +
                           "       rb.average_rating as averageRating, rb.total_ratings as totalRatings, " +
                           "       rb.reading_difficulty as readingDifficulty " +
                           "ORDER BY rb.average_rating DESC " +
                           "LIMIT 10";
            
            List<Map<String, Object>> results = neo4jClient
                .query(cypher)
                .bind(userId).to("userId")
                .fetch()
                .all()
                .stream()
                .collect(Collectors.toList());
            
            System.out.println("Libros para recomendaciones encontrados: " + results.size());
            
            List<BookResponse> recommendedBooks = new ArrayList<>();
            for (Map<String, Object> result : results) {
                Book book = new Book();
                book.setId((String) result.get("id"));
                book.setTitle((String) result.get("title"));
                book.setIsbn((String) result.get("isbn"));
                book.setDescription((String) result.get("description"));
                
                // Convertir Long a Integer para campos numéricos
                Object publicationYear = result.get("publicationYear");
                book.setPublicationYear(publicationYear != null ? ((Long) publicationYear).intValue() : null);
                
                Object pageCount = result.get("pageCount");
                book.setPageCount(pageCount != null ? ((Long) pageCount).intValue() : null);
                
                book.setLanguage((String) result.get("language"));
                book.setCoverImage((String) result.get("coverImage"));
                book.setAgeRating((String) result.get("ageRating"));
                book.setAverageRating((Double) result.get("averageRating"));
                
                Object totalRatings = result.get("totalRatings");
                book.setTotalRatings(totalRatings != null ? ((Long) totalRatings).intValue() : null);
                
                book.setReadingDifficulty((String) result.get("readingDifficulty"));
                
                recommendedBooks.add(convertToBookResponse(book));
            }
            
            // Si no hay suficientes recomendaciones, completar con libros mejor calificados
            if (recommendedBooks.size() < 10) {
                List<Book> topBooks = getTopRatedBooksNotRatedByUser(userId, 10 - recommendedBooks.size());
                for (Book book : topBooks) {
                    recommendedBooks.add(convertToBookResponse(book));
                }
            }
            
            return recommendedBooks;
                
        } catch (Exception e) {
            System.out.println("ERROR en getRecommendationsForUser: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener recomendaciones: " + e.getMessage());
        }
    }

    /**
     * Obtiene top 10 libros con mayor promedio de calificaciones
     */
    public List<BookResponse> getTop10Books() {
        try {
            System.out.println("=== OBTENIENDO TOP 10 LIBROS ===");
            
            // Primero obtener libros con calificaciones
            String cypher = "MATCH (b:Book) " +
                           "WHERE b.average_rating IS NOT NULL " +
                           "RETURN b " +
                           "ORDER BY b.average_rating DESC, b.total_ratings DESC " +
                           "LIMIT 10";
            
            List<Book> topBooks = neo4jClient
                .query(cypher)
                .fetch()
                .all()
                .stream()
                .map(record -> (Book) record.get("b"))
                .collect(Collectors.toList());
            
            System.out.println("Libros con calificaciones encontrados: " + topBooks.size());
            
            // Si faltan libros para completar 10, agregar libros relacionados
            if (topBooks.size() < 10) {
                int needed = 10 - topBooks.size();
                List<String> existingIds = topBooks.stream().map(Book::getId).collect(Collectors.toList());
                
                // Obtener géneros de los libros ya seleccionados
                if (!topBooks.isEmpty()) {
                    String relatedCypher = "MATCH (tb:Book)-[:BELONGS_TO_GENRE]->(g:Genre)<-[:BELONGS_TO_GENRE]-(rb:Book) " +
                                         "WHERE tb.id IN $existingIds AND NOT rb.id IN $existingIds " +
                                         "RETURN DISTINCT rb " +
                                         "ORDER BY rb.publicationYear DESC " +
                                         "LIMIT $needed";
                    
                    List<Book> relatedBooks = neo4jClient
                        .query(relatedCypher)
                        .bind(existingIds).to("existingIds")
                        .bind(needed).to("needed")
                        .fetch()
                        .all()
                        .stream()
                        .map(record -> (Book) record.get("rb"))
                        .collect(Collectors.toList());
                    
                    topBooks.addAll(relatedBooks);
                    System.out.println("Libros relacionados agregados: " + relatedBooks.size());
                }
                
                // Si aún faltan, completar con libros más recientes
                if (topBooks.size() < 10) {
                    int stillNeeded = 10 - topBooks.size();
                    List<String> allExistingIds = topBooks.stream().map(Book::getId).collect(Collectors.toList());
                    
                    String recentCypher = "MATCH (b:Book) " +
                                        "WHERE NOT b.id IN $allExistingIds " +
                                        "RETURN b " +
                                        "ORDER BY b.publicationYear DESC " +
                                        "LIMIT $stillNeeded";
                    
                    List<Book> recentBooks = neo4jClient
                        .query(recentCypher)
                        .bind(allExistingIds).to("allExistingIds")
                        .bind(stillNeeded).to("stillNeeded")
                        .fetch()
                        .all()
                        .stream()
                        .map(record -> (Book) record.get("b"))
                        .collect(Collectors.toList());
                    
                    topBooks.addAll(recentBooks);
                    System.out.println("Libros recientes agregados: " + recentBooks.size());
                }
            }
            
            System.out.println("Total de libros en top 10: " + topBooks.size());
            
            return topBooks.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.out.println("ERROR en getTop10Books: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener top 10 libros: " + e.getMessage());
        }
    }

    /**
     * Método auxiliar para obtener libros mejor calificados que no han sido calificados por el usuario
     */
    private List<Book> getTopRatedBooksNotRatedByUser(String userId, int limit) {
        String cypher = "MATCH (b:Book) " +
                       "WHERE b.average_rating IS NOT NULL " +
                       "AND NOT EXISTS((u:User)-[:RATED]->(b) WHERE u.id = $userId) " +
                       "RETURN b " +
                       "ORDER BY b.average_rating DESC " +
                       "LIMIT $limit";
        
        return neo4jClient
            .query(cypher)
            .bind(userId).to("userId")
            .bind(limit).to("limit")
            .fetch()
            .all()
            .stream()
            .map(record -> (Book) record.get("b"))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene libros con paginación usando offset y limit
     */
    public List<BookResponse> getAllBooksWithPagination(int offset, int limit) {
        try {
            System.out.println("=== OBTENIENDO LIBROS CON PAGINACIÓN ===");
            System.out.println("Offset: " + offset + ", Limit: " + limit);
            
            // Validar parámetros
            if (limit > 100) {
                limit = 100; // Máximo 100 registros por consulta
                System.out.println("Límite ajustado a: " + limit);
            }
            if (limit <= 0) {
                limit = 10; // Límite por defecto
            }
            if (offset < 0) {
                offset = 0; // Offset por defecto
            }
            
            // Usar Neo4jClient para obtener IDs de libros con paginación
            String cypher = "MATCH (b:Book) " +
                           "RETURN b.id AS bookId " +
                           "ORDER BY b.title ASC " +
                           "SKIP $offset LIMIT $limit";
            
            List<String> bookIds = neo4jClient
                .query(cypher)
                .bind(offset).to("offset")
                .bind(limit).to("limit")
                .fetch()
                .all()
                .stream()
                .map(record -> (String) record.get("bookId"))
                .collect(Collectors.toList());
            
            System.out.println("IDs de libros encontrados: " + bookIds.size());
            
            // Convertir a BookResponse cargando cada libro por ID
            List<BookResponse> bookResponses = new ArrayList<>();
            for (String bookId : bookIds) {
                try {
                    // Cargar el libro por ID para obtener las relaciones
                    Optional<Book> bookOpt = bookRepository.findById(bookId);
                    if (bookOpt.isPresent()) {
                        Book book = bookOpt.get();
                        BookResponse response = convertToBookResponse(book);
                        bookResponses.add(response);
                        System.out.println("Libro convertido: " + response.getTitle());
                    } else {
                        System.out.println("Libro no encontrado con ID: " + bookId);
                    }
                } catch (Exception e) {
                    System.out.println("Error al convertir libro " + bookId + ": " + e.getMessage());
                    // Continuar con el siguiente libro
                }
            }
            
            System.out.println("Total de libros convertidos: " + bookResponses.size());
            return bookResponses;
            
        } catch (Exception e) {
            System.out.println("ERROR en getAllBooksWithPagination: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al obtener libros paginados: " + e.getMessage());
        }
    }

    // Método para recalcular y actualizar los promedios de calificación de todos los libros
    public void recalculateAllBookRatings() {
        try {
            System.out.println("=== RECALCULANDO PROMEDIOS DE CALIFICACIÓN ===");
            
            // Consulta para obtener todos los libros con sus calificaciones
            String cypher = "MATCH (b:Book) " +
                           "OPTIONAL MATCH (u:User)-[r:RATED]->(b) " +
                           "WITH b, collect(r.rating) as ratings " +
                           "WHERE size(ratings) > 0 " +
                           "WITH b, ratings, " +
                           "     reduce(sum = 0.0, rating IN ratings | sum + rating) as totalSum, " +
                           "     size(ratings) as totalCount " +
                           "SET b.average_rating = totalSum / totalCount, " +
                           "    b.total_ratings = totalCount " +
                           "RETURN b.id as bookId, b.average_rating as avgRating, b.total_ratings as totalRatings";
            
            List<Map<String, Object>> results = neo4jClient
                .query(cypher)
                .fetch()
                .all()
                .stream()
                .collect(Collectors.toList());
            
            System.out.println("Libros actualizados con nuevos promedios: " + results.size());
            
            for (Map<String, Object> result : results) {
                System.out.println("Libro ID: " + result.get("bookId") + 
                                 " - Promedio: " + result.get("avgRating") + 
                                 " - Total calificaciones: " + result.get("totalRatings"));
            }
            
        } catch (Exception e) {
            System.out.println("ERROR en recalculateAllBookRatings: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al recalcular promedios de calificación: " + e.getMessage());
        }
    }

    // Método para verificar si hay calificaciones en la base de datos
    public Map<String, Object> checkRatingsStatus() {
        try {
            System.out.println("=== VERIFICANDO ESTADO DE CALIFICACIONES ===");
            
            // Consulta para contar libros con calificaciones
            String cypherCount = "MATCH (u:User)-[r:RATED]->(b:Book) " +
                                "RETURN count(DISTINCT b) as booksWithRatings, " +
                                "       count(r) as totalRatings, " +
                                "       avg(r.rating) as overallAverage";
            
            Map<String, Object> countResult = neo4jClient
                .query(cypherCount)
                .fetch()
                .one()
                .orElse(Map.of());
            
            // Consulta para ver algunos ejemplos de calificaciones
            String cypherSample = "MATCH (u:User)-[r:RATED]->(b:Book) " +
                                 "RETURN b.id as bookId, b.title as bookTitle, " +
                                 "       collect(r.rating) as ratings " +
                                 "LIMIT 5";
            
            List<Map<String, Object>> sampleResults = neo4jClient
                .query(cypherSample)
                .fetch()
                .all()
                .stream()
                .collect(Collectors.toList());
            
            System.out.println("Libros con calificaciones: " + countResult.get("booksWithRatings"));
            System.out.println("Total de calificaciones: " + countResult.get("totalRatings"));
            System.out.println("Promedio general: " + countResult.get("overallAverage"));
            
            for (Map<String, Object> sample : sampleResults) {
                System.out.println("Libro: " + sample.get("bookTitle") + 
                                 " - Calificaciones: " + sample.get("ratings"));
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("statistics", countResult);
            result.put("samples", sampleResults);
            
            return result;
            
        } catch (Exception e) {
            System.out.println("ERROR en checkRatingsStatus: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al verificar estado de calificaciones: " + e.getMessage());
        }
    }

    // Método para limpiar completamente la base de datos Neo4j
    public void cleanDatabase() {
        try {
            System.out.println("=== LIMPIANDO BASE DE DATOS NEO4J ===");
            
            // Primero eliminar todas las relaciones
            String deleteRelationships = "MATCH ()-[r]-() DELETE r";
            neo4jClient.query(deleteRelationships).run();
            System.out.println("✓ Todas las relaciones eliminadas");
            
            // Luego eliminar todos los nodos
            String deleteNodes = "MATCH (n) DELETE n";
            neo4jClient.query(deleteNodes).run();
            System.out.println("✓ Todos los nodos eliminados");
            
            // Verificar que la base de datos esté vacía
            String countNodes = "MATCH (n) RETURN count(n) as nodeCount";
            Map<String, Object> result = neo4jClient
                .query(countNodes)
                .fetch()
                .one()
                .orElse(Map.of("nodeCount", 0L));
            
            String countRelationships = "MATCH ()-[r]-() RETURN count(r) as relCount";
            Map<String, Object> relResult = neo4jClient
                .query(countRelationships)
                .fetch()
                .one()
                .orElse(Map.of("relCount", 0L));
            
            System.out.println("✓ Base de datos limpiada completamente");
            System.out.println("  - Nodos restantes: " + result.get("nodeCount"));
            System.out.println("  - Relaciones restantes: " + relResult.get("relCount"));
            
        } catch (Exception e) {
            System.out.println("ERROR en cleanDatabase: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al limpiar la base de datos: " + e.getMessage());
        }
    }
}