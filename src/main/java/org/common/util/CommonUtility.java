package org.common.util;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.common.service.UserState;

import java.util.HashMap;
import java.util.Map;

public class CommonUtility {
    public static Response returnResponse(String responseJSON, boolean responseStatus) {
        if (responseStatus) {
            return Response.ok(responseJSON, MediaType.APPLICATION_JSON).build();

        } else {
            return Response.serverError().entity(responseJSON).build();
        }
    }

    public static boolean mobileNumberCheck(String number) {
        return number.matches("(\\+91)?[0-9]{10}");
    }

    public static boolean emailCheck(String email) {

        if (email.isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static String validateContactInfo(String email, String mobile) {
        boolean isEmailValid = CommonUtility.emailCheck(email);
        boolean isMobileValid = CommonUtility.mobileNumberCheck(mobile);

        if (!isEmailValid || !isMobileValid) {
            StringBuilder errorMessage = new StringBuilder("Enter valid ");
            if (!isEmailValid && !isMobileValid) {
                errorMessage.append("email and mobile number.");
            } else if (!isEmailValid) {
                errorMessage.append("email address.");
            } else {
                errorMessage.append("mobile number.");
            }
            return errorMessage.toString();
        }

        return null; // Indicates no error
    }

    public static Map<String, String> contactToMap(UserState contact) {
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
    public static UserState buildContactFromRequest(CreateUserRequest request) {
        UserState contact = new UserState();
        contact.setFirstName(request.getFirstName());
        contact.setMiddleName(request.getMiddleName());
        contact.setLastName(request.getLastName());
        contact.setEmailAddress(request.getEmailAddress());
        contact.setMobileNumber(request.getMobileNumber());
        return contact;
    }

    public static UserState buildContactFromRequest(CreateUserRequest request, Long id) {
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
