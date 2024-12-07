package com.pural_ba3a.vulcanwash;

public class Shop {
    private String uid;
    private String shopName;
    private String username;
    private String contact;

    // Constructor
    public Shop(String uid, String shopName, String username, String contact) {
        this.uid = uid;
        this.shopName = shopName;
        this.username = username;
        this.contact = contact;
    }

    public String getUid() {
        return uid;
    }

    public String getShopName() {
        return shopName;
    }

    public String getUsername() {
        return username;
    }

    public String getContact() {
        return contact;
    }
}

