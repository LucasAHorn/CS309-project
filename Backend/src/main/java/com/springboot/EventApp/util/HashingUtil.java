package com.springboot.EventApp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class will be used for any validation of private information (password, 2fa...)
 *
 * @author Lucas Horn
 */
public class HashingUtil {

    /**
     * This takes two strings (order matters), and creates a hex code stored in a string out of them.
     *
     * @param username      - the username
     * @param sensitiveInfo - the information meant to be stored or checked
     * @return a String of hex values - the hash of name and sensitive info
     * @throws NoSuchAlgorithmException if sha256 does not exist
     */
    public static String getHashCode(String username, String sensitiveInfo) throws NoSuchAlgorithmException {
        String str = username + sensitiveInfo;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            hexString.append(Integer.toHexString((b & 0xFF)));
        }
        return hexString.toString();
    }
}