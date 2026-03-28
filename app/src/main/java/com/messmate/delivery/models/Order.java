package com.messmate.delivery.models;

public class Order {
    private String _id;
    private String mess_name;
    private double total_price;
    private double deliveryFee;
    private LocationData pickupLocation;
    private LocationData dropLocation;
    private String deliveryStatus;

    public String getId() { return _id; }
    public String getMessName() { return mess_name; }
    public double getTotalPrice() { return total_price; }
    public double getDeliveryFee() { return deliveryFee; }
    public LocationData getPickupLocation() { return pickupLocation; }
    public LocationData getDropLocation() { return dropLocation; }
    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String status) { this.deliveryStatus = status; }

    public static class LocationData {
        private double lat;
        private double lng;
        private String address;

        public double getLat() { return lat; }
        public double getLng() { return lng; }
        public String getAddress() { return address; }
    }
}
