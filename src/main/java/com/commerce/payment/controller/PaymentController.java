package com.commerce.payment.controller;

import com.commerce.payment.model.Payment;
import com.commerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    public List<Payment> getAllPayments() { return paymentService.getAllPayments(); }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable String id) {
        return paymentService.getPaymentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{paymentId}")
    public List<Payment> getPaymentsByPaymentId(@PathVariable String paymentId) {
        return paymentService.getPaymentsByPaymentId(paymentId);
    }

    @PostMapping
    public Payment createPayment(@RequestBody Payment payment) { return paymentService.savePayment(payment); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable String id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}
