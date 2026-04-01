package com.diary;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * FileManager: Handles reading from and writing to the .txt file, 
 * passing data through CryptoUtil.
 */
public class FileManager {
    private static final String FILE_NAME = "secure_diary.txt";
    private CryptoUtil cryptoUtil;

    public FileManager(CryptoUtil cryptoUtil) {
        this.cryptoUtil = cryptoUtil;
    }

    /**
     * Saves a list of entries to the local file. The entries are serialized to 
     * bytes, then encrypted, then written as Base64 strings.
     */
    public void saveEntries(List<Entry> entries) throws Exception {
        // Serialize entries list to string representation
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(entries);
        oos.close();

        // Convert the byte array into a Base64 string so we have plain text to encrypt
        String plainTextData = Base64.getEncoder().encodeToString(baos.toByteArray());

        // Encrypt the plain text string representing the serial data
        String encryptedData = cryptoUtil.encrypt(plainTextData);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            writer.write(encryptedData);
        }
    }

    /**
     * Extracts and decrypts the entries from the file.
     */
    @SuppressWarnings("unchecked")
    public List<Entry> loadEntries() throws Exception {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>(); // Return empty list if no file exists
        }

        StringBuilder encryptedData = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                encryptedData.append(line);
            }
        }

        if (encryptedData.length() == 0) {
            return new ArrayList<>();
        }

        // Decrypt the data back into a serialized Base64 string
        String decryptedPlainData = cryptoUtil.decrypt(encryptedData.toString());
        
        // Decode the Base64 string back into bytes
        byte[] dataBytes = Base64.getDecoder().decode(decryptedPlainData);

        // Deserialize the bytes back into List<Entry>
        ByteArrayInputStream bais = new ByteArrayInputStream(dataBytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        ois.close();

        return (List<Entry>) obj;
    }
}
