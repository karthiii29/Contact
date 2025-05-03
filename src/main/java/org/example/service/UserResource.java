package org.example.service;


import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.apache.catalina.User;
import org.example.APIMessages;
import org.example.CommonUtility;
import org.example.UserState;
import org.example.repository.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping(path = "/contacts")
public class UserResource {

    @Autowired
    private UserRepository userRepository;

    @PostMapping(path = "/add")
    public @ResponseBody Response createUserAPI(
            @RequestParam("firstName") String firstName,
            @RequestParam("middleName") String middleName,
            @RequestParam("lastName") String lastName,
            @RequestParam("emailAddress") String emailAddress,
            @RequestParam("mobileNumber") String mobileNumber) {

        try {
            // Basic mandatory checks
            if (firstName == null || firstName.isEmpty() ||
                    emailAddress == null || emailAddress.isEmpty()) {
                return Response.serverError().entity(APIMessages.MANDATORY_ERROR).build();
            }

            // Validate email
            if (!CommonUtility.emailCheck(emailAddress)) {
                return Response.serverError().entity(APIMessages.EMAIL_ERROR).build();
            }

            // Validate mobile number
            if (!CommonUtility.mobileNumberCheck(mobileNumber)) {
                return Response.serverError().entity(APIMessages.MOBILE_NUMBER_ERROR).build();
            }

            // Create and populate user object
            UserState user = new UserState();
            user.setFirstName(firstName);
            user.setMiddleName(middleName);
            user.setLastName(lastName);
            user.setEmailAddress(emailAddress);
            user.setMobileNumber(mobileNumber);

            // Prepare success response
            JSONObject userJson = new JSONObject();
            userJson.put("userId", user.getId());
            userJson.put("status", "success");

            JSONArray resultArray = new JSONArray();
            resultArray.put(userJson);

            return CommonUtility.returnResponse(resultArray.toString(), true);

        } catch (Exception e) {
            return CommonUtility.returnResponse(APIMessages.CREATE_CONTACT_FAIL, false);
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
