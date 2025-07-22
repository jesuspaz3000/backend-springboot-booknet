package com.booknet.backend.repository;

import com.booknet.backend.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends Neo4jRepository<Book, String> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByLanguage(String language);

    List<Book> findByAgeRating(String ageRating);

    List<Book> findByReadingDifficulty(String readingDifficulty);

    Page<Book> findByPublicationYearBetween(Integer startYear, Integer endYear, Pageable pageable);

    @Query("MATCH (b:Book) WHERE b.average_rating >= $minRating RETURN b ORDER BY b.average_rating DESC")
    List<Book> findBooksByMinimumRating(@Param("minRating") Double minRating);

    @Query("MATCH (b:Book) RETURN b ORDER BY b.average_rating DESC LIMIT $limit")
    List<Book> findTopRatedBooks(@Param("limit") Integer limit);

    @Query("MATCH (b:Book) RETURN b ORDER BY b.total_ratings DESC LIMIT $limit")
    List<Book> findMostReviewedBooks(@Param("limit") Integer limit);

    @Query("MATCH (b:Book)-[:BELONGS_TO_GENRE]->(g:Genre) WHERE g.name = $genreName RETURN b")
    List<Book> findBooksByGenre(@Param("genreName") String genreName);

    @Query("MATCH (b:Book)-[:WRITTEN_BY]->(a:Author) WHERE a.name = $authorName RETURN b")
    List<Book> findBooksByAuthor(@Param("authorName") String authorName);

    @Query("MATCH (b:Book)-[:HAS_TAG]->(t:Tag) WHERE t.name = $tagName RETURN b")
    List<Book> findBooksByTag(@Param("tagName") String tagName);

    @Query("MATCH (b:Book)-[:PART_OF_SERIES]->(s:Series) WHERE s.name = $seriesName RETURN b ORDER BY b.title")
    List<Book> findBooksBySeries(@Param("seriesName") String seriesName);

    @Query(value = "MATCH (b:Book) " +
           "WHERE toLower(b.title) CONTAINS toLower($searchTerm) " +
           "OR ANY(author IN [(b)-[:WRITTEN_BY]->(a:Author) | a.name] WHERE toLower(author) CONTAINS toLower($searchTerm)) " +
           "OR ANY(genre IN [(b)-[:BELONGS_TO_GENRE]->(g:Genre) | g.name] WHERE toLower(genre) CONTAINS toLower($searchTerm)) " +
           "RETURN b",
           countQuery = "MATCH (b:Book) " +
           "WHERE toLower(b.title) CONTAINS toLower($searchTerm) " +
           "OR ANY(author IN [(b)-[:WRITTEN_BY]->(a:Author) | a.name] WHERE toLower(author) CONTAINS toLower($searchTerm)) " +
           "OR ANY(genre IN [(b)-[:BELONGS_TO_GENRE]->(g:Genre) | g.name] WHERE toLower(genre) CONTAINS toLower($searchTerm)) " +
           "RETURN COUNT(b)")
    Page<Book> searchBooksWithPagination(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query(value = "MATCH (b:Book)-[:BELONGS_TO_GENRE]->(g:Genre) WHERE g.name = $genreName RETURN b",
           countQuery = "MATCH (b:Book)-[:BELONGS_TO_GENRE]->(g:Genre) WHERE g.name = $genreName RETURN COUNT(b)")
    Page<Book> findBooksByGenreWithPagination(@Param("genreName") String genreName, Pageable pageable);

    @Query(value = "MATCH (b:Book)-[:WRITTEN_BY]->(a:Author) WHERE a.name = $authorName RETURN b",
           countQuery = "MATCH (b:Book)-[:WRITTEN_BY]->(a:Author) WHERE a.name = $authorName RETURN COUNT(b)")
    Page<Book> findBooksByAuthorWithPagination(@Param("authorName") String authorName, Pageable pageable);

    @Query("MATCH (u:User {id: $userId})-[r:RATED {rating: $rating}]->(b:Book) " +
           "MATCH (u)-[r2:RATED {rating: $rating}]->(similar_book:Book) " +
           "WHERE b <> similar_book " +
           "RETURN similar_book, COUNT(*) as co_ratings " +
           "ORDER BY co_ratings DESC LIMIT $limit")
    List<Book> findSimilarBooksByUserRatings(@Param("userId") String userId, 
                                             @Param("rating") Double rating, 
                                             @Param("limit") Integer limit);

    @Query("MATCH (u:User {id: $userId})-[:RATED {rating: $minRating}]->(b:Book)-[:BELONGS_TO_GENRE]->(g:Genre) " +
           "MATCH (recommended:Book)-[:BELONGS_TO_GENRE]->(g) " +
           "WHERE NOT (u)-[:read]->(recommended) " +
           "RETURN recommended, g.name as genre, COUNT(g) as genre_matches " +
           "ORDER BY genre_matches DESC LIMIT $limit")
    List<Book> findRecommendationsByPreferredGenres(@Param("userId") String userId, 
                                                     @Param("minRating") Double minRating,
                                                     @Param("limit") Integer limit);

    @Query("MATCH (b:Book) WHERE b.created_at >= datetime() - duration({days: $days}) " +
           "RETURN b ORDER BY b.created_at DESC")
    List<Book> findRecentBooks(@Param("days") Integer days);

    @Query("MATCH (b:Book) " +
           "WHERE toLower(b.title) CONTAINS toLower($searchTerm) " +
           "OR ANY(author IN [(b)-[:WRITTEN_BY]->(a:Author) | a.name] WHERE toLower(author) CONTAINS toLower($searchTerm)) " +
           "OR ANY(genre IN [(b)-[:BELONGS_TO_GENRE]->(g:Genre) | g.name] WHERE toLower(genre) CONTAINS toLower($searchTerm)) " +
           "RETURN b")
    List<Book> searchBooks(@Param("searchTerm") String searchTerm);

    @Query("MATCH (b:Book) RETURN b ORDER BY b.createdAt DESC SKIP $offset LIMIT $limit")
    List<Book> findAllOrderByCreatedAtDescWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    @Query("MATCH (b:Book) RETURN count(b)")
    long countAllBooks();
}