package com.commerce.analytics.controller;

import com.commerce.analytics.dto.*;
import com.commerce.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/best-selling")
    @CrossOrigin
    public List<BestSellingProductDto> bestSelling(@RequestParam String from, @RequestParam String to,
                                                    @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getBestSelling(parseDate(from), parseDateEndOfDay(to), limit);
    }

    @GetMapping("/revenue-over-time")
    @CrossOrigin
    public List<RevenueOverTimeDto> revenueOverTime(@RequestParam String from, @RequestParam String to,
                                                     @RequestParam(defaultValue = "DAY") String granularity) {
        return analyticsService.getRevenueOverTime(parseDate(from), parseDateEndOfDay(to), granularity);
    }

    @GetMapping("/revenue-by-category")
    @CrossOrigin
    public List<RevenueByCategoryDto> revenueByCategory(@RequestParam String from, @RequestParam String to) {
        return analyticsService.getRevenueByCategory(parseDate(from), parseDateEndOfDay(to));
    }

    @GetMapping("/order-summary")
    @CrossOrigin
    public OrderSummaryDto orderSummary(@RequestParam String from, @RequestParam String to) {
        return analyticsService.getOrderSummary(parseDate(from), parseDateEndOfDay(to));
    }

    @GetMapping("/top-customers")
    @CrossOrigin
    public List<TopCustomerDto> topCustomers(@RequestParam String from, @RequestParam String to,
                                              @RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getTopCustomers(parseDate(from), parseDateEndOfDay(to), limit);
    }

    private Instant parseDate(String date) {
        return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant parseDateEndOfDay(String date) {
        return LocalDate.parse(date).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
