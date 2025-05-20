package com.commerce.service;

import com.commerce.model.Payment;
import com.commerce.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(String id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> getPaymentsByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public Payment savePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    public void deletePayment(String id) {
        paymentRepository.deleteById(id);
    }
}

