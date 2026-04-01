package com.diary;

import java.io.Serializable;
import java.util.Date;

/**
 * DiaryModel: Represents a single entry.
 */
public class Entry implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Date date;
    private String content;

    public Entry(Date date, String content) {
        this.date = date;
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        // Formats how it appears in the JList
        return date.toString();
    }
}
