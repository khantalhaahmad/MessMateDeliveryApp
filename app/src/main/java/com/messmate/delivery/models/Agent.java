package com.messmate.delivery.models;

public class Agent {
    private String _id;
    private String name;
    private boolean isOnline;
    private boolean isAvailable;
    private String currentOrderId;
    private double totalEarnings;

    public String getId() { return _id; }
    public String getName() { return name; }
    public boolean isOnline() { return isOnline; }
    public boolean isAvailable() { return isAvailable; }
    public String getCurrentOrderId() { return currentOrderId; }
    public double getTotalEarnings() { return totalEarnings; }
}
