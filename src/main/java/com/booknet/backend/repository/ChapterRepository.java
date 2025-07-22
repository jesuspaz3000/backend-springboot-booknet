package com.booknet.backend.repository;

import com.booknet.backend.model.Chapter;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends Neo4jRepository<Chapter, String> {

    @Query("MATCH (b:Book {id: $bookId})-[:HAS_CHAPTER]->(c:Chapter) " +
           "RETURN c ORDER BY c.chapter_number")
    List<Chapter> findChaptersByBookId(@Param("bookId") String bookId);

    @Query("MATCH (b:Book {id: $bookId})-[:HAS_CHAPTER]->(c:Chapter) " +
           "WHERE c.chapter_number = $chapterNumber RETURN c")
    Chapter findChapterByBookIdAndNumber(@Param("bookId") String bookId, @Param("chapterNumber") Integer chapterNumber);

    @Query("MATCH (b:Book {id: $bookId})-[:HAS_CHAPTER]->(c:Chapter) " +
           "RETURN COUNT(c) as total_chapters")
    Integer countChaptersByBookId(@Param("bookId") String bookId);

    @Query("MATCH (c:Chapter) WHERE c.title CONTAINS $searchTerm RETURN c")
    List<Chapter> findChaptersByTitleContaining(@Param("searchTerm") String searchTerm);

    @Query("MATCH (b:Book {id: $bookId})-[:HAS_CHAPTER]->(c:Chapter) " +
           "RETURN SUM(c.word_count) as total_words")
    Integer getTotalWordCountByBookId(@Param("bookId") String bookId);

    @Query("MATCH (b:Book {id: $bookId})-[:HAS_CHAPTER]->(c:Chapter) " +
           "RETURN SUM(c.reading_time_minutes) as total_reading_time")
    Integer getTotalReadingTimeByBookId(@Param("bookId") String bookId);
}