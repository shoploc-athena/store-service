package com.shoploc.store.product;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository repository;

    public DataInitializer(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            repository.save(new Product("Baguette", "Traditional French baguette, baked every morning", 1.10));
            repository.save(new Product("Fromage de chèvre", "Fresh local goat cheese", 6.50));
        }
    }
}
