package com.commerce.service;

import com.commerce.inventory.model.Inventory;
import com.commerce.inventory.repository.InventoryRepository;
import com.commerce.inventory.service.InventoryService;
import com.commerce.order.dto.OrderItemRequest;
import com.commerce.order.dto.OrderResponse;
import com.commerce.order.dto.PlaceOrderRequest;
import com.commerce.order.model.OrderStatus;
import com.commerce.order.model.ShippingAddress;
import com.commerce.order.repository.OrderRepository;
import com.commerce.order.service.OrderService;
import com.commerce.product.model.Product;
import com.commerce.product.repository.ProductRepository;
import com.commerce.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataMongoTest
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
    }

    @Autowired MongoTemplate mongoTemplate;
    @Autowired ProductRepository productRepository;
    @Autowired InventoryRepository inventoryRepository;
    @Autowired OrderRepository orderRepository;

    OrderService orderService;

    private static final ShippingAddress ADDRESS = ShippingAddress.builder()
            .fullName("John Doe").street("123 Main St").city("Belgrade")
            .state("Serbia").zip("11000").country("RS").build();

    @BeforeEach
    void setUp() {
        InventoryService inventoryService = new InventoryService(inventoryRepository, productRepository, mongoTemplate);
        ProductService productService = new ProductService(productRepository, null, null, mongoTemplate, null);
        orderService = new OrderService(orderRepository, productService, inventoryService);
        productRepository.deleteAll();
        inventoryRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void placeOrder_success_computesTotalsServerSide() {
        Product product = productRepository.save(Product.builder()
                .name("Creatine").price(24.99).discount(0).brand("Bulk")
                .categories(List.of("supplements")).build());
        inventoryRepository.save(Inventory.builder().productId(product.getId()).stock(10).build());

        OrderResponse resp = orderService.placeOrder(new PlaceOrderRequest(
                "customer-1", List.of(new OrderItemRequest(product.getId(), 2)), ADDRESS, "USD"));

        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getItems().get(0).getLineSubtotal()).isEqualTo(49.98, within(0.001));
        assertThat(resp.getSubtotal()).isEqualTo(49.98, within(0.001));
        assertThat(resp.getShippingCost()).isEqualTo(5.99, within(0.001));
        assertThat(resp.getTax()).isEqualTo(49.98 * 0.08, within(0.001));
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void placeOrder_decrementsStock() {
        Product product = productRepository.save(Product.builder()
                .name("Whey").price(50.0).discount(0).brand("ON").categories(List.of("protein")).build());
        inventoryRepository.save(Inventory.builder().productId(product.getId()).stock(5).build());

        orderService.placeOrder(new PlaceOrderRequest(
                "customer-1", List.of(new OrderItemRequest(product.getId(), 3)), ADDRESS, "USD"));

        Inventory updated = inventoryRepository.findInventoryByProductId(product.getId()).get(0);
        assertThat(updated.getStock()).isEqualTo(2);
    }

    @Test
    void placeOrder_insufficientStock_throws409() {
        Product product = productRepository.save(Product.builder()
                .name("Pre-Workout").price(39.99).discount(0).brand("C4").categories(List.of("supplements")).build());
        inventoryRepository.save(Inventory.builder().productId(product.getId()).stock(1).build());

        assertThatThrownBy(() -> orderService.placeOrder(new PlaceOrderRequest(
                "customer-1", List.of(new OrderItemRequest(product.getId(), 5)), ADDRESS, "USD")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void placeOrder_insufficientStock_rollsBackAlreadyDecrementedItems() {
        Product p1 = productRepository.save(Product.builder()
                .name("BCAA").price(30.0).discount(0).brand("Sci").categories(List.of("amino")).build());
        Product p2 = productRepository.save(Product.builder()
                .name("Creatine").price(25.0).discount(0).brand("Bulk").categories(List.of("creatine")).build());
        inventoryRepository.save(Inventory.builder().productId(p1.getId()).stock(10).build());
        inventoryRepository.save(Inventory.builder().productId(p2.getId()).stock(1).build());

        assertThatThrownBy(() -> orderService.placeOrder(new PlaceOrderRequest("customer-1", List.of(
                new OrderItemRequest(p1.getId(), 2),
                new OrderItemRequest(p2.getId(), 5)), ADDRESS, "USD")))
                .isInstanceOf(ResponseStatusException.class);

        Inventory p1Inv = inventoryRepository.findInventoryByProductId(p1.getId()).get(0);
        assertThat(p1Inv.getStock()).isEqualTo(10);
    }
}
