package com.dev09.kumaracoconutoil.util;
import java.util.regex.Pattern;

public class Validation {

    // Email regex (simple but covers most cases)
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Name regex: letters, spaces, hyphens, apostrophes (adjust as needed)
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z\\s'-]+$");

    // Contact regex: numeric only, 10–15 digits (adjust length as needed)
    private static final Pattern CONTACT_PATTERN =
            Pattern.compile("^\\d{10,15}$");

    /**
     * Validates email format.
     * @param email email string to validate
     * @return true if email matches basic pattern
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates password length (minimum 6 characters).
     * @param password password string
     * @return true if password is not null and length >= 6
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Checks if a string can be parsed as an integer.
     * @param value string to check
     * @return true if integer
     */
    public static boolean isInteger(String value) {
        if (value == null) return false;
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a string can be parsed as a double.
     * @param value string to check
     * @return true if double
     */
    public static boolean isDouble(String value) {
        if (value == null) return false;
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validates a name (letters, spaces, hyphens, apostrophes only).
     * @param name name string
     * @return true if name matches pattern and is not blank
     */
    public static boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Validates a contact number (digits only, length between 10 and 15).
     * Adjust regex if you need different length.
     * @param contact contact string
     * @return true if contact matches pattern
     */
    public static boolean isValidContact(String contact) {
        return contact != null && CONTACT_PATTERN.matcher(contact).matches();
    }
}
