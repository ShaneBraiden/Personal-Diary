import java.io.Serializable;

/**
 * The Entry class represents a single diary entry.
 * It strictly follows OOP principles by encapsulating the date and content properties.
 */
public class Entry implements Serializable {
    private String date;
    private String content;

    /**
     * Constructor to initialize a new Entry.
     *
     * @param date    The date of the entry (e.g., "2023-10-27").
     * @param content The actual diary text.
     */
    public Entry(String date, String content) {
        this.date = date;
        this.content = content;
    }

    // --- Getters and Setters ---

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * The toString method is overridden so that when this object is added
     * to a JList, it will display the date nicely.
     */
    @Override
    public String toString() {
        return date;
    }
}
