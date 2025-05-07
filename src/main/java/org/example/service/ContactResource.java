package org.example.service;


import org.example.*;
import org.example.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/contacts")
public class ContactResource {

    @Autowired
    private ContactRepository contactRepository;

    @PostMapping(path = "/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createContact(@RequestBody CreateUserRequest request) {

        try {
            // Validate input
            String validationError = CommonUtility.validateContactInfo(request.getEmailAddress(), request.getMobileNumber());
            if (validationError != null) {
                return ResponseEntity.ok(new ApiResponse<>(false, validationError, null));
            }

            // Save contact
            UserState contact = new UserState();
            contact.setFirstName(request.getFirstName());
            contact.setMiddleName(request.getMiddleName());
            contact.setLastName(request.getLastName());
            contact.setEmailAddress(request.getEmailAddress());
            contact.setMobileNumber(request.getMobileNumber());

            contactRepository.save(contact);

            // Prepare success response
            Map<String, String> data = new HashMap<>();
            data.put("ContactId", String.valueOf(contact.getId()));
            data.put("status", "success");

            return ResponseEntity.ok(new ApiResponse<>(true, APIMessages.SUCCESS_MESSAGE, data));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, APIMessages.CREATE_CONTACT_FAIL, null));
        }
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getContactById(@PathVariable Long id) {
        try {
            Optional<UserState> optionalContact = contactRepository.findById(Math.toIntExact(id));

            if (optionalContact.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null));
            }

            UserState contact = optionalContact.get();
            Map<String, String> data = new HashMap<>();
            data.put("ContactId", String.valueOf(contact.getId()));
            data.put("firstName", contact.getFirstName());
            data.put("middleName", contact.getMiddleName());
            data.put("lastName", contact.getLastName());
            data.put("emailAddress", contact.getEmailAddress());
            data.put("mobileNumber", contact.getMobileNumber());

            return ResponseEntity.ok(new ApiResponse<>(true, APIMessages.CONTACT_FETCHED, data));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch Contact", null));
        }
    }

    @GetMapping(path = "/all")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getAllContacts() {
        try {
            List<UserState> contacts = (List<UserState>) contactRepository.findAll();

            if (contacts.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "", null));
            }

            List<Map<String, String>> contactList = new ArrayList<>();
            for (UserState contact : contacts) {
                Map<String, String> contactData = new HashMap<>();
                contactData.put("userId", String.valueOf(contact.getId()));
                contactData.put("firstName", contact.getFirstName());
                contactData.put("middleName", contact.getMiddleName());
                contactData.put("lastName", contact.getLastName());
                contactData.put("emailAddress", contact.getEmailAddress());
                contactData.put("mobileNumber", contact.getMobileNumber());
                contactList.add(contactData);
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "Contacts are fetched successfully", contactList));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to fetch Contacts", null));
        }
    }


    @PutMapping(path = "/update/{Id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> updateContact(
            @PathVariable Long Id,
            @RequestBody CreateUserRequest request) {

        try {

            // Find existing contact
            Optional<UserState> existingContactOpt = contactRepository.findById(Math.toIntExact(Id));
            if (existingContactOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, APIMessages.USER_NOT_FOUND, null));
            }

            // Mandatory checks
            if (request.getFirstName() == null || request.getFirstName().isEmpty() ||
                    request.getEmailAddress() == null || request.getEmailAddress().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(false, APIMessages.MANDATORY_ERROR, null));
            }

            // Email validation
            if (!CommonUtility.emailCheck(request.getEmailAddress())) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(false, APIMessages.EMAIL_ERROR, null));
            }

            // Mobile number validation
            if (!CommonUtility.mobileNumberCheck(request.getMobileNumber())) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(false, APIMessages.MOBILE_NUMBER_ERROR, null));
            }

            // Update contact data
            UserState contact = existingContactOpt.get();
            contact.setFirstName(request.getFirstName());
            contact.setMiddleName(request.getMiddleName());
            contact.setLastName(request.getLastName());
            contact.setEmailAddress(request.getEmailAddress());
            contact.setMobileNumber(request.getMobileNumber());

            contactRepository.save(contact);

            // Prepare success response
            Map<String, String> data = new HashMap<>();
            data.put("Id", String.valueOf(contact.getId()));
            data.put("status", "updated");

            return ResponseEntity.ok(new ApiResponse<>(true, APIMessages.UPDATE_SUCCESS, data));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, APIMessages.UPDATE_CONTACT_FAIL, null));
        }
    }


    @DeleteMapping(path = "/delete/{id}")
    public ResponseEntity<ApiResponse<Map<String, String>>> deleteContactById(@PathVariable Long id) {
        try {
            Optional<UserState> optionalUser = contactRepository.findById(Math.toIntExact(id));

            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, APIMessages.CONTACT_NOT_FOUND_ERROR, null));
            }

            contactRepository.deleteById(Math.toIntExact(id));

            Map<String, String> data = new HashMap<>();
            data.put("userId", String.valueOf(id));
            data.put("status", "deleted");

            return ResponseEntity.ok(new ApiResponse<>(true, "", data));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "", null));
        }
    }


}
