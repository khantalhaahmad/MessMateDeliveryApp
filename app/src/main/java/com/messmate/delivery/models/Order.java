package com.messmate.delivery.models;

public class Order {

    // ================= BASIC =================
    private String _id;
    private String mess_name;

    private double total_price;
    private double deliveryFee;

    // ================= CUSTOMER =================
    private String customerName;
    private String customerPhone;

    // ================= PAYMENT =================
    private String paymentMethod; // COD / Online

    // ================= ADDRESS =================
    private String pickupAddress;
    private String dropAddress;

    // ================= LOCATION =================
    private LocationData pickupLocation;
    private LocationData dropLocation;

    // ================= DELIVERY =================
    private String deliveryStatus;

    // ================= TIMER =================
    private long expiresAt;

    // ================= GETTERS =================
    public String getId() { return _id; }

    public String getMessName() { return mess_name; }

    public double getTotalPrice() { return total_price; }

    public double getDeliveryFee() { return deliveryFee; }

    public String getCustomerName() { return customerName; }

    public String getCustomerPhone() { return customerPhone; }

    public String getPaymentMethod() { return paymentMethod; }

    public String getPickupAddress() { return pickupAddress; }

    public String getDropAddress() { return dropAddress; }

    public LocationData getPickupLocation() { return pickupLocation; }

    public LocationData getDropLocation() { return dropLocation; }

    public String getDeliveryStatus() { return deliveryStatus; }

    public long getExpiresAt() { return expiresAt; }

    // ================= SETTERS =================
    public void setDeliveryStatus(String status) {
        this.deliveryStatus = status;
    }

    // ================= LOCATION CLASS =================
    public static class LocationData {

        private double lat;
        private double lng;
        private String address;

        public double getLat() { return lat; }

        public double getLng() { return lng; }

        public String getAddress() { return address; }
    }
}