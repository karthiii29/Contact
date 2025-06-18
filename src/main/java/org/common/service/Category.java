package org.common.service;

import jakarta.persistence.*;

@Entity
@Table(name = "categories") // optional, if you want to customize table name
public class Category {

    @Id
    @Column(name = "id")
    private Long id;
    private String categoryName;

    public Category() {
    }

    public Category(Long categoryId, String categoryName) {
        this.id = categoryId;
        this.categoryName = categoryName;
    }

    // Getters and setters
    public Long getCategoryId() {
        return id;
    }

    public void setCategoryId(Long categoryId) {
        this.id = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
