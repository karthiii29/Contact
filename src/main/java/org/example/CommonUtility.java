package org.example;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }




}
