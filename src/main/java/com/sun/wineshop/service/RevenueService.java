package com.sun.wineshop.service;

import com.sun.wineshop.dto.response.MonthlyOrderStats;

import java.util.List;

public interface RevenueService {
    List<MonthlyOrderStats> getMonthlyOrderStatistics();
}
