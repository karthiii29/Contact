package org.common.util;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.catalina.User;
import org.common.service.Category;
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
        // Mandatory fields
        data.put("ContactId", String.valueOf(contact.getId()));
        data.put("firstName", contact.getFirstName());
        data.put("emailAddress", contact.getEmailAddress());

        // Optional fields
        if (contact.getMiddleName() != null && !contact.getMiddleName().isBlank()) {
            data.put("middleName", contact.getMiddleName());
        }
        if (contact.getLastName() != null && !contact.getLastName().isBlank()) {
            data.put("lastName", contact.getLastName());
        }
        if (contact.getMobileNumber() != null && !contact.getMobileNumber().isBlank()) {
            data.put("mobileNumber", contact.getMobileNumber());
        }

        // Always include favorite (boolean has default value: false if not set)
        data.put("favorite", String.valueOf(contact.isFavorites()));

        // Only include categories if not null or empty
        if (contact.getCategories() != null && !contact.getCategories().isEmpty()) {
            data.put("categories", String.join(",", contact.getCategories()));
        }

        return data;
    }

    public static Map<String, String> categoryToMap(Category category) {
        Map<String, String> data = new HashMap<>();
        data.put("CategoryId", String.valueOf(category.getCategoryId()));
        data.put("CategoryName", category.getCategoryName());
        return data;
    }

    // Helper Method: Create contact object from request
    public static UserState buildContactFromRequest(UserState request) {
        UserState contact = new UserState();
        contact.setFirstName(request.getFirstName());
        contact.setMiddleName(request.getMiddleName());
        contact.setLastName(request.getLastName());
        contact.setEmailAddress(request.getEmailAddress());
        contact.setMobileNumber(request.getMobileNumber());
        contact.setFavorites(request.isFavorites());
        contact.setCategories(request.getCategories());
        return contact;
    }

    public static UserState buildContactFromRequest(UserState request, Integer id) {
        UserState contact = new UserState();
        contact.setId(Math.toIntExact(id));
        contact.setFirstName(request.getFirstName());
        contact.setMiddleName(request.getMiddleName());
        contact.setLastName(request.getLastName());
        contact.setEmailAddress(request.getEmailAddress());
        contact.setMobileNumber(request.getMobileNumber());
        contact.setFavorites(request.isFavorites());
        contact.setCategories(request.getCategories());
        return contact;
    }

    public static Category buildCategoryFromRequest(Category request) {
        Category category = new Category();
        category.setCategoryName(request.getCategoryName());
        return category;
    }

    public static Category buildCategoryFromRequest(Category request, Integer id) {
        Category category = new Category();
        category.setCategoryId(id);
        category.setCategoryName(request.getCategoryName());
        return category;
    }

}
