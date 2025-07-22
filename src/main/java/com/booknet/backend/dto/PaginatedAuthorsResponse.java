package com.booknet.backend.dto;

import java.util.List;

public class PaginatedAuthorsResponse {
    private List<AuthorResponse> autores;
    private long totalAutores;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PaginatedAuthorsResponse() {}

    public PaginatedAuthorsResponse(List<AuthorResponse> autores, long totalAutores, int offset, int limit) {
        this.autores = autores;
        this.totalAutores = totalAutores;
        this.pageSize = limit;
        this.currentPage = (offset / limit) + 1;
        this.totalPages = (int) Math.ceil((double) totalAutores / limit);
        this.hasNext = (offset + limit) < totalAutores;
        this.hasPrevious = offset > 0;
    }

    // Getters y setters
    public List<AuthorResponse> getAutores() {
        return autores;
    }

    public void setAutores(List<AuthorResponse> autores) {
        this.autores = autores;
    }

    public long getTotalAutores() {
        return totalAutores;
    }

    public void setTotalAutores(long totalAutores) {
        this.totalAutores = totalAutores;
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
