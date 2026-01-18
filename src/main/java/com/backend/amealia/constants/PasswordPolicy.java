package com.backend.amealia.constants;

public final class PasswordPolicy {
    private PasswordPolicy() {}

    public static final String REGEX =
            "^(?=.*?[a-z])(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[ !@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{12,}$";

    public static final String MESSAGE =
            "Password must be at least 12 characters long and contain " +
                    "at least one uppercase letter, one lowercase letter, " +
                    "one digit, and one special character.";
}
