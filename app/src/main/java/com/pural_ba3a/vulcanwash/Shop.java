package com.pural_ba3a.vulcanwash;

public class Shop {
    private String uid;
    private String shopName;

    // Constructor
    public Shop(String uid, String shopName) {
        this.uid = uid;
        this.shopName = shopName;
    }

    public String getUid() {
        return uid;
    }

    public String getShopName() {
        return shopName;
    }
}

