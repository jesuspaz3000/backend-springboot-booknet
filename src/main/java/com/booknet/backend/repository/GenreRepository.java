package com.booknet.backend.repository;

import com.booknet.backend.model.Genre;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends Neo4jRepository<Genre, String> {

    List<Genre> findByNameContainingIgnoreCase(String name);

    Optional<Genre> findByName(String name);

    List<Genre> findByParentGenre(String parentGenre);

    @Query("MATCH (g:Genre) WHERE g.parent_genre IS NULL RETURN g ORDER BY g.name")
    List<Genre> findMainGenres();

    @Query("MATCH (g:Genre)-[:BELONGS_TO_GENRE]-(b:Book) " +
           "RETURN g, COUNT(b) as book_count ORDER BY book_count DESC LIMIT $limit")
    List<Genre> findMostPopularGenres(@Param("limit") Integer limit);

    @Query("MATCH (g:Genre)-[:BELONGS_TO_GENRE]-(b:Book) " +
           "WHERE b.average_rating >= $minRating " +
           "RETURN g, AVG(b.average_rating) as avg_rating ORDER BY avg_rating DESC LIMIT $limit")
    List<Genre> findTopRatedGenres(@Param("minRating") Double minRating, @Param("limit") Integer limit);

    @Query("MATCH (u:User {id: $userId})-[:RATED {rating: $minRating}]->(b:Book)-[:BELONGS_TO_GENRE]->(g:Genre) " +
           "RETURN g, COUNT(*) as preference_count ORDER BY preference_count DESC LIMIT $limit")
    List<Genre> findUserPreferredGenres(@Param("userId") String userId, 
                                        @Param("minRating") Double minRating,
                                        @Param("limit") Integer limit);

    @Query("MATCH (g:Genre) RETURN COUNT(g)")
    long countAllGenres();

    @Query("MATCH (g:Genre) RETURN g ORDER BY g.created_at DESC SKIP $offset LIMIT $limit")
    List<Genre> findAllOrderByCreatedAtDescWithPagination(@Param("offset") int offset, @Param("limit") int limit);
}