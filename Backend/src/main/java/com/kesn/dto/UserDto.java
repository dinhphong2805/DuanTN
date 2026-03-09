package com.kesn.dto;

public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String role;

    public UserDto(Long id, String email, String fullName, String phone, String role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
}
