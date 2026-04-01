import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DiaryManager handles the logic of our application.
 * It separation concerns by keeping data manipulation away from the GUI.
 */
public class DiaryManager {
    private List<Entry> entries;
    private final String FILE_NAME = "diary_entries.txt";
    private final String password;

    public DiaryManager(String password) throws Exception {
        this.password = password;
        entries = new ArrayList<>();
        loadEntries();
    }

    /**
     * Adds a new entry and automatically saves it to the file.
     * If an entry with the same date exists, it updates it.
     */
    public void addOrUpdateEntry(String date, String content) {
        boolean found = false;
        // Check if an entry for this date already exists
        for (Entry entry : entries) {
            if (entry.getDate().equals(date)) {
                entry.setContent(content);
                found = true;
                break;
            }
        }
        
        // If not found, add a new one
        if (!found) {
            entries.add(new Entry(date, content));
        }

        saveEntries();
    }

    /**
     * Returns the list of all entries.
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * Saves the list of entries to a text file using a simple text format.
     */
    private void saveEntries() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Entry entry : entries) {
                writer.write("===ENTRY===");
                writer.newLine();
                writer.write(entry.getDate());
                writer.newLine();
                
                // Encrypt the content before writing to file
                String encryptedContent = CryptoUtil.encrypt(entry.getContent(), password);
                writer.write(encryptedContent);
                writer.newLine();
                writer.write("===END===");
                writer.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error saving entries: " + e.getMessage());
        }
    }

    /**
     * Loads entries from the text file.
     */
    private void loadEntries() throws Exception {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return; // No file yet, which is fine on first run
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentDate = null;
            StringBuilder currentContent = new StringBuilder();
            boolean readingContent = false;

            while ((line = reader.readLine()) != null) {
                if (line.equals("===ENTRY===")) {
                    currentDate = reader.readLine(); // The line after ===ENTRY=== is the date
                    currentContent = new StringBuilder();
                    readingContent = true;
                } else if (line.equals("===END===")) {
                    if (currentDate != null) {
                        // Remove the trailing newline character from the encrypted content string
                        String content = currentContent.toString();
                        if (content.endsWith("\n")) {
                            content = content.substring(0, content.length() - 1);
                        }
                        
                        // Decrypt the content back to plain text
                        String plainTextContent = CryptoUtil.decrypt(content, password);
                        
                        entries.add(new Entry(currentDate, plainTextContent));
                    }
                    readingContent = false;
                } else if (readingContent) {
                    currentContent.append(line).append("\n");
                }
            }
        }
    }
}
