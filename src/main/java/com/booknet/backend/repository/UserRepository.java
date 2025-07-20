package com.booknet.backend.repository;

import com.booknet.backend.model.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends Neo4jRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query("MATCH (u:User) RETURN u.role = $role return u")
    List<User> findByRole(String role);

    @Query("MATCH (u:User) RETURN u ORDER BY u.createdAt DESC")
    List<User> findAllOrderByCreatedAtDesc();

    @Query("MATCH (u:User) RETURN u ORDER BY u.createdAt DESC SKIP $offset LIMIT $limit")
    List<User> findAllOrderByCreatedAtDescWithPagination(int offset, int limit);

    @Query("MATCH (u:User) RETURN count(u)")
    long countAllUsers();
}
