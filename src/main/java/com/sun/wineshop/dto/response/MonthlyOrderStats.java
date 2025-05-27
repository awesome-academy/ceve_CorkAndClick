package com.sun.wineshop.dto.response;

public record MonthlyOrderStats(
        int year,
        int month,
        long totalOrders,
        long cancelledOrders,
        long rejectedOrders,
        long completedOrders,
        double totalRevenue
) {}
