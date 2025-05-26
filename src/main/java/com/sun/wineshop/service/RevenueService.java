package com.sun.wineshop.service;

import com.sun.wineshop.dto.response.MonthlyRevenueResponse;

import java.util.List;

public interface RevenueService {
    List<MonthlyRevenueResponse> getMonthlyRevenue();
}
