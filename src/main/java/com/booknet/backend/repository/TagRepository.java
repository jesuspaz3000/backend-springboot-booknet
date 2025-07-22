package com.booknet.backend.repository;

import com.booknet.backend.model.Tag;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends Neo4jRepository<Tag, String> {

    List<Tag> findByNameContainingIgnoreCase(String name);

    Optional<Tag> findByName(String name);

    List<Tag> findByCategory(String category);

    @Query("MATCH (t:Tag)-[:HAS_TAG]-(b:Book) " +
           "RETURN t, COUNT(b) as book_count ORDER BY book_count DESC LIMIT $limit")
    List<Tag> findMostUsedTags(@Param("limit") Integer limit);

    @Query("MATCH (t:Tag)-[:HAS_TAG]-(b:Book) " +
           "WHERE b.average_rating >= $minRating " +
           "RETURN t, AVG(b.average_rating) as avg_rating ORDER BY avg_rating DESC LIMIT $limit")
    List<Tag> findTopRatedTags(@Param("minRating") Double minRating, @Param("limit") Integer limit);

    @Query("MATCH (u:User {id: $userId})-[:RATED {rating: $minRating}]->(b:Book)-[:HAS_TAG]->(t:Tag) " +
           "RETURN t, COUNT(*) as preference_count ORDER BY preference_count DESC LIMIT $limit")
    List<Tag> findUserPreferredTags(@Param("userId") String userId, 
                                    @Param("minRating") Double minRating,
                                    @Param("limit") Integer limit);

    @Query("MATCH (b:Book {id: $bookId})-[:HAS_TAG]->(t:Tag) RETURN t")
    List<Tag> findTagsByBookId(@Param("bookId") String bookId);

    @Query("MATCH (t:Tag) WHERE t.category = $category RETURN DISTINCT t.name ORDER BY t.name")
    List<String> findDistinctTagNamesByCategory(@Param("category") String category);

    @Query("MATCH (t:Tag) RETURN COUNT(t)")
    long countAllTags();

    @Query("MATCH (t:Tag) RETURN t ORDER BY t.created_at DESC SKIP $offset LIMIT $limit")
    List<Tag> findAllOrderByCreatedAtDescWithPagination(@Param("offset") int offset, @Param("limit") int limit);
}