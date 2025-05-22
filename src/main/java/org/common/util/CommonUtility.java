package org.common.util;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.common.service.UserState;

import java.util.HashMap;
import java.util.Map;

public class CommonUtility {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String MOBILE_REGEX = "(\\+91)?[0-9]{10}";
    private static final String SIMPLE_MOBILE_REGEX = "\\d{10}";

    public static Response returnResponse(String responseJSON, boolean responseStatus) {
        return responseStatus
                ? Response.ok(responseJSON, MediaType.APPLICATION_JSON).build()
                : Response.serverError().entity(responseJSON).build();
    }

    public static boolean mobileNumberCheck(String number) {
        return number != null && number.matches(MOBILE_REGEX);
    }

    public static boolean emailCheck(String email) {
        return email != null && !email.isEmpty() && email.matches(EMAIL_REGEX);
    }

    public static String validateContactInfo(String firstName, String emailAddress, String mobileNumber) {
        boolean isFirstNameBlank = firstName == null || firstName.isBlank();
        boolean isEmailBlank = emailAddress == null || emailAddress.isBlank();
        boolean isMobileBlank = mobileNumber == null || mobileNumber.isBlank();

        if (isFirstNameBlank && isEmailBlank) {
            return "mandatory fields are missing";
        }

        if (!isEmailBlank && !emailAddress.matches(EMAIL_REGEX)) {
            return "Invalid email address";
        }

        if (!isMobileBlank && !mobileNumber.matches(SIMPLE_MOBILE_REGEX)) {
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
        return new UserState(
                request.getFirstName(),
                request.getMiddleName(),
                request.getLastName(),
                request.getEmailAddress(),
                request.getMobileNumber()
        );
    }

    public static UserState buildContactFromRequest(CreateUserRequest request, Long id) {
        UserState contact = buildContactFromRequest(request);
        contact.setId(Math.toIntExact(id));
        return contact;
    }
}