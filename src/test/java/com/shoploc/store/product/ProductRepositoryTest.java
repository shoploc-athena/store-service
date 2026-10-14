package com.shoploc.store.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void savedProductCanBeFound() {
        repository.save(new Product("Pain", "Whole wheat bread", 1.0));

        assertEquals(1, repository.findAll().size());
    }
}
