package com.booknet.backend.model;

import com.booknet.backend.model.relationship.RatedRelationship;
import com.booknet.backend.model.relationship.ReadRelationship;
import com.booknet.backend.model.relationship.RecommendedRelationship;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Node("User")
public class User {

    @Id
    private String id;

    @Property("email")
    private String email;

    @Property("username")
    private String username;

    @Property("password")
    private String password;

    @Property("role")
    private String role;

    @Property("first_name")
    private String firstName;

    @Property("last_name")
    private String lastName;

    @Property("birth_date")
    private LocalDate birthDate;

    @Property("country")
    private String country;

    @Property("preferred_language")
    private String preferredLanguage;

    @Property("profile_image")
    private String profileImage;

    @Property("reading_level")
    private String readingLevel; // "beginner", "intermediate", "advanced"

    @Property("created_at")
    private LocalDateTime createdAt;

    @Property("updated_at")
    private LocalDateTime updatedAt;

    @Relationship(type = "RATED", direction = Relationship.Direction.OUTGOING)
    private Set<RatedRelationship> ratedBooks = new HashSet<>();

    @Relationship(type = "READ", direction = Relationship.Direction.OUTGOING)
    private Set<ReadRelationship> readBooks = new HashSet<>();

    @Relationship(type = "FAVORITE", direction = Relationship.Direction.OUTGOING)
    private Set<Book> favoriteBooks = new HashSet<>();

    @Relationship(type = "RECOMMENDED", direction = Relationship.Direction.INCOMING)
    private Set<RecommendedRelationship> recommendedBooks = new HashSet<>();

    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private Set<User> following = new HashSet<>();

    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    private Set<User> followers = new HashSet<>();

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String email, String username, String password, String role) {
        this();
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.preferredLanguage = "es"; // Default language
        this.readingLevel = "intermediate"; // Default reading level
    }

    public User(String email, String username, String password, String role, 
                String firstName, String lastName, LocalDate birthDate, String country) {
        this(email, username, password, role);
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getReadingLevel() {
        return readingLevel;
    }

    public void setReadingLevel(String readingLevel) {
        this.readingLevel = readingLevel;
    }

    public Set<RatedRelationship> getRatedBooks() {
        return ratedBooks;
    }

    public void setRatedBooks(Set<RatedRelationship> ratedBooks) {
        this.ratedBooks = ratedBooks;
    }

    public Set<ReadRelationship> getReadBooks() {
        return readBooks;
    }

    public void setReadBooks(Set<ReadRelationship> readBooks) {
        this.readBooks = readBooks;
    }

    public Set<Book> getFavoriteBooks() {
        return favoriteBooks;
    }

    public void setFavoriteBooks(Set<Book> favoriteBooks) {
        this.favoriteBooks = favoriteBooks;
    }

    public Set<RecommendedRelationship> getRecommendedBooks() {
        return recommendedBooks;
    }

    public void setRecommendedBooks(Set<RecommendedRelationship> recommendedBooks) {
        this.recommendedBooks = recommendedBooks;
    }

    public Set<User> getFollowing() {
        return following;
    }

    public void setFollowing(Set<User> following) {
        this.following = following;
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        this.followers = followers;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role='" + role + '\'' +
                ", country='" + country + '\'' +
                ", readingLevel='" + readingLevel + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
