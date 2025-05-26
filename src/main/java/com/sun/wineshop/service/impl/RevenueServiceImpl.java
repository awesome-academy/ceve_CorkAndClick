package com.sun.wineshop.service.impl;

import com.sun.wineshop.dto.response.MonthlyRevenueResponse;
import com.sun.wineshop.repository.OrderRepository;
import com.sun.wineshop.service.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final OrderRepository orderRepository;

    @Override
    public List<MonthlyRevenueResponse> getMonthlyRevenue() {
        return orderRepository.getMonthlyRevenue();
    }
}
