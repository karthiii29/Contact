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
        Map<String, String> contactData = new HashMap<>();
        contactData.put("userId", String.valueOf(contact.getId()));
        contactData.put("firstName", contact.getFirstName());
        contactData.put("middleName", contact.getMiddleName());
        contactData.put("lastName", contact.getLastName());
        contactData.put("emailAddress", contact.getEmailAddress());
        contactData.put("mobileNumber", contact.getMobileNumber());
        return contactData;
    }



}
