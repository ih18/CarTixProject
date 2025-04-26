package com.example.cartix;

import java.io.Serializable;

public class HistoryItem implements Serializable {
    private String query;
    private String response;
    private String timestamp;

    // Default constructor for GSON deserialization
    public HistoryItem() {
        this.query = "";
        this.response = "";
        this.timestamp = "";
    }

    public HistoryItem(String query, String response, String timestamp) {
        this.query = query != null ? query : "";
        this.response = response != null ? response : "";
        this.timestamp = timestamp != null ? timestamp : "";
    }

    public String getQuery() {
        return query != null ? query : "";
    }

    public String getResponse() {
        return response != null ? response : "";
    }

    public String getTimestamp() {
        return timestamp != null ? timestamp : "";
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}