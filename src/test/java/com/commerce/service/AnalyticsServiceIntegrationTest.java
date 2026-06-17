package com.commerce.service;

import com.commerce.analytics.dto.BestSellingProductDto;
import com.commerce.analytics.dto.OrderSummaryDto;
import com.commerce.analytics.dto.RevenueOverTimeDto;
import com.commerce.analytics.dto.RevenueByCategoryDto;
import com.commerce.analytics.service.AnalyticsService;
import com.commerce.order.model.*;
import com.commerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@Testcontainers
class AnalyticsServiceIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
    }

    @Autowired MongoTemplate mongoTemplate;
    @Autowired OrderRepository orderRepository;

    AnalyticsService analyticsService;

    private final Instant now = Instant.now();
    private final Instant from = now.minus(30, ChronoUnit.DAYS);
    private final Instant to = now.plus(1, ChronoUnit.DAYS);

    @BeforeEach
    void setUp() {
        analyticsService = new AnalyticsService(mongoTemplate);
        orderRepository.deleteAll();
        seedOrders();
    }

    private void seedOrders() {
        orderRepository.saveAll(List.of(
                Order.builder().customerId("customer-1")
                        .items(List.of(
                                LineItem.builder().productId("p1").productName("Creatine").unitPrice(24.99)
                                        .quantity(3).lineSubtotal(74.97).categories(List.of("supplements", "creatine")).build(),
                                LineItem.builder().productId("p2").productName("Whey Protein").unitPrice(49.99)
                                        .quantity(2).lineSubtotal(99.98).categories(List.of("supplements", "protein")).build()))
                        .subtotal(174.95).shippingCost(5.99).tax(13.996).total(194.936)
                        .currency("USD").status(OrderStatus.PAID).paymentStatus(PaymentStatus.PAID)
                        .createdAt(now.minus(5, ChronoUnit.DAYS)).updatedAt(now.minus(5, ChronoUnit.DAYS)).build(),

                Order.builder().customerId("customer-2")
                        .items(List.of(
                                LineItem.builder().productId("p1").productName("Creatine").unitPrice(24.99)
                                        .quantity(1).lineSubtotal(24.99).categories(List.of("supplements", "creatine")).build()))
                        .subtotal(24.99).shippingCost(5.99).tax(2.0).total(32.98)
                        .currency("USD").status(OrderStatus.DELIVERED).paymentStatus(PaymentStatus.PAID)
                        .createdAt(now.minus(2, ChronoUnit.DAYS)).updatedAt(now.minus(2, ChronoUnit.DAYS)).build(),

                Order.builder().customerId("customer-1")
                        .items(List.of(
                                LineItem.builder().productId("p2").productName("Whey Protein").unitPrice(49.99)
                                        .quantity(10).lineSubtotal(499.9).categories(List.of("supplements", "protein")).build()))
                        .subtotal(499.9).shippingCost(5.99).tax(39.99).total(545.88)
                        .currency("USD").status(OrderStatus.CANCELLED).paymentStatus(PaymentStatus.REFUNDED)
                        .createdAt(now.minus(1, ChronoUnit.DAYS)).updatedAt(now.minus(1, ChronoUnit.DAYS)).build()
        ));
    }

    @Test
    void bestSelling_ranksProductsByQuantity_excludesCancelled() {
        List<BestSellingProductDto> result = analyticsService.getBestSelling(from, to, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductId()).isEqualTo("p1");
        assertThat(result.get(0).getTotalQuantity()).isEqualTo(4);
        assertThat(result.get(1).getProductId()).isEqualTo("p2");
        assertThat(result.get(1).getTotalQuantity()).isEqualTo(2);
    }

    @Test
    void orderSummary_computesCorrectAggregates() {
        OrderSummaryDto summary = analyticsService.getOrderSummary(from, to);

        assertThat(summary.getTotalOrders()).isEqualTo(2);
        assertThat(summary.getTotalRevenue()).isEqualTo(194.936 + 32.98, within(0.01));
        assertThat(summary.getAverageOrderValue()).isEqualTo((194.936 + 32.98) / 2, within(0.01));
    }

    @Test
    void orderSummary_emptyRange_returnsZeros() {
        Instant past = now.minus(365, ChronoUnit.DAYS);
        OrderSummaryDto summary = analyticsService.getOrderSummary(past, past.plus(1, ChronoUnit.DAYS));
        assertThat(summary.getTotalOrders()).isZero();
    }

    @Test
    void revenueByCategory_excludesCancelled() {
        List<RevenueByCategoryDto> result = analyticsService.getRevenueByCategory(from, to);
        assertThat(result).extracting(RevenueByCategoryDto::getCategory)
                .contains("supplements", "creatine", "protein");
        RevenueByCategoryDto supplements = result.stream()
                .filter(r -> "supplements".equals(r.getCategory())).findFirst().orElseThrow();
        assertThat(supplements.getRevenue()).isEqualTo(199.94, within(0.01));
    }

    @Test
    void revenueOverTime_groupsByDay() {
        List<RevenueOverTimeDto> result = analyticsService.getRevenueOverTime(from, to, "DAY");
        assertThat(result).hasSize(2);
        assertThat(result).isSortedAccordingTo(java.util.Comparator.comparing(RevenueOverTimeDto::getPeriod));
    }
}
