package org.common.controller;

import org.common.repository.CategoryRepository;
import org.common.service.Category;
import org.common.util.APIMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryResource {

    @Autowired
    private CategoryRepository categoryRepository;

    private ResponseEntity<Map<String, Object>> buildResponse(boolean success, String message, Object data, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return new ResponseEntity<>(response, status);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return buildResponse(false, "Category name cannot be empty", null, HttpStatus.BAD_REQUEST);
        }

        Category category = new Category(categoryName.trim());
        categoryRepository.save(category);

        Map<String, String> data = Map.of("categoryId", category.getCategoryId(), "status", "success");
        return buildResponse(true, "Category created successfully", data, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable String id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            return buildResponse(false, "Category not found", null, HttpStatus.NOT_FOUND);
        }

        Map<String, String> data = Map.of(
                "categoryId", category.get().getCategoryId(),
                "categoryName", category.get().getCategoryName()
        );
        return buildResponse(true, "Category fetched successfully", data, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            return buildResponse(false, "No categories found", null, HttpStatus.NOT_FOUND);
        }

        List<Map<String, String>> data = categories.stream()
                .map(c -> Map.of("categoryId", c.getCategoryId(), "categoryName", c.getCategoryName()))
                .collect(Collectors.toList());

        return buildResponse(true, "Categories fetched successfully", data, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable String id, @RequestBody String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return buildResponse(false, "Category name cannot be empty", null, HttpStatus.BAD_REQUEST);
        }

        Category updatedCategory = new Category(id, categoryName.trim());
        categoryRepository.save(updatedCategory);

        Map<String, String> data = Map.of("categoryId", id, "status", "updated");
        return buildResponse(true, "Category updated successfully", data, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable String id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isEmpty()) {
            return buildResponse(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null, HttpStatus.NOT_FOUND);
        }

        categoryRepository.deleteById(id);

        Map<String, String> data = Map.of("categoryId", id, "status", "deleted");
        return buildResponse(true, "Category deleted successfully", data, HttpStatus.OK);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        return buildResponse(false, "An error occurred: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
