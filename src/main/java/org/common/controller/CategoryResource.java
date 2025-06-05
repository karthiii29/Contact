package org.common.controller;

import org.common.repository.CategoryRepository;
import org.common.service.Category;
import org.common.util.APIMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categories")
public class CategoryResource {

    @Autowired
    private CategoryRepository categoryService;

    private Map<String, Object> buildResponse(boolean success, String message, Object data, HttpStatus notFound) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return new ResponseEntity<>(buildResponse(false, "Category name cannot be empty", null, HttpStatus.NOT_FOUND), HttpStatus.BAD_REQUEST);
        }

        Category category = new Category(categoryName);

        categoryService.save(category);

        Map<String, String> data = Map.of("categoryId", category.getCategoryId(), "status", "success");
        return new ResponseEntity<>(buildResponse(true, "Category created successfully", data, HttpStatus.NOT_FOUND), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable String id) {
        Optional<Category> category = categoryService.findById(id);
        if (category.isEmpty()) {
            return new ResponseEntity<>(buildResponse(false, "Category not found", null, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        Map<String, String> data = Map.of("categoryId", category.get().getCategoryId(), "categoryName", category.get().getCategoryName());
        return new ResponseEntity<>(buildResponse(true, "Category fetched successfully", data, HttpStatus.NOT_FOUND), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        if (categories.isEmpty()) {
            return new ResponseEntity<>(buildResponse(false, "No categories found", null, HttpStatus.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        List<Map<String, String>> data = categories.stream()
                .map(c -> Map.of("categoryId", c.getCategoryId(), "categoryName", c.getCategoryName()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(buildResponse(true, "Categories fetched successfully", data, HttpStatus.NOT_FOUND), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable String id, @RequestBody String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return new ResponseEntity<>(buildResponse(false, "Category name cannot be empty", null, HttpStatus.NOT_FOUND), HttpStatus.BAD_REQUEST);
        }
        Category category = new Category(id, categoryName.trim());
        categoryService.save(category);
        Map<String, String> data = Map.of("categoryId", category.getCategoryId(), "status", "updated");
        return new ResponseEntity<>(buildResponse(true, "Category updated successfully", data, HttpStatus.NOT_FOUND), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public CompletableFuture<Map<String, Object>> deleteCategory(@PathVariable String id) {
        if (categoryService.findById(id).isEmpty()) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null, HttpStatus.NOT_FOUND));
        }

        categoryService.deleteById(id);
        Map<String, String> data = Map.of("categoryId", String.valueOf(id), "status", "deleted");
        return CompletableFuture.completedFuture(
                buildResponse(true, "Contact deleted successfully", data, HttpStatus.OK));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        return new ResponseEntity<>(buildResponse(false, "An error occurred: " + e.getMessage(), null, HttpStatus.NOT_FOUND), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}