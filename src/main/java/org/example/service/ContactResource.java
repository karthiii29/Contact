package org.example.service;

import org.example.*;
import org.example.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
                    .map(this::contactToMap)
                    .toList();

            return ResponseEntity.ok(new ApiResponse<>(true, "Contacts are fetched successfully", contactList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch contacts", null));
        }
    }

    // Update Contact
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateContact(
            @PathVariable Long id, @RequestBody CreateUserRequest request) {

        Optional<UserState> existingContactOpt = contactRepository.findById(id);

        if (existingContactOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, APIMessages.USER_NOT_FOUND, null));
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
    private Map<String, String> contactToMap(UserState contact) {
        Map<String, String> data = new HashMap<>();
        data.put("ContactId", String.valueOf(contact.getId()));
        data.put("firstName", contact.getFirstName());
        data.put("middleName", contact.getMiddleName());
        data.put("lastName", contact.getLastName());
        data.put("emailAddress", contact.getEmailAddress());
        data.put("mobileNumber", contact.getMobileNumber());
        return data;
    }


    // Helper Method: Create contact object from request
    private UserState buildContactFromRequest(CreateUserRequest request) {
        UserState contact = new UserState();
        contact.setFirstName(request.getFirstName());
        contact.setMiddleName(request.getMiddleName());
        contact.setLastName(request.getLastName());
        contact.setEmailAddress(request.getEmailAddress());
        contact.setMobileNumber(request.getMobileNumber());
        return contact;
    }

    private UserState buildContactFromRequest(CreateUserRequest request, Long id) {
        UserState contact = new UserState();
        contact.setId(Math.toIntExact(id));
        contact.setFirstName(request.getFirstName());
        contact.setMiddleName(request.getMiddleName());
        contact.setLastName(request.getLastName());
        contact.setEmailAddress(request.getEmailAddress());
        contact.setMobileNumber(request.getMobileNumber());
        return contact;
    }
}
