package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.example.model.Customer;
import com.example.repository.CustomerRepository;

@Testcontainers
@SpringBootTest
class CustomerServiceContainerIT {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceContainerIT.class);

    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        Cache cache = cacheManager.getCache(CustomerService.CACHE_NAME);
        if (cache != null) cache.clear();
    }

    @Test
    @DisplayName("[Service Test] Should create and retrieve customer by email")
    void createAndFindCustomerByEmail() {
        Customer customer = new Customer("Alice Smith", "alice@example.com", "123 Main St");
        customerService.createCustomer(customer);
        List<Customer> found = customerService.getCustomersByEmail("alice@example.com");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Alice Smith");
    }

    @Test
    @DisplayName("[Service Test] Should update customer details")
    void updateCustomerTest() {
        Customer customer = new Customer("Bob Jones", "bob@example.com", "456 Oak Ave");
        Customer saved = customerService.createCustomer(customer);
        Customer updatedDetails = new Customer("Bob Jones Jr.", "bob.jr@example.com", "456 Oak Ave, Apt 2");
        Optional<Customer> updated = customerService.updateCustomer(saved.getId(), updatedDetails);
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Bob Jones Jr.");
        assertThat(updated.get().getEmail()).isEqualTo("bob.jr@example.com");
    }

    @Test
    @DisplayName("[Service Test] Should delete customer")
    void deleteCustomerTest() {
        Customer customer = new Customer("Charlie Brown", "charlie@example.com", "789 Pine Rd");
        Customer saved = customerService.createCustomer(customer);
        boolean deleted = customerService.deleteCustomer(saved.getId());
        assertThat(deleted).isTrue();
        Optional<Customer> found = customerService.getCustomerById(saved.getId());
        assertThat(found).isNotPresent();
    }
}
