package com.sun.wineshop.controller;


import com.sun.wineshop.dto.response.MonthlyRevenueResponse;
import com.sun.wineshop.service.RevenueService;
import com.sun.wineshop.utils.api.RevenueApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(RevenueApiPaths.BASE)
@RequiredArgsConstructor
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping(RevenueApiPaths.Endpoint.MONTHLY)
    public List<MonthlyRevenueResponse> getMonthlyRevenue() {
        return revenueService.getMonthlyRevenue();
    }
}
