package com.commerce.payment.repository;

import com.commerce.payment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    List<Payment> findByPaymentId(String paymentId);
}
