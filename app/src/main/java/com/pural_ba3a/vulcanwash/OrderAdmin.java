package com.pural_ba3a.vulcanwash;

public class OrderAdmin {
    private String service;
    private String status;
    private String time;
    private String username;
    private String contact;
    private boolean accepted;
    private boolean rejected;
    private boolean archived;
    private String orderId;


    // Empty constructor for Firestore
    public OrderAdmin() {}

    public OrderAdmin(String service, String status, String time, String username, String contact, String orderId, boolean accepted, boolean rejected, boolean archived) {
        this.service = service;
        this.status = status;
        this.time = time;
        this.username = username;
        this.orderId = orderId;
        this.contact = contact;
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

    public String getusername() {
        return username;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getContact() {
        return contact;
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

    public void setusername(String username) {
        this.username = username;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setContact(String contact) {
        this.contact = contact;
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
