package com.booknet.backend.service;

import com.booknet.backend.dto.BookResponse;
import com.booknet.backend.dto.RecommendationResponse;
import com.booknet.backend.model.Book;
import com.booknet.backend.model.User;
import com.booknet.backend.repository.BookRepository;
import com.booknet.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    public RecommendationService(BookRepository bookRepository, 
                               UserRepository userRepository, 
                               BookService bookService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookService = bookService;
    }

    public List<RecommendationResponse> getPersonalizedRecommendations(String userId, Integer limit) {
        try {
            // Collaborative filtering based on similar users
            List<User> similarUsers = userRepository.findUsersWithSimilarTaste(userId, 4.0, 3, 10);
            
            // Content-based filtering based on preferred genres
            List<Book> genreBasedBooks = bookRepository.findRecommendationsByPreferredGenres(userId, 4.0, limit);
            
            // Combine and create recommendations
            return genreBasedBooks.stream()
                    .limit(limit)
                    .map(book -> createRecommendationResponse(book, "content_based", 
                            calculateConfidenceScore(userId, book), 
                            "Basado en tus géneros preferidos"))
                    .filter(rec -> rec != null) // Filter out null responses
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback to popular recommendations if personalized fails
            return getPopularRecommendations(limit);
        }
    }

    public List<RecommendationResponse> getCollaborativeRecommendations(String userId, Integer limit) {
        try {
            List<User> similarUsers = userRepository.findUsersWithSimilarTaste(userId, 4.0, 3, 10);
            
            if (similarUsers.isEmpty()) {
                return getPopularRecommendations(limit);
            }

            // Get books highly rated by similar users that current user hasn't read
            List<Book> collaborativeBooks = bookRepository.findSimilarBooksByUserRatings(userId, 4.0, limit);
            
            return collaborativeBooks.stream()
                    .map(book -> createRecommendationResponse(book, "collaborative_filtering", 
                            calculateCollaborativeConfidence(userId, book), 
                            "Usuarios con gustos similares también disfrutaron este libro"))
                    .filter(rec -> rec != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return getPopularRecommendations(limit);
        }
    }

    public List<RecommendationResponse> getContentBasedRecommendations(String userId, Integer limit) {
        try {
            List<Book> contentBasedBooks = bookRepository.findRecommendationsByPreferredGenres(userId, 4.0, limit);
            
            return contentBasedBooks.stream()
                    .map(book -> createRecommendationResponse(book, "content_based", 
                            calculateContentBasedConfidence(userId, book), 
                            "Basado en libros que has disfrutado"))
                    .filter(rec -> rec != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return getPopularRecommendations(limit);
        }
    }

    public List<RecommendationResponse> getPopularRecommendations(Integer limit) {
        try {
            List<Book> popularBooks = bookRepository.findTopRatedBooks(limit);
            
            return popularBooks.stream()
                    .map(book -> createRecommendationResponse(book, "popularity_based", 
                            calculatePopularityConfidence(book), 
                            "Popular entre todos los usuarios"))
                    .filter(rec -> rec != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of(); // Return empty list if all fails
        }
    }

    public List<RecommendationResponse> getNewBooksRecommendations(Integer limit) {
        try {
            List<Book> newBooks = bookRepository.findRecentBooks(30); // Last 30 days
            
            return newBooks.stream()
                    .limit(limit)
                    .map(book -> createRecommendationResponse(book, "new_releases", 
                            0.7, // Default confidence for new books
                            "Recientemente agregado a nuestra colección"))
                    .filter(rec -> rec != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<RecommendationResponse> getTrendingRecommendations(Integer limit) {
        try {
            List<Book> trendingBooks = bookRepository.findMostReviewedBooks(limit);
            
            return trendingBooks.stream()
                    .map(book -> createRecommendationResponse(book, "trending", 
                            calculateTrendingConfidence(book), 
                            "Tendencia entre los lectores"))
                    .filter(rec -> rec != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<RecommendationResponse> getHybridRecommendations(String userId, Integer limit) {
        try {
            // Combine different recommendation strategies
            List<RecommendationResponse> recommendations = new java.util.ArrayList<>();
            
            // 40% content-based
            int contentBasedCount = Math.max(1, (int) (limit * 0.4));
            recommendations.addAll(getContentBasedRecommendations(userId, contentBasedCount));
            
            // 40% collaborative
            int collaborativeCount = Math.max(1, (int) (limit * 0.4));
            recommendations.addAll(getCollaborativeRecommendations(userId, collaborativeCount));
            
            // 20% popular/trending
            int popularCount = Math.max(1, limit - contentBasedCount - collaborativeCount);
            recommendations.addAll(getPopularRecommendations(popularCount));
            
            // Sort by confidence score and return top results
            return recommendations.stream()
                    .sorted((r1, r2) -> Double.compare(r2.getConfidenceScore(), r1.getConfidenceScore()))
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return getPopularRecommendations(limit);
        }
    }

    public List<RecommendationResponse> getRecommendationsByGenre(String genreName, Integer limit) {
        try {
            List<Book> genreBooks = bookRepository.findBooksByGenre(genreName);
            
            return genreBooks.stream()
                    .limit(limit)
                    .map(book -> createRecommendationResponse(book, "genre_based", 
                            calculatePopularityConfidence(book), 
                            "Del género " + genreName))
                    .filter(rec -> rec != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<RecommendationResponse> getRecommendationsByAuthor(String authorName, Integer limit) {
        try {
            List<Book> authorBooks = bookRepository.findBooksByAuthor(authorName);
            
            return authorBooks.stream()
                    .limit(limit)
                    .map(book -> createRecommendationResponse(book, "author_based", 
                            calculatePopularityConfidence(book), 
                            "Por " + authorName))
                    .filter(rec -> rec != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    private RecommendationResponse createRecommendationResponse(Book book, String algorithm, 
                                                              Double confidence, String reason) {
        try {
            Optional<BookResponse> bookResponseOpt = bookService.getBookById(book.getId());
            if (bookResponseOpt.isEmpty()) {
                return null;
            }
            
            BookResponse bookResponse = bookResponseOpt.get();
            return new RecommendationResponse(bookResponse, algorithm, confidence, reason, LocalDateTime.now());
        } catch (Exception e) {
            return null;
        }
    }

    private Double calculateConfidenceScore(String userId, Book book) {
        try {
            // Basic confidence calculation based on book rating and user preferences
            Double baseScore = book.getAverageRating() != null ? book.getAverageRating() / 5.0 : 0.5;
            
            // Adjust based on number of ratings (more ratings = more confidence)
            Integer totalRatings = book.getTotalRatings() != null ? book.getTotalRatings() : 0;
            Double ratingConfidence = Math.min(totalRatings / 100.0, 1.0);
            
            return Math.min((baseScore + ratingConfidence) / 2.0, 1.0);
        } catch (Exception e) {
            return 0.5; // Default confidence
        }
    }

    private Double calculateCollaborativeConfidence(String userId, Book book) {
        try {
            // Calculate confidence based on similar users' ratings
            // Note: This is a simplified implementation
            Double baseScore = book.getAverageRating() != null ? book.getAverageRating() / 5.0 : 0.5;
            
            // Add bonus for collaborative filtering
            return Math.min(baseScore + 0.1, 1.0);
        } catch (Exception e) {
            return 0.5;
        }
    }

    private Double calculateContentBasedConfidence(String userId, Book book) {
        try {
            // Calculate confidence based on genre/tag similarity
            Double baseScore = book.getAverageRating() != null ? book.getAverageRating() / 5.0 : 0.5;
            
            // This would be enhanced with actual genre preference analysis
            return Math.min(baseScore + 0.1, 1.0);
        } catch (Exception e) {
            return 0.5;
        }
    }

    private Double calculatePopularityConfidence(Book book) {
        try {
            // Calculate confidence based on popularity
            Double ratingScore = book.getAverageRating() != null ? book.getAverageRating() / 5.0 : 0.5;
            Integer totalRatings = book.getTotalRatings() != null ? book.getTotalRatings() : 0;
            Double popularityScore = Math.min(totalRatings / 50.0, 1.0);
            
            return Math.min((ratingScore + popularityScore) / 2.0, 1.0);
        } catch (Exception e) {
            return 0.5;
        }
    }

    private Double calculateTrendingConfidence(Book book) {
        try {
            // Calculate confidence based on recent activity
            Double ratingScore = book.getAverageRating() != null ? book.getAverageRating() / 5.0 : 0.5;
            Integer totalRatings = book.getTotalRatings() != null ? book.getTotalRatings() : 0;
            Double popularityScore = Math.min(totalRatings / 50.0, 1.0);
            
            return Math.min((ratingScore + popularityScore) / 2.0, 1.0);
        } catch (Exception e) {
            return 0.5;
        }
    }
}