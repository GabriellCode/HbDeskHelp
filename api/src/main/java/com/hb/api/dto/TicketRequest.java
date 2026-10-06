package com.hb.api.dto;

public class TicketRequest {
    private String title;
    private String description;

    public TicketRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
