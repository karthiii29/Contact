package org.common.service;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

public class Category {

    private String categoryId;
    private String categoryName;

    public Category(String categoryName) {
        long timestamp = Instant.now().toEpochMilli(); // current time in ms
        int random = ThreadLocalRandom.current().nextInt(10000, 99999); // 5-digit random
        this.categoryId = String.valueOf(timestamp) + random;
        this.categoryName = categoryName;
    }

    public Category(String categoryId, String categoryName) {
        long timestamp = Instant.now().toEpochMilli(); // current time in ms
        int random = ThreadLocalRandom.current().nextInt(10000, 99999); // 5-digit random
        this.categoryId = String.valueOf(timestamp) + random;
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
