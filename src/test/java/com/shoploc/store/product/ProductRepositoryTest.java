package com.shoploc.store.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shoploc.store.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void savedProductCanBeFoundWithItsValues() {
        Product saved = repository.save(new Product("Pain", "Whole wheat bread", 1.0));

        Product found = repository.findById(saved.getId()).orElseThrow();

        assertEquals("Pain", found.getName());
        assertEquals("Whole wheat bread", found.getDescription());
        assertEquals(1.0, found.getPrice(), 0.001);
    }
}