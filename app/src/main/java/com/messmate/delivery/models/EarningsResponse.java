package com.messmate.delivery.models;

import java.util.List;

public class EarningsResponse {

    private boolean success;
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public Data getData() {
        return data;
    }

    // 🔥 Inner Data Class
    public static class Data {

        private double total;
        private double today;
        private List<Double> weekly;
        private List<Double> monthly;

        public double getTotal() {
            return total;
        }

        public double getToday() {
            return today;
        }

        public List<Double> getWeekly() {
            return weekly;
        }

        public List<Double> getMonthly() {
            return monthly;
        }
    }
}