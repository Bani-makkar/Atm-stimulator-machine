package com.atm.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Cryptographic and validation utilities for PIN hashing and input verification.
 */
public final class SecurityUtil {

    private SecurityUtil() {
        // Utility class
    }

    /**
     * Hashes a raw PIN string using SHA-256 and returns the lowercase hexadecimal digest.
     *
     * @param rawPin The 4-digit PIN string
     * @return SHA-256 hex string
     */
    public static String hashPin(String rawPin) {
        if (rawPin == null) {
            throw new IllegalArgumentException("PIN cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawPin.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available in JVM", e);
        }
    }

    /**
     * Verifies whether a raw 4-digit PIN matches the stored SHA-256 hash.
     *
     * @param rawPin     Raw PIN entered by user
     * @param storedHash Stored hash (or plaintext fallback if legacy)
     * @return true if matched
     */
    public static boolean verifyPin(String rawPin, String storedHash) {
        if (rawPin == null || storedHash == null) {
            return false;
        }
        String computedHash = hashPin(rawPin);
        return computedHash.equalsIgnoreCase(storedHash) || rawPin.trim().equals(storedHash);
    }

    /**
     * Validates that a PIN is strictly 4 numeric digits.
     */
    public static boolean isValidPinFormat(String pin) {
        return pin != null && pin.matches("^\\d{4}$");
    }

    /**
     * Validates that an account number consists of 5 to 16 digits.
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && accountNumber.trim().matches("^\\d{5,16}$");
    }
}
