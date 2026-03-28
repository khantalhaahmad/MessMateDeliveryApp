package com.messmate.delivery.models;

import java.util.List;

public class AvailableOrdersResponse {
    private boolean success;
    private List<Order> data;

    public boolean isSuccess() { return success; }
    public List<Order> getData() { return data; }
}
