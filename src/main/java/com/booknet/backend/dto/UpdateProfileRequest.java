package com.booknet.backend.dto;

public class UpdateProfileRequest {
    private String currentPassword; // Requerido para verificar identidad
    private String newEmail;        // Opcional
    private String newUsername;     // Opcional
    private String newPassword;     // Opcional

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String currentPassword, String newEmail, String newUsername, String newPassword) {
        this.currentPassword = currentPassword;
        this.newEmail = newEmail;
        this.newUsername = newUsername;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public String getNewUsername() {
        return newUsername;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
