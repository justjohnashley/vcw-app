package com.pural_ba3a.vulcanwash;

public class Order {
    private String service;
    private String status;
    private String time;
    private String shopName;
    private String orderId;
    private boolean accepted; // Indicates if the order is accepted
    private boolean rejected; // Indicates if the order is rejected
    private boolean archived; // Indicates if the order is archived

    // Empty constructor for Firestore
    public Order() {}

    public Order(String service, String status, String time, String shopName, String orderId, boolean accepted, boolean rejected, boolean archived) {
        this.service = service;
        this.status = status;
        this.time = time;
        this.shopName = shopName;
        this.orderId = orderId;
        this.accepted = accepted;
        this.rejected = rejected;
        this.archived = archived;
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

    public String getOrderId() {
        return orderId;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public boolean isRejected() {
        return rejected;
    }

    public boolean isArchived() {
        return archived;
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

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }


    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
