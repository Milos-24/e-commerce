package com.commerce.customer.service;

import com.commerce.customer.model.Customer;
import com.commerce.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() { return customerRepository.findAll(); }
    public Optional<Customer> getCustomerById(String id) { return customerRepository.findById(id); }
    public Customer saveCustomer(Customer customer) { return customerRepository.save(customer); }
    public void deleteCustomer(String id) { customerRepository.deleteById(id); }
}
