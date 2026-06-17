package com.commerce.analytics.service;

import com.commerce.analytics.dto.*;
import com.commerce.order.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;

    public List<BestSellingProductDto> getBestSelling(Instant from, Instant to, int limit) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(from).lte(to)
                        .and("status").ne(OrderStatus.CANCELLED)),
                Aggregation.unwind("items"),
                Aggregation.group("items.productId")
                        .first("items.productName").as("productName")
                        .sum("items.quantity").as("totalQuantity")
                        .sum("items.lineSubtotal").as("totalRevenue"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalQuantity")),
                Aggregation.limit(limit)
        );
        return mongoTemplate.aggregate(agg, "orders", Document.class).getMappedResults().stream()
                .map(doc -> BestSellingProductDto.builder()
                        .productId(doc.getString("_id")).productName(doc.getString("productName"))
                        .totalQuantity(toLong(doc.get("totalQuantity"))).totalRevenue(toDouble(doc.get("totalRevenue")))
                        .build()).toList();
    }

    public List<RevenueOverTimeDto> getRevenueOverTime(Instant from, Instant to, String granularity) {
        String format = "MONTH".equalsIgnoreCase(granularity) ? "%Y-%m" : "%Y-%m-%d";
        AggregationOperation projectPeriod = ctx -> new Document("$project",
                new Document("period", new Document("$dateToString",
                        new Document("format", format).append("date", "$createdAt"))).append("total", 1));
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(from).lte(to)
                        .and("status").ne(OrderStatus.CANCELLED)),
                projectPeriod,
                Aggregation.group("period").sum("total").as("revenue").count().as("orderCount"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id"))
        );
        return mongoTemplate.aggregate(agg, "orders", Document.class).getMappedResults().stream()
                .map(doc -> RevenueOverTimeDto.builder()
                        .period(doc.getString("_id")).revenue(toDouble(doc.get("revenue")))
                        .orderCount(toLong(doc.get("orderCount"))).build()).toList();
    }

    public List<RevenueByCategoryDto> getRevenueByCategory(Instant from, Instant to) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(from).lte(to)
                        .and("status").ne(OrderStatus.CANCELLED)),
                Aggregation.unwind("items"),
                Aggregation.unwind("items.categories"),
                Aggregation.group("items.categories")
                        .sum("items.lineSubtotal").as("revenue").sum("items.quantity").as("totalQuantity"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "revenue"))
        );
        return mongoTemplate.aggregate(agg, "orders", Document.class).getMappedResults().stream()
                .map(doc -> RevenueByCategoryDto.builder()
                        .category(doc.getString("_id")).revenue(toDouble(doc.get("revenue")))
                        .totalQuantity(toLong(doc.get("totalQuantity"))).build()).toList();
    }

    public OrderSummaryDto getOrderSummary(Instant from, Instant to) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(from).lte(to)
                        .and("status").ne(OrderStatus.CANCELLED)),
                Aggregation.group().count().as("totalOrders").sum("total").as("totalRevenue").avg("total").as("averageOrderValue")
        );
        List<Document> results = mongoTemplate.aggregate(agg, "orders", Document.class).getMappedResults();
        if (results.isEmpty()) return OrderSummaryDto.builder().build();
        Document doc = results.get(0);
        return OrderSummaryDto.builder()
                .totalOrders(toLong(doc.get("totalOrders"))).totalRevenue(toDouble(doc.get("totalRevenue")))
                .averageOrderValue(toDouble(doc.get("averageOrderValue"))).build();
    }

    public List<TopCustomerDto> getTopCustomers(Instant from, Instant to, int limit) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(from).lte(to)
                        .and("status").ne(OrderStatus.CANCELLED)),
                Aggregation.group("customerId").sum("total").as("totalSpend").count().as("orderCount"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalSpend")),
                Aggregation.limit(limit)
        );
        return mongoTemplate.aggregate(agg, "orders", Document.class).getMappedResults().stream()
                .map(doc -> TopCustomerDto.builder()
                        .customerId(doc.getString("_id")).totalSpend(toDouble(doc.get("totalSpend")))
                        .orderCount(toLong(doc.get("orderCount"))).build()).toList();
    }

    private double toDouble(Object v) { return v instanceof Number n ? n.doubleValue() : 0.0; }
    private long toLong(Object v) { return v instanceof Number n ? n.longValue() : 0L; }
}
