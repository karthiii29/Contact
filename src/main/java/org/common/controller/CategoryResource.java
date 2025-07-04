package org.common.controller;

import org.common.repository.CategoryRepository;
import org.common.service.Category;
import org.common.service.UserState;
import org.common.util.APIMessages;
import org.common.util.ApiResponse;
import org.common.util.CommonUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for managing category information.
 *
 * This class exposes endpoints for:
 * - Creating a new category
 * - Retrieving a category by ID
 * - Retrieving all categories
 * - Updating an existing category
 * - Deleting a category
 *
 * Utilizes asynchronous processing for write operations and response wrapping
 * for consistent API structure. Includes caching for optimized read performance.
 */

@RestController
@RequestMapping("/categories")
public class CategoryResource {

    @Autowired
    CategoryRepository categoryRepository;

    private <T> ResponseEntity<ApiResponse<T>> buildResponse(boolean success, String message, T data, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse<>(success, message, data), status);
    }

    // Create Contact
    @PostMapping("/create")
    @Async
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> createCategory(
            @Valid @RequestBody Category request) {

        Category category = CommonUtility.buildCategoryFromRequest(request);

        // Set generated long ID
        category.setCategoryId(CommonUtility.generateUniqueContactId());

        Category savedCategory = categoryRepository.save(category);

        Map<String, String> data = Map.of(
                "contactId", String.valueOf(savedCategory.getCategoryId()),
                "status", "success"
        );

        return CompletableFuture.completedFuture(
                buildResponse(true, APIMessages.SUCCESS_MESSAGE, data, HttpStatus.OK));
    }

    // Get Category by ID
    @GetMapping("/{id}")
    @Cacheable(value = "category", key = "#id")
    public ResponseEntity<ApiResponse<Optional<Category>>> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(c -> buildResponse(true, APIMessages.CONTACT_FETCHED, category, HttpStatus.OK))
                .orElseGet(() -> buildResponse(false, "Category not found", null, HttpStatus.NOT_FOUND));
    }

    // Get All Categories
    @GetMapping("/all")
    @Cacheable(value = "allCategories")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllCategories() {
        List<Category> categories = (List<Category>) categoryRepository.findAll();

        if (categories.isEmpty()) {
            return buildResponse(false, "No Categories found", null, HttpStatus.NOT_FOUND);
        }

        List<Map<String, String>> categoryList = categories.stream()
                .map(CommonUtility::categoryToMap)
                .collect(Collectors.toList());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("count", categoryList.size());
        responseData.put("categories", categoryList);

        return buildResponse(true, "Categories fetched successfully", responseData, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    @CacheEvict(value = {"categories", "allCategories"}, allEntries = true)
    @Async
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody Category request) {
        if (categoryRepository.findById(id).isEmpty()) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, "Category not found", null, HttpStatus.NOT_FOUND));
        }

        Category category = CommonUtility.buildCategoryFromRequest(request, id);

        Category savedCategory = categoryRepository.save(category);

        categoryRepository.save(category);
        Map<String, String> data = Map.of("categoryId", String.valueOf(request.getCategoryId()), "status", "updated");
        return CompletableFuture.completedFuture(
                buildResponse(true, "Category added successfully", data, HttpStatus.OK));
    }

    // Delete Contact
    @DeleteMapping("/delete/{id}")
    @CacheEvict(value = {"categories", "allCategories"}, allEntries = true)
    @Async
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> deleteCategoryById(@PathVariable Long id) {
        if (categoryRepository.findById(id).isEmpty()) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, "Category not found", null, HttpStatus.NOT_FOUND));
        }

        categoryRepository.deleteById(id);
        Map<String, String> data = Map.of("categoryId", String.valueOf(id), "status", "deleted");
        return CompletableFuture.completedFuture(
                buildResponse(true, "Category deleted successfully", data, HttpStatus.OK));
    }

}