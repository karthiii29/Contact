package org.example.utility;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class EmailValidator {

    // Define a regex pattern for a valid email address
    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^.-]+@[\\w.-]+\\.[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false; // Null email is invalid
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches(); // Returns true if the email matches the regex pattern
    }

    public static void main(String[] args) {
        // Test emails
        String[] testEmails = {
                "example@example.com",
                "user.name+tag+sorting@example.com",
                "user@sub.example.com",
                "invalid-email@",      // Invalid
                "user@.com",           // Invalid
                "user@domain.c",       // Invalid
                "user@domain.toolongtld"
        };

        // Validate and print results
        for (String email : testEmails) {
            System.out.println(email + " is valid: " + isValidEmail(email));
        }
    }
}

