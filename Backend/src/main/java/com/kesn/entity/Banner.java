package com.kesn.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "banners")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "ImageUrl", nullable = false, length = 1000)
    private String imageUrl;

    /** URL đích khi click vào banner (có thể null) */
    @Column(name = "LinkUrl", length = 500)
    private String linkUrl;

    /** Thứ tự hiển thị — số nhỏ hiện trước */
    @Column(name = "Position")
    private Integer sortOrder = 0;

    /** true = đang hiển thị, false = ẩn */
    @Column(name = "IsActive", nullable = false)
    private Boolean active = true;

    @Column(name = "StartAt")
    private Instant createdAt = Instant.now();

    // Mặc định cho EndAt là 10 năm kể từ lúc tạo để tránh lỗi NOT NULL
    @Column(name = "EndAt", nullable = false)
    private Instant endAt = Instant.now().plusSeconds(365L * 24 * 60 * 60 * 10);

    // ---------- Getters / Setters ----------
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }
}
