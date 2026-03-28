package com.messmate.delivery.models;

public class OrderStatusRequest {
    private String orderId;
    private String status;

    public OrderStatusRequest(String orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
}
