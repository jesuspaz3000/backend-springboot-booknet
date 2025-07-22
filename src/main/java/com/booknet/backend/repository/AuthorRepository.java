package com.booknet.backend.repository;

import com.booknet.backend.model.Author;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends Neo4jRepository<Author, String> {

    List<Author> findByNameContainingIgnoreCase(String name);

    Optional<Author> findByName(String name);

    List<Author> findByNationality(String nationality);

    @Query("MATCH (a:Author) WHERE a.birth_date.year >= $startYear AND a.birth_date.year <= $endYear RETURN a")
    List<Author> findAuthorsByBirthYearRange(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    @Query("MATCH (a:Author)-[:WRITTEN_BY]-(b:Book) RETURN a, COUNT(b) as book_count ORDER BY book_count DESC LIMIT $limit")
    List<Author> findMostProductiveAuthors(@Param("limit") Integer limit);

    @Query("MATCH (a:Author)-[:WRITTEN_BY]-(b:Book) WHERE b.average_rating >= $minRating " +
           "RETURN a, AVG(b.average_rating) as avg_rating ORDER BY avg_rating DESC LIMIT $limit")
    List<Author> findTopRatedAuthors(@Param("minRating") Double minRating, @Param("limit") Integer limit);

    @Query("MATCH (a:Author)-[:WRITTEN_BY]-(b:Book)-[:BELONGS_TO_GENRE]->(g:Genre) " +
           "WHERE g.name = $genreName RETURN DISTINCT a")
    List<Author> findAuthorsByGenre(@Param("genreName") String genreName);

    @Query("MATCH (a:Author) WHERE a.death_date IS NULL RETURN a ORDER BY a.name")
    List<Author> findLivingAuthors();

    @Query("MATCH (a:Author) WHERE a.death_date IS NOT NULL RETURN a ORDER BY a.name")
    List<Author> findDeceasedAuthors();

    @Query("MATCH (a:Author) RETURN a ORDER BY a.createdAt DESC SKIP $offset LIMIT $limit")
    List<Author> findAllOrderByCreatedAtDescWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    @Query("MATCH (a:Author) RETURN count(a)")
    long countAllAuthors();

    // Método para contar libros de un autor específico
    @Query("MATCH (a:Author {id: $authorId})-[:WRITTEN_BY]-(b:Book) RETURN COUNT(b)")
    long countBooksByAuthorId(@Param("authorId") String authorId);
}