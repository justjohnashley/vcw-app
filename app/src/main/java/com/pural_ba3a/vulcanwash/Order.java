package com.pural_ba3a.vulcanwash;

public class Order {
    private String service;
    private String status;
    private String time;
    private String shopName;
    private boolean accepted; // Indicates if the order is accepted
    private boolean rejected; // Indicates if the order is rejected

    // Empty constructor for Firestore
    public Order() {}

    public Order(String service, String status, String time, String shopName, boolean accepted, boolean rejected) {
        this.service = service;
        this.status = status;
        this.time = time;
        this.shopName = shopName;
        this.accepted = accepted;
        this.rejected = rejected;
    }

    public String getService() {
        return service;
    }

    public String getStatus() {
        return status;
    }

    public String getTime() {
        return time;
    }

    public String getShopName() {
        return shopName;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }
}
