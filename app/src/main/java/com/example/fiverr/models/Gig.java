package com.example.fiverr.models;

public class Gig {
    private String gigId;
    private String title;
    private String description;
    private String category;
    private double price;
    private String postedByUserId;
    private String postedByName;
    private String acceptedByUserId;
    private String acceptedByName;
    private String status; // "open", "accepted", "completed"
    private long createdAt;

    public Gig() {}

    public Gig(String title, String description, String category, double price,
               String postedByUserId, String postedByName) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.postedByUserId = postedByUserId;
        this.postedByName = postedByName;
        this.status = "open";
        this.acceptedByUserId = "";
        this.acceptedByName = "";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getGigId() { return gigId; }
    public void setGigId(String gigId) { this.gigId = gigId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getPostedByUserId() { return postedByUserId; }
    public void setPostedByUserId(String postedByUserId) { this.postedByUserId = postedByUserId; }

    public String getPostedByName() { return postedByName; }
    public void setPostedByName(String postedByName) { this.postedByName = postedByName; }

    public String getAcceptedByUserId() { return acceptedByUserId; }
    public void setAcceptedByUserId(String acceptedByUserId) { this.acceptedByUserId = acceptedByUserId; }

    public String getAcceptedByName() { return acceptedByName; }
    public void setAcceptedByName(String acceptedByName) { this.acceptedByName = acceptedByName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
