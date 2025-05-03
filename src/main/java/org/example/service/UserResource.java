package org.example.service;


import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.apache.catalina.User;
import org.example.CommonUtility;
import org.example.UserState;
import org.example.repository.UserRepository;
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
    public @ResponseBody String createUserAPI(
                              @RequestParam("firstName") String firstName,
                              @RequestParam("middleName") String middleName,
                              @RequestParam("lastName") String lastname,
                              @RequestParam("emailAddress") String emailAddress,
                              @RequestParam("mobileNumber") String mobileNumber) {
        

        UserState user = new UserState();

        if (firstName != null || emailAddress != null) {
            user.setFirstName(firstName);

            if (CommonUtility.emailCheck(emailAddress)) {
                user.setEmailAddress(emailAddress);
            } else {
                return "Please enter email address properly!";
            }

        }

        user.setMiddleName(middleName);
        user.setLastName(lastname);

        if (CommonUtility.mobileNumberCheck(mobileNumber)) {
            user.setMobileNumber(mobileNumber);
        } else {
            return "Please enter mobile number properly!";
        }


        userRepository.save(user);

        return null;
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
