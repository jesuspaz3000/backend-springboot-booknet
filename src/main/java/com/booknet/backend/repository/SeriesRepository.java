package com.booknet.backend.repository;

import com.booknet.backend.model.Series;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeriesRepository extends Neo4jRepository<Series, String> {

    List<Series> findByNameContainingIgnoreCase(String name);

    Optional<Series> findByName(String name);

    List<Series> findByIsCompleted(Boolean isCompleted);

    @Query("MATCH (s:Series) RETURN s ORDER BY s.total_books DESC LIMIT $limit")
    List<Series> findLongestSeries(@Param("limit") Integer limit);

    @Query("MATCH (s:Series)-[:PART_OF_SERIES]-(b:Book) " +
           "WHERE b.average_rating >= $minRating " +
           "RETURN s, AVG(b.average_rating) as avg_rating ORDER BY avg_rating DESC LIMIT $limit")
    List<Series> findTopRatedSeries(@Param("minRating") Double minRating, @Param("limit") Integer limit);

    @Query("MATCH (s:Series)-[:PART_OF_SERIES]-(b:Book) " +
           "RETURN s, COUNT(b) as actual_book_count " +
           "ORDER BY actual_book_count DESC LIMIT $limit")
    List<Series> findMostPopularSeries(@Param("limit") Integer limit);

    @Query("MATCH (u:User {id: $userId})-[:READ]->(b:Book)-[:PART_OF_SERIES]->(s:Series) " +
           "RETURN s, COUNT(b) as books_read ORDER BY books_read DESC")
    List<Series> findUserReadSeries(@Param("userId") String userId);
}