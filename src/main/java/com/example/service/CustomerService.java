package com.example.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    public static final String CACHE_NAME = "customers";

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        log.info("Fetching all customers from database");
        return customerRepository.findAll();
    }

    @Cacheable(value = CACHE_NAME, key = "#id")
    public Optional<Customer> getCustomerById(String id) {
        log.info("Fetching customer with id {} from database", id);
        return customerRepository.findById(id);
    }

    public List<Customer> getCustomersByEmail(String email) {
        log.info("Fetching customers with email {} from database", email);
        return customerRepository.findByEmail(email);
    }

    @CacheEvict(value = CACHE_NAME, allEntries = true)
    public Customer createCustomer(Customer customer) {
        log.info("Creating new customer: {}", customer.getName());
        return customerRepository.save(customer);
    }

    @Caching(
            put = { @CachePut(value = CACHE_NAME, key = "#result.id") },
            evict = { @CacheEvict(value = CACHE_NAME, key = "'all'", beforeInvocation = true) }
    )
    public Optional<Customer> updateCustomer(String id, Customer customerDetails) {
        log.info("Attempting to update customer with id: {}", id);
        return customerRepository.findById(id)
                .map(existingCustomer -> {
                    existingCustomer.setName(customerDetails.getName());
                    existingCustomer.setEmail(customerDetails.getEmail());
                    Customer updatedCustomer = customerRepository.save(existingCustomer);
                    log.info("Successfully updated customer with id: {}", updatedCustomer.getId());
                    return updatedCustomer;
                });
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_NAME, key = "#id"),
            @CacheEvict(value = CACHE_NAME, key = "'all'", beforeInvocation = true)
    })
    public boolean deleteCustomer(String id) {
        log.info("Attempting to delete customer with id: {}", id);
        if (customerRepository.existsById(id)) {
            customerRepository.deleteById(id);
            log.info("Successfully deleted customer with id: {}", id);
            return true;
        } else {
            log.warn("Customer with id {} not found for deletion.", id);
            return false;
        }
    }
}

