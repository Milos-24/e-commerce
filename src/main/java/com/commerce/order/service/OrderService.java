package com.commerce.order.service;

import com.commerce.inventory.service.InventoryService;
import com.commerce.order.dto.*;
import com.commerce.order.model.*;
import com.commerce.order.repository.OrderRepository;
import com.commerce.product.model.Product;
import com.commerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final InventoryService inventoryService;

    private static final double SHIPPING_COST = 5.99;
    private static final double TAX_RATE = 0.08;

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public OrderResponse placeOrder(PlaceOrderRequest request) {
        List<LineItem> lineItems = new ArrayList<>();
        List<DecrementRecord> decremented = new ArrayList<>();

        try {
            for (OrderItemRequest itemReq : request.getItems()) {
                Product product = productService.getProductForOrder(itemReq.getProductId());

                boolean success = inventoryService.decrementStock(itemReq.getProductId(), itemReq.getQuantity());
                if (!success) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Insufficient stock for: " + product.getName());
                }
                decremented.add(new DecrementRecord(itemReq.getProductId(), itemReq.getQuantity()));

                double unitPrice = product.getPrice() * (1.0 - product.getDiscount() / 100.0);
                lineItems.add(LineItem.builder()
                        .productId(product.getId())
                        .productName(product.getName())
                        .unitPrice(unitPrice)
                        .brand(product.getBrand())
                        .categories(product.getCategories())
                        .attributes(product.getAttributes())
                        .quantity(itemReq.getQuantity())
                        .lineSubtotal(unitPrice * itemReq.getQuantity())
                        .build());
            }
        } catch (Exception e) {
            decremented.forEach(r -> inventoryService.incrementStock(r.productId(), r.quantity()));
            throw e;
        }

        double subtotal = lineItems.stream().mapToDouble(LineItem::getLineSubtotal).sum();
        double tax = subtotal * TAX_RATE;

        return toResponse(orderRepository.save(Order.builder()
                .customerId(request.getCustomerId())
                .items(lineItems)
                .subtotal(subtotal)
                .shippingCost(SHIPPING_COST)
                .tax(tax)
                .total(subtotal + SHIPPING_COST + tax)
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .shippingAddress(request.getShippingAddress())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build()));
    }

    public OrderResponse getOrderById(String id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
    }

    public Page<OrderResponse> getCustomerOrders(String customerId, int page, int size) {
        return orderRepository.findByCustomerId(customerId, PageRequest.of(page, size)).map(this::toResponse);
    }

    public OrderResponse updateStatus(String id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .items(order.getItems().stream().map(li -> LineItemResponse.builder()
                        .productId(li.getProductId()).productName(li.getProductName())
                        .unitPrice(li.getUnitPrice()).brand(li.getBrand())
                        .categories(li.getCategories()).attributes(li.getAttributes())
                        .quantity(li.getQuantity()).lineSubtotal(li.getLineSubtotal())
                        .build()).toList())
                .subtotal(order.getSubtotal()).shippingCost(order.getShippingCost())
                .tax(order.getTax()).total(order.getTotal()).currency(order.getCurrency())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus()).paymentStatus(order.getPaymentStatus())
                .createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt())
                .build();
    }

    private record DecrementRecord(String productId, int quantity) {}
}
