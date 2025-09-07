package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.model.Customer;

import java.util.List;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    List<Customer> findByEmail(String email);
}

