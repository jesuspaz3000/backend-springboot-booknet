package com.booknet.backend.repository;

import com.booknet.backend.model.User;
import com.booknet.backend.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Consulta optimizada para login que no carga relaciones
    @Query("MATCH (u:User {username: $username}) RETURN u")
    Optional<User> findByUsernameForLogin(@Param("username") String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("MATCH (u:User) WHERE u.role = $role RETURN u")
    List<User> findByRole(@Param("role") String role);

    @Query("MATCH (u:User) RETURN u ORDER BY u.createdAt DESC")
    List<User> findAllOrderByCreatedAtDesc();

    // Métodos de paginación con consultas de conteo
    @Query(value = "MATCH (u:User) RETURN u ORDER BY u.createdAt DESC SKIP $skip LIMIT $limit",
           countQuery = "MATCH (u:User) RETURN count(u)")
    Page<User> findAllUsersOrderByCreatedAt(Pageable pageable);

    @Query(value = "MATCH (u:User) WHERE u.role = $role RETURN u ORDER BY u.createdAt DESC SKIP $skip LIMIT $limit",
           countQuery = "MATCH (u:User) WHERE u.role = $role RETURN count(u)")
    Page<User> findUsersByRole(@Param("role") String role, Pageable pageable);

    @Query(value = "MATCH (u:User) WHERE toLower(u.username) CONTAINS toLower($searchTerm) OR toLower(u.email) CONTAINS toLower($searchTerm) RETURN u ORDER BY u.createdAt DESC SKIP $skip LIMIT $limit",
           countQuery = "MATCH (u:User) WHERE toLower(u.username) CONTAINS toLower($searchTerm) OR toLower(u.email) CONTAINS toLower($searchTerm) RETURN count(u)")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Métodos de paginación manual (sin Page)
    @Query("MATCH (u:User) RETURN u ORDER BY u.createdAt DESC SKIP $offset LIMIT $limit")
    List<User> findAllOrderByCreatedAtDescWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    @Query("MATCH (u:User) RETURN count(u)")
    long countAllUsers();

    @Query("MATCH (u:User {id: $userId})-[r:RATED]->(b:Book) RETURN b ORDER BY r.rating DESC")
    List<Book> findUserRatedBooks(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId})-[r:READ]->(b:Book) WHERE r.status = $status RETURN b")
    List<Book> findUserBooksByReadStatus(@Param("userId") String userId, @Param("status") String status);

    @Query("MATCH (u:User {id: $userId})-[:FAVORITE]->(b:Book) RETURN b")
    List<Book> findUserFavoriteBooks(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId})-[r:RECOMMENDED]->(b:Book) " +
           "WHERE r.confidenceScore >= $minConfidence " +
           "RETURN b ORDER BY r.confidenceScore DESC LIMIT $limit")
    List<Book> findUserRecommendations(@Param("userId") String userId, 
                                       @Param("minConfidence") Double minConfidence,
                                       @Param("limit") Integer limit);

    @Query("MATCH (u1:User {id: $userId1})-[:RATED]->(b:Book)<-[:RATED]-(u2:User {id: $userId2}) " +
           "RETURN COUNT(b)")
    Long countCommonRatedBooks(@Param("userId1") String userId1, @Param("userId2") String userId2);

    @Query("MATCH (u1:User {id: $userId})-[r1:RATED]->(b:Book)<-[r2:RATED]-(u2:User) " +
           "WHERE u1 <> u2 AND r1.rating >= $minRating AND r2.rating >= $minRating " +
           "WITH u2, COUNT(b) as commonBooks " +
           "WHERE commonBooks >= $minCommonBooks " +
           "RETURN u2 ORDER BY commonBooks DESC LIMIT $limit")
    List<User> findUsersWithSimilarTaste(@Param("userId") String userId, 
                                         @Param("minRating") Double minRating,
                                         @Param("minCommonBooks") Integer minCommonBooks,
                                         @Param("limit") Integer limit);

    @Query("MATCH (u:User {id: $userId})-[:FOLLOWS]->(following:User) RETURN following")
    List<User> findUserFollowing(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId})<-[:FOLLOWS]-(follower:User) RETURN follower")
    List<User> findUserFollowers(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId})-[:FOLLOWS]->(following:User) RETURN count(following)")
    Long countUserFollowing(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId})<-[:FOLLOWS]-(follower:User) RETURN count(follower)")
    Long countUserFollowers(@Param("userId") String userId);

    @Query("MATCH (u:User) WHERE u.country = $country RETURN u")
    List<User> findUsersByCountry(@Param("country") String country);

    @Query("MATCH (u:User) WHERE u.readingLevel = $readingLevel RETURN u")
    List<User> findUsersByReadingLevel(@Param("readingLevel") String readingLevel);

    @Query("MATCH (u:User {id: $userId})-[r:RATED]->(b:Book) " +
           "RETURN AVG(r.rating)")
    Double getUserAverageRating(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId})-[r:READ]->(b:Book) " +
           "WHERE r.status = 'completed' " +
           "RETURN COUNT(b)")
    Long countUserCompletedBooks(@Param("userId") String userId);

    @Query("MATCH (u:User {id: $userId})-[r:READ]->(b:Book) " +
           "RETURN COALESCE(SUM(r.totalReadingTimeMinutes), 0)")
    Long getUserTotalReadingTime(@Param("userId") String userId);

    @Query(value = "MATCH (u:User)-[r:RATED]->(b:Book) " +
                  "WHERE b.id = $bookId " +
                  "RETURN u.id AS userId, u.username AS username, r.rating AS rating, r.review AS review")
    List<Map<String, Object>> findBookRatingsWithUserInfo(String bookId);

    @Query("MATCH (u:User)-[r:RATED]->(b:Book {id: $bookId}) RETURN u")
    List<User> findUsersWhoRatedBook(@Param("bookId") String bookId);
}
