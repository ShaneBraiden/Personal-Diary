package com.diary;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * CryptoUtil: Handles the AES encryption and decryption logic.
 * Generates the key from the user's password.
 */
public class CryptoUtil {
    private static final String ALGORITHM = "AES";
    private SecretKey secretKey;

    /**
     * Initializes the CryptoUtil with a user password.
     * Uses SHA-256 to hash the password into a valid 256-bit AES key.
     */
    public CryptoUtil(String password) throws Exception {
        byte[] keyBytes = password.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        keyBytes = sha.digest(keyBytes);
        keyBytes = Arrays.copyOf(keyBytes, 16); // Use 128-bit or 256-bit
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * Encrypts the given plain text string.
     */
    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypts the given Base64 encoded encrypted string.
     */
    public String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
