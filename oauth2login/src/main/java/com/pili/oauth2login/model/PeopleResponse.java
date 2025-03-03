package com.pili.oauth2login.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PeopleResponse {
    private List<Person> connections;
    private String nextPageToken;
    private int totalItems;

    // Getters and setters
    public List<Person> getConnections() {
        return connections;
    }

    public void setConnections(List<Person> connections) {
        this.connections = connections;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}