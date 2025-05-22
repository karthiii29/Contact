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
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static String validateContactInfo(String firstName, String emailAddress, String mobileNumber) {
        // Check if at least one of firstName or emailAddress is provided
        if ((firstName == null || firstName.isBlank()) && (emailAddress == null || emailAddress.isBlank())) {
            return "mandatory fields are missing";
        }

        // Validate emailAddress if provided
        if (emailAddress != null && !emailAddress.isBlank() && !emailAddress.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Invalid email address";
        }

        // Validate mobileNumber if provided
        if (mobileNumber != null && !mobileNumber.isBlank() && !mobileNumber.matches("\\d{10}")) {
            return "Invalid mobile number";
        }

        return null;
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
