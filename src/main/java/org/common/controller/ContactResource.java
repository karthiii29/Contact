package org.common.controller;

import org.common.repository.ContactRepository;
import org.common.service.UserState;
import org.common.util.APIMessages;
import org.common.util.ApiResponse;
import org.common.util.CommonUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.Valid;
import java.util.*;
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
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> createContact(
            @Valid @RequestBody UserState request) {
    String validationError = CommonUtility.validateContactInfo(request.getFirstName(), request.getEmailAddress(), request.getMobileNumber());
        if (validationError != null) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, validationError, null, HttpStatus.BAD_REQUEST));
        }

        UserState userState = CommonUtility.buildContactFromRequest(request);

        contactRepository.save(userState);
        Map<String, String> data = Map.of("contactId", String.valueOf(request.getId()), "status", "success");
        return CompletableFuture.completedFuture(
                buildResponse(true, APIMessages.SUCCESS_MESSAGE, data, HttpStatus.OK));
    }

    // Get Contact by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getContactById(@PathVariable Integer id) {
        Optional<UserState> contact = contactRepository.findById(id);
        return contact.map(c -> buildResponse(true, APIMessages.CONTACT_FETCHED, CommonUtility.contactToMap(c), HttpStatus.OK))
                .orElseGet(() -> buildResponse(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null, HttpStatus.NOT_FOUND));
    }

    // Get All Contacts
    @GetMapping("/all")
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
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> updateContact(
            @PathVariable Integer id, @Valid @RequestBody UserState request) {
        if (contactRepository.findById(id).isEmpty()) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null, HttpStatus.NOT_FOUND));
        }

        String validationError = CommonUtility.validateContactInfo(request.getFirstName(), request.getEmailAddress(), request.getMobileNumber());
        if (validationError != null) {
            return CompletableFuture.completedFuture(
                    buildResponse(false, validationError, null, HttpStatus.BAD_REQUEST));
        }

        UserState userState = CommonUtility.buildContactFromRequest(request, id);

        contactRepository.save(userState);
        Map<String, String> data = Map.of("contactId", String.valueOf(request.getId()), "status", "updated");
        return CompletableFuture.completedFuture(
                buildResponse(true, APIMessages.UPDATE_SUCCESS, data, HttpStatus.OK));
    }

    // Delete Contact
    @DeleteMapping("/delete/{id}")
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, String>>>> deleteContactById(@PathVariable Integer id) {
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

    // List contact based on category

    @GetMapping("/category/{id}")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllContactsByCategory(@PathVariable Integer id) {
        String categoryId = String.valueOf(id);

        List<Map<String, String>> filteredContacts = ((List<UserState>) contactRepository.findAll()).stream()
                .filter(c -> c.getCategories() != null &&
                        c.getCategories().stream()
                                .map(String::trim)
                                .anyMatch(cat -> cat.equals(categoryId)))
                .map(CommonUtility::contactToMap)
                .toList();

        if (filteredContacts.isEmpty()) {
            return buildResponse(false, "No contacts found in this category", null, HttpStatus.NOT_FOUND);
        }

        return buildResponse(true, "Contacts fetched successfully", filteredContacts, HttpStatus.OK);
    }

    @GetMapping("/favourite")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllFavouriteContacts() {
        List<UserState> allContacts = (List<UserState>) contactRepository.findAll();

        List<Map<String, String>> favouriteContacts = allContacts.stream()
                .filter(UserState::isFavorites)
                .map(CommonUtility::contactToMap)
                .collect(Collectors.toList()).reversed();

        if (favouriteContacts.isEmpty()) {
            return buildResponse(false, "No favourite contacts found", null, HttpStatus.NOT_FOUND);
        }

        return buildResponse(true, "Favourite contacts fetched successfully", favouriteContacts, HttpStatus.OK);
    }



}
