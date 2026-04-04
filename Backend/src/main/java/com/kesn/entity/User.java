package com.kesn.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true) 
    private String password;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    @Column(nullable = false, length = 20)
    private String role = "customer";

    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "reset_code_expires_at")
    private java.time.Instant resetCodeExpiresAt;

 
    @Column(name = "provider")
    private String provider; // "google" hoặc "local"

    @Column(name = "provider_id")
    private String providerId; // ID định danh duy nhất từ Google

    @Column(name = "image_url")
    private String imageUrl; // Link ảnh đại diện từ Google

 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getResetCode() { return resetCode; }
    public void setResetCode(String resetCode) { this.resetCode = resetCode; }

    public java.time.Instant getResetCodeExpiresAt() { return resetCodeExpiresAt; }
    public void setResetCodeExpiresAt(java.time.Instant resetCodeExpiresAt) { this.resetCodeExpiresAt = resetCodeExpiresAt; }


    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}