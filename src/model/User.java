package model;

import java.time.LocalDate;

public class User {
    private String user_id;
    private String fullname;
    private String username;
    private String thumbnail;
    private LocalDate yearold;
    private String email;
    private String phoneNumber;
    private String password;
    private Status status;
    private Role role;
    private boolean deleted = false;
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public User(String id, String fullName) {
        this.user_id = id;
        this.fullname = fullName;
    }

    public User(String user_id, String fullname, String username, String thumbnail, LocalDate yearold, String email, String phoneNumber, String password, Status status, Role role) {
        this.user_id = user_id;
        this.fullname = fullname;
        this.username = username;
        this.thumbnail = thumbnail;
        this.yearold = yearold;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.status = status;
        this.role = role;
    }

    public String getUserId() {
        return user_id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getUsername() {
        return username;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public LocalDate getYearold() {
        return yearold;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public Status getStatus() {
        return status;
    }

    public Role getRole() {
        return role;
    }

    public Boolean deleted() {
        return deleted;
    }

    public void setPassword(String password) {
        this.password = password; // Đảm bảo gán giá trị mới
    }
}