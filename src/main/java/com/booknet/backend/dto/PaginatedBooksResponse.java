package com.booknet.backend.dto;

import java.util.List;

public class PaginatedBooksResponse {
    private List<BookResponse> books;
    private long totalBooks;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PaginatedBooksResponse() {}

    public PaginatedBooksResponse(List<BookResponse> books, long totalBooks, int offset, int limit) {
        this.books = books;
        this.totalBooks = totalBooks;
        this.pageSize = limit;
        this.currentPage = (offset / limit) + 1;
        this.totalPages = (int) Math.ceil((double) totalBooks / limit);
        this.hasNext = (offset + limit) < totalBooks;
        this.hasPrevious = offset > 0;
    }

    // Getters y setters
    public List<BookResponse> getBooks() {
        return books;
    }

    public void setBooks(List<BookResponse> books) {
        this.books = books;
    }

    public long getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(long totalBooks) {
        this.totalBooks = totalBooks;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
