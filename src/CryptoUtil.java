import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * CryptoUtil handles the logic of converting plain text strings to encrypted bytes 
 * (Base64 encoded) and vice versa, using AES-GCM and PBKDF2 for key derivation.
 */
public class CryptoUtil {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int GCM_TAG_LENGTH_BIT = 128; // in bits
    private static final int GCM_IV_LENGTH_BYTE = 12; // in bytes
    private static final int SALT_LENGTH_BYTE = 16;
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH_BIT = 256;

    /**
     * Derives a secret key from the given password and salt.
     */
    private static SecretKey getSecretKey(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BIT);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        byte[] secretKeyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(secretKeyBytes, ALGORITHM);
    }

    /**
     * Encrypts plain text using the user's password.
     */
    public static String encrypt(String plainText, String password) throws Exception {
        // Generate random salt and IV
        byte[] salt = new byte[SALT_LENGTH_BYTE];
        byte[] iv = new byte[GCM_IV_LENGTH_BYTE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);
        random.nextBytes(iv);

        // Derive key and initialize cipher
        SecretKey secretKey = getSecretKey(password, salt);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Package salt, IV, and cipherText together into one byte array
        ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + cipherText.length);
        byteBuffer.put(salt);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);

        // Encode to Base64 to save as string
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    /**
     * Decrypts Base64 encrypted text using the user's password.
     */
    public static String decrypt(String encryptedText, String password) throws Exception {
        // Decode Base64 string to bytes
        byte[] cipherMessage = Base64.getDecoder().decode(encryptedText);
        ByteBuffer byteBuffer = ByteBuffer.wrap(cipherMessage);

        // Extract salt, IV, and cipherText
        byte[] salt = new byte[SALT_LENGTH_BYTE];
        byteBuffer.get(salt);
        
        byte[] iv = new byte[GCM_IV_LENGTH_BYTE];
        byteBuffer.get(iv);
        
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        // Derive key and initialize cipher
        SecretKey secretKey = getSecretKey(password, salt);
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

        // Perform decryption
        byte[] plainTextBytes = cipher.doFinal(cipherText);
        return new String(plainTextBytes, StandardCharsets.UTF_8);
    }
}
