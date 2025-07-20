package com.booknet.backend.dto;

import java.util.List;

public class PaginatedUsersResponse {
    private List<UserResponse> users;
    private long totalUsers;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public PaginatedUsersResponse() {}

    public PaginatedUsersResponse(List<UserResponse> users, long totalUsers, int offset, int limit) {
        this.users = users;
        this.totalUsers = totalUsers;
        this.pageSize = limit;
        this.currentPage = (offset / limit) + 1;
        this.totalPages = (int) Math.ceil((double) totalUsers / limit);
        this.hasNext = (offset + limit) < totalUsers;
        this.hasPrevious = offset > 0;
    }

    public List<UserResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserResponse> users) {
        this.users = users;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
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
