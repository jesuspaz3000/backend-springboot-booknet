package com.booknet.backend.service;

import com.booknet.backend.dto.RateBookRequest;
import com.booknet.backend.dto.ReadingProgressRequest;
import com.booknet.backend.model.Book;
import com.booknet.backend.model.User;
import com.booknet.backend.model.relationship.RatedRelationship;
import com.booknet.backend.model.relationship.ReadRelationship;
import com.booknet.backend.repository.BookRepository;
import com.booknet.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserInteractionService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    public UserInteractionService(UserRepository userRepository, 
                                 BookRepository bookRepository, 
                                 BookService bookService) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    public boolean rateBook(String userId, RateBookRequest request) {
        try {
            System.out.println("=== DEPURACIÓN RATE BOOK ===");
            System.out.println("UserId: " + userId);
            System.out.println("BookId: " + request.getBookId());
            System.out.println("Rating: " + request.getRating());
            System.out.println("Review: " + request.getReview());
            System.out.println("ReviewTitle: " + request.getReviewTitle());
            
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Book> bookOpt = bookRepository.findById(request.getBookId());

            if (userOpt.isPresent() && bookOpt.isPresent()) {
                User user = userOpt.get();
                Book book = bookOpt.get();
                
                System.out.println("Usuario encontrado: " + user.getUsername());
                System.out.println("Libro encontrado: " + book.getTitle());

                // Validar rating
                if (request.getRating() == null || request.getRating() < 1.0 || request.getRating() > 5.0) {
                    throw new RuntimeException("La calificación debe estar entre 1.0 y 5.0");
                }

                // Check if user already rated this book
                RatedRelationship existingRating = user.getRatedBooks().stream()
                        .filter(r -> r.getBook().getId().equals(book.getId()))
                        .findFirst()
                        .orElse(null);

                boolean isNewRating = (existingRating == null);
                System.out.println("Es nueva calificación: " + isNewRating);

                if (existingRating != null) {
                    // Update existing rating
                    System.out.println("Actualizando calificación existente");
                    existingRating.setRating(request.getRating());
                    existingRating.setReview(request.getReview());
                    existingRating.setReviewTitle(request.getReviewTitle());
                    existingRating.setUpdatedAt(LocalDateTime.now());
                } else {
                    // Create new rating
                    System.out.println("Creando nueva calificación");
                    RatedRelationship ratedRelationship = new RatedRelationship(
                            request.getRating(), request.getReview(), request.getReviewTitle());
                    ratedRelationship.setBook(book);
                    user.getRatedBooks().add(ratedRelationship);
                    System.out.println("Relación creada y agregada al usuario");
                }

                System.out.println("Guardando usuario...");
                userRepository.save(user);
                System.out.println("Usuario guardado exitosamente");
                
                // Update book's average rating with correct flag
                bookService.updateBookRating(book.getId(), request.getRating(), isNewRating);
                
                return true;
            } else {
                System.out.println("Usuario o libro no encontrado");
                System.out.println("Usuario presente: " + userOpt.isPresent());
                System.out.println("Libro presente: " + bookOpt.isPresent());
            }
            return false;
        } catch (Exception e) {
            System.out.println("ERROR en rateBook: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al calificar libro: " + e.getMessage());
        }
    }

    public boolean updateReadingProgress(String userId, ReadingProgressRequest request) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Book> bookOpt = bookRepository.findById(request.getBookId());

            if (userOpt.isPresent() && bookOpt.isPresent()) {
                User user = userOpt.get();
                Book book = bookOpt.get();

                // Validar progreso
                if (request.getProgressPercentage() != null && 
                    (request.getProgressPercentage() < 0.0 || request.getProgressPercentage() > 100.0)) {
                    throw new RuntimeException("El porcentaje de progreso debe estar entre 0 y 100");
                }

                // Check if user already has reading progress for this book
                ReadRelationship existingProgress = user.getReadBooks().stream()
                        .filter(r -> r.getBook().getId().equals(book.getId()))
                        .findFirst()
                        .orElse(null);

                if (existingProgress != null) {
                    // Update existing progress
                    existingProgress.setStatus(request.getStatus());
                    existingProgress.setProgressPercentage(request.getProgressPercentage());
                    existingProgress.setCurrentChapter(request.getCurrentChapter());
                    
                    if (request.getReadingTimeMinutes() != null && request.getReadingTimeMinutes() > 0) {
                        Integer currentTime = existingProgress.getTotalReadingTimeMinutes();
                        existingProgress.setTotalReadingTimeMinutes(
                                (currentTime != null ? currentTime : 0) + request.getReadingTimeMinutes());
                        existingProgress.setReadingSessions(
                                (existingProgress.getReadingSessions() != null ? existingProgress.getReadingSessions() : 0) + 1);
                    }
                    
                    // Set finish date if completed
                    if ("completed".equals(request.getStatus())) {
                        existingProgress.setFinishDate(LocalDateTime.now());
                    }
                } else {
                    // Create new reading progress
                    ReadRelationship readRelationship = new ReadRelationship(request.getStatus());
                    readRelationship.setBook(book);
                    readRelationship.setProgressPercentage(request.getProgressPercentage());
                    readRelationship.setCurrentChapter(request.getCurrentChapter());
                    readRelationship.setTotalReadingTimeMinutes(request.getReadingTimeMinutes() != null ? 
                            request.getReadingTimeMinutes() : 0);
                    readRelationship.setReadingSessions(1);
                    readRelationship.setStartDate(LocalDateTime.now());
                    
                    if ("completed".equals(request.getStatus())) {
                        readRelationship.setFinishDate(LocalDateTime.now());
                    }
                    
                    user.getReadBooks().add(readRelationship);
                }

                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar progreso de lectura: " + e.getMessage());
        }
    }

    public boolean addToFavorites(String userId, String bookId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Book> bookOpt = bookRepository.findById(bookId);

            if (userOpt.isPresent() && bookOpt.isPresent()) {
                User user = userOpt.get();
                Book book = bookOpt.get();

                if (!user.getFavoriteBooks().contains(book)) {
                    user.getFavoriteBooks().add(book);
                    userRepository.save(user);
                    return true;
                }
                return false; // Ya está en favoritos
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al agregar a favoritos: " + e.getMessage());
        }
    }

    public boolean removeFromFavorites(String userId, String bookId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<Book> bookOpt = bookRepository.findById(bookId);

            if (userOpt.isPresent() && bookOpt.isPresent()) {
                User user = userOpt.get();
                Book book = bookOpt.get();

                if (user.getFavoriteBooks().contains(book)) {
                    user.getFavoriteBooks().remove(book);
                    userRepository.save(user);
                    return true;
                }
                return false; // No estaba en favoritos
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al remover de favoritos: " + e.getMessage());
        }
    }

    public boolean followUser(String followerId, String followeeId) {
        try {
            if (followerId.equals(followeeId)) {
                throw new RuntimeException("No puedes seguirte a ti mismo");
            }

            Optional<User> followerOpt = userRepository.findById(followerId);
            Optional<User> followeeOpt = userRepository.findById(followeeId);

            if (followerOpt.isPresent() && followeeOpt.isPresent()) {
                User follower = followerOpt.get();
                User followee = followeeOpt.get();

                if (!follower.getFollowing().contains(followee)) {
                    follower.getFollowing().add(followee);
                    userRepository.save(follower);
                    return true;
                }
                return false; // Ya lo sigue
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al seguir usuario: " + e.getMessage());
        }
    }

    public boolean unfollowUser(String followerId, String followeeId) {
        try {
            Optional<User> followerOpt = userRepository.findById(followerId);
            Optional<User> followeeOpt = userRepository.findById(followeeId);

            if (followerOpt.isPresent() && followeeOpt.isPresent()) {
                User follower = followerOpt.get();
                User followee = followeeOpt.get();

                if (follower.getFollowing().contains(followee)) {
                    follower.getFollowing().remove(followee);
                    userRepository.save(follower);
                    return true;
                }
                return false; // No lo seguía
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Error al dejar de seguir usuario: " + e.getMessage());
        }
    }

    public List<Book> getUserRatedBooks(String userId) {
        return userRepository.findUserRatedBooks(userId);
    }

    public List<Book> getUserBooksByStatus(String userId, String status) {
        return userRepository.findUserBooksByReadStatus(userId, status);
    }

    public List<Book> getUserFavoriteBooks(String userId) {
        return userRepository.findUserFavoriteBooks(userId);
    }

    public List<User> getUserFollowing(String userId) {
        return userRepository.findUserFollowing(userId);
    }

    public List<User> getUserFollowers(String userId) {
        return userRepository.findUserFollowers(userId);
    }

    public List<User> getUsersWithSimilarTaste(String userId, Integer limit) {
        return userRepository.findUsersWithSimilarTaste(userId, 4.0, 3, limit);
    }

    public Double getUserAverageRating(String userId) {
        Double rating = userRepository.getUserAverageRating(userId);
        return rating != null ? rating : 0.0;
    }

    // Corregido: cambiar Integer a Long para consistencia con UserRepository
    public Long getUserCompletedBooksCount(String userId) {
        Long count = userRepository.countUserCompletedBooks(userId);
        return count != null ? count : 0L;
    }

    // Corregido: cambiar Integer a Long para consistencia con UserRepository
    public Long getUserTotalReadingTime(String userId) {
        Long time = userRepository.getUserTotalReadingTime(userId);
        return time != null ? time : 0L;
    }

    public Map<String, Object> getUserRatingForBook(String userId, String bookId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                Optional<RatedRelationship> ratingOpt = user.getRatedBooks().stream()
                        .filter(r -> r.getBook().getId().equals(bookId))
                        .findFirst();
                
                if (ratingOpt.isPresent()) {
                    RatedRelationship rating = ratingOpt.get();
                    Map<String, Object> result = new HashMap<>();
                    result.put("bookId", bookId);
                    result.put("rating", rating.getRating());
                    result.put("review", rating.getReview());
                    result.put("reviewTitle", rating.getReviewTitle());
                    result.put("helpfulVotes", rating.getHelpfulVotes());
                    result.put("createdAt", rating.getCreatedAt());
                    result.put("updatedAt", rating.getUpdatedAt());
                    return result;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener calificación del usuario: " + e.getMessage());
        }
    }

    public boolean markRecommendationAsClicked(String userId, String bookId) {
        // TODO: Implementar tracking de recomendaciones clickeadas
        // Por ahora retorna true como placeholder
        return true;
    }

    public boolean markRecommendationAsLiked(String userId, String bookId, boolean liked) {
        // TODO: Implementar feedback de recomendaciones
        // Por ahora retorna true como placeholder
        return true;
    }
}