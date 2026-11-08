package com.dev09.kumaracoconutoil.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserLogDto {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private String id;
    private String userName;
    private String role;
    private String action;
    private LocalDateTime loggedAt;

    public UserLogDto() {
    }

    public UserLogDto(String id, String userName, String role, String action, LocalDateTime loggedAt) {
        this.id = id;
        this.userName = userName;
        this.role = role;
        this.action = action;
        this.loggedAt = loggedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }

    public String getDate() {
        return loggedAt == null ? "" : loggedAt.format(DATE_FMT);
    }

    public String getTime() {
        return loggedAt == null ? "" : loggedAt.format(TIME_FMT);
    }
}
