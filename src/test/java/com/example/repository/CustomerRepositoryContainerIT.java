package com.example.repository;

import com.example.model.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
public class CustomerRepositoryContainerIT {

    @Container
    static MongoDBContainer mongoDBContainer =
            new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    @DisplayName("[Repo Test] Should find customers by email")
    void testFindByEmail() {
        Customer c1 = new Customer("Alice Smith", "alice@example.com", "123 Main St");
        Customer c2 = new Customer("Bob Jones", "bob@example.com", "456 Oak Ave");
        Customer c3 = new Customer("Charlie Brown", "charlie@example.com", "789 Pine Rd");

        customerRepository.saveAll(List.of(c1, c2, c3));

        List<Customer> found = customerRepository.findByEmail("alice@example.com");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Alice Smith");
    }
}

