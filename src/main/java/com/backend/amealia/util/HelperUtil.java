package com.backend.amealia.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HelperUtil {
    public static boolean isBlank(String value) {
        return (value == null || value.trim().isEmpty() || "null".equals(value));
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
