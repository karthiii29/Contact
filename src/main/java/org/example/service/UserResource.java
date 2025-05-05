package org.example.service;


import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.apache.catalina.User;
import org.example.*;
import org.example.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "/contacts")
public class UserResource {

    @Autowired
    private UserRepository userRepository;

    @PostMapping(path = "/create")
    public ResponseEntity<ApiResponse<Map<String, String>>> createUser(@RequestBody CreateUserRequest request) {

        try {
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

            // Save user
            UserState user = new UserState();
            user.setFirstName(request.getFirstName());
            user.setMiddleName(request.getMiddleName());
            user.setLastName(request.getLastName());
            user.setEmailAddress(request.getEmailAddress());
            user.setMobileNumber(request.getMobileNumber());

            userRepository.save(user);

            // Prepare success response
            Map<String, String> data = new HashMap<>();
            data.put("userId", String.valueOf(user.getId()));
            data.put("status", "success");

            return ResponseEntity.ok(new ApiResponse<>(true, "User created successfully", data));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, APIMessages.CREATE_CONTACT_FAIL, null));
        }
    }


    @GetMapping(path = "/all")
    public @ResponseBody Iterable<UserState> getUserAPI() {
        return userRepository.findAll();
    }

    @PutMapping(path = "/update/{id}")
    public @ResponseBody UserState updateUserAPI(@PathVariable Integer id,
                                                 @RequestParam("firstName") String firstName,
                                                 @RequestParam("middleName") String middleName,
                                                 @RequestParam("lastName") String lastname,
                                                 @RequestParam("emailAddress") String emailAddress,
                                                 @RequestParam("mobileNumber") String mobileNumber ) {


        UserState user = userRepository.findById(id).get();

        try {

            if (firstName != null || emailAddress != null) {
                user.setFirstName(firstName);
                user.setEmailAddress(emailAddress);
            }

            user.setMiddleName(middleName);
            user.setLastName(lastname);
            user.setMobileNumber(mobileNumber);


        } catch (Exception e) {
            // logging
        }

        return user;
    }

    @DeleteMapping(path = "/delete/{id}")
    public @ResponseBody String deleteUserAPI(@PathVariable Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            return "User not found";
        }
        return "User Deleted";
    }

}
