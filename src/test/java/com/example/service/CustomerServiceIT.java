package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;

/**
 * Integration test for CustomerService using @SpringBootTest (Baseline - Minimal).
 * Requires external MongoDB started via `docker-compose up`.
 */
@Testcontainers
@SpringBootTest
public class CustomerServiceIT {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceIT.class);

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CacheManager cacheManager;

    @Container
    static GenericContainer<?> mongoDBContainer =
            new GenericContainer<>(DockerImageName.parse("mongo:latest")).withExposedPorts(27017);

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        String uri = String.format("mongodb://%s:%d/test",
            mongoDBContainer.getHost(),
            mongoDBContainer.getMappedPort(27017));
        registry.add("spring.data.mongodb.uri", () -> uri);
    }

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        Cache cache = cacheManager.getCache(CustomerService.CACHE_NAME);
        if (cache != null) cache.clear();
    }

    @Test
    @DisplayName("[Service Test] Should create and retrieve customer by id")
    void createAndFindCustomerById() {
        Customer customer = new Customer("Alice Smith", "alice@example.com", "123 Main St");
        Customer saved = customerService.createCustomer(customer);
        Optional<Customer> found = customerService.getCustomerById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice Smith");
    }
}
