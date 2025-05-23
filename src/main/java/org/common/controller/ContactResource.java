package org.common.controller;

import org.common.repository.ContactRepository;
import org.common.service.UserState;
import org.common.util.APIMessages;
import org.common.util.ApiResponse;
import org.common.util.CreateUserRequest;
import org.common.util.CommonUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for managing contact information.
 *
 * This class exposes endpoints for:
 * - Creating a new contact
 * - Retrieving a contact by ID
 * - Retrieving all contacts
 * - Searching for contacts by fields
 * - Updating an existing contact
 * - Deleting a contact
 *
 * Utilizes asynchronous processing for write operations and response wrapping
 * for consistent API structure. Includes caching for optimized read performance.
 */

@RestController
@RequestMapping("/contacts")
public class ContactResource {

    @Autowired
    private ContactRepository contactRepository;

    // Centralized response builder
    private <T> ResponseEntity<ApiResponse<T>> buildResponse(boolean success, String message, T data, HttpStatus status) {
        return new ResponseEntity<>(new ApiResponse<>(success, message, data), status);
    }

    // Create Contact
    @PostMapping("/create")
    @Async
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> createContact(
            @Valid @RequestBody CreateUserRequest request) {
    String validationError = CommonUtility.validateContactInfo(request.getFirstName(), request.getEmailAddress(), request.getMobileNumber());
        if (validationError != null) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, validationError, null, HttpStatus.BAD_REQUEST));
        }

        UserState contact = CommonUtility.buildContactFromRequest(request);
        contactRepository.save(contact);
        Map<String, String> data = Map.of("contactId", String.valueOf(contact.getId()), "status", "success");
        return CompletableFuture.completedFuture(
                buildResponse(true, APIMessages.SUCCESS_MESSAGE, data, HttpStatus.OK));
    }

    // Get Contact by ID
    @GetMapping("/{id}")
    @Cacheable(value = "contacts", key = "#id")
    public ResponseEntity<ApiResponse<Map<String, String>>> getContactById(@PathVariable Long id) {
        Optional<UserState> contact = contactRepository.findById(id);
        return contact.map(c -> buildResponse(true, APIMessages.CONTACT_FETCHED, CommonUtility.contactToMap(c), HttpStatus.OK))
                .orElseGet(() -> buildResponse(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null, HttpStatus.NOT_FOUND));
    }

    // Get All Contacts
    @GetMapping("/all")
    @Cacheable(value = "allContacts")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllContacts() {
        List<UserState> contacts = (List<UserState>) contactRepository.findAll();
        if (contacts.isEmpty()) {
            return buildResponse(false, "No contacts found", null, HttpStatus.NOT_FOUND);
        }

        List<Map<String, String>> contactList = contacts.stream()
                .map(CommonUtility::contactToMap)
                .collect(Collectors.toList());
        return buildResponse(true, "Contacts fetched successfully", contactList, HttpStatus.OK);
    }

    // Search Contacts by firstName, lastName, emailAddress
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> searchContactsByFields(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String emailAddress) {
        List<UserState> contacts = contactRepository.searchByFields(
                firstName != null && !firstName.isBlank() ? firstName.trim() : null,
                lastName != null && !lastName.isBlank() ? lastName.trim() : null,
                emailAddress != null && !emailAddress.isBlank() ? emailAddress.trim() : null
        );

        if (contacts.isEmpty()) {
            return buildResponse(false, "No matching contacts found", null, HttpStatus.NOT_FOUND);
        }

        List<Map<String, String>> result = contacts.stream()
                .map(CommonUtility::contactToMap)
                .collect(Collectors.toList());
        return buildResponse(true, "Contacts matched successfully", result, HttpStatus.OK);
    }

    // Update Contact
    @PutMapping("/update/{id}")
    @CacheEvict(value = {"contacts", "allContacts"}, allEntries = true)
    @Async
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> updateContact(
            @PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        if (contactRepository.findById(id).isEmpty()) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null, HttpStatus.NOT_FOUND));
        }

        String validationError = CommonUtility.validateContactInfo(request.getFirstName(), request.getEmailAddress(), request.getMobileNumber());
        if (validationError != null) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, validationError, null, HttpStatus.BAD_REQUEST));
        }

        UserState contact = CommonUtility.buildContactFromRequest(request, id);
        contactRepository.save(contact);
        Map<String, String> data = Map.of("contactId", String.valueOf(contact.getId()), "status", "updated");
        return CompletableFuture.completedFuture(
                buildResponse(true, APIMessages.UPDATE_SUCCESS, data, HttpStatus.OK));
    }

    // Delete Contact
    @DeleteMapping("/delete/{id}")
    @CacheEvict(value = {"contacts", "allContacts"}, allEntries = true)
    @Async
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> deleteContactById(@PathVariable Long id) {
        if (contactRepository.findById(id).isEmpty()) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null, HttpStatus.NOT_FOUND));
        }

        contactRepository.deleteById(id);
        Map<String, String> data = Map.of("contactId", String.valueOf(id), "status", "deleted");
        return CompletableFuture.completedFuture(
                buildResponse(true, "Contact deleted successfully", data, HttpStatus.OK));
    }

    // Centralized exception handling
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        return buildResponse(false, "An unexpected error occurred: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
