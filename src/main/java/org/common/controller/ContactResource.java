package org.common.controller;

import org.common.repository.ContactRepository;
import org.common.service.UserState;
import org.common.util.APIMessages;
import org.common.util.ApiResponse;
import org.common.util.CommonUtility;
import org.common.util.CreateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.common.util.CommonUtility.buildContactFromRequest;
import static org.common.util.CommonUtility.contactToMap;

@RestController
@RequestMapping("/contacts")
public class ContactResource {

    @Autowired
    private ContactRepository contactRepository;

    // Create Contact
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createContact(@RequestBody CreateUserRequest request) {
        try {
            String validationError = CommonUtility.validateContactInfo(request.getEmailAddress(), request.getMobileNumber());
            if (validationError != null) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, validationError, null));
            }

            UserState contact = buildContactFromRequest(request);
            contactRepository.save(contact);

            Map<String, String> data = Map.of(
                    "contactId", String.valueOf(contact.getId()),
                    "status", "success"
            );

            return ResponseEntity.ok(new ApiResponse<>(true, APIMessages.SUCCESS_MESSAGE, data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, APIMessages.CREATE_CONTACT_FAIL, null));
        }
    }

    // Get Contact by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getContactById(@PathVariable Long id) {
        return contactRepository.findById(id)
                .map(contact -> {
                    Map<String, String> data = contactToMap(contact);
                    return ResponseEntity.ok(new ApiResponse<>(true, APIMessages.CONTACT_FETCHED, data));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null)));
    }

    // Get All Contacts
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllContacts() {
        try {
            List<UserState> contacts = (List<UserState>) contactRepository.findAll();
            if (contacts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "No contacts found", null));
            }

            List<Map<String, String>> contactList = contacts.stream()
                    .map(CommonUtility::contactToMap)
                    .toList();

            return ResponseEntity.ok(new ApiResponse<>(true, "Contacts are fetched successfully", contactList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch contacts", null));
        }
    }

    // Search Contacts by firstName, lastName, emailAddress
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> searchContactsByFields(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String emailAddress) {

        try {
            List<UserState> contacts = contactRepository.searchByFields(
                    firstName != null && !firstName.isBlank() ? firstName.trim() : null,
                    lastName != null && !lastName.isBlank() ? lastName.trim() : null,
                    emailAddress != null && !emailAddress.isBlank() ? emailAddress.trim() : null
            );

            if (contacts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "No matching contacts found", null));
            }

            List<Map<String, String>> result = contacts.stream()
                    .map(CommonUtility::contactToMap)
                    .toList();

            return ResponseEntity.ok(new ApiResponse<>(true, "Contacts matched successfully", result));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to search contacts", null));
        }
    }



    // Update Contact
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateContact(
            @PathVariable Long id, @RequestBody CreateUserRequest request) {

        Optional<UserState> existingContactOpt = contactRepository.findById(id);

        if (existingContactOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null));
        }

        String validationError = CommonUtility.validateContactInfo(request.getEmailAddress(), request.getMobileNumber());
        if (validationError != null) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, validationError, null));
        }

        try {

            UserState contact = buildContactFromRequest(request, id);
            contactRepository.save(contact);

            Map<String, String> data = Map.of(
                    "ContactId", String.valueOf(contact.getId()),
                    "status", "updated"
            );

            return ResponseEntity.ok(new ApiResponse<>(true, APIMessages.UPDATE_SUCCESS, data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, APIMessages.UPDATE_CONTACT_FAIL, null));
        }
    }

    // Delete Contact
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteContactById(@PathVariable Long id) {
        Optional<UserState> optionalUser = contactRepository.findById(id);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null));
        }

        try {
            contactRepository.deleteById(id);

            Map<String, String> data = Map.of(
                    "ContactId", String.valueOf(id),
                    "status", "deleted"
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Contact deleted successfully", data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to delete contact", null));
        }
    }

    // Helper Method: Convert contact to map

}
