package com.shoploc.store.product;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.shoploc.store.CityDatabases;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

class ProductCityIsolationTest {

    @Test
    void productSavedInOneCityIsNotVisibleInAnotherCity() {
        CityDatabases.create("lille");
        CityDatabases.create("paris");

        try (ConfigurableApplicationContext lille = CityDatabases.startApplication("lille");
             ConfigurableApplicationContext paris = CityDatabases.startApplication("paris")) {

            ProductRepository lilleRepository = lille.getBean(ProductRepository.class);
            ProductRepository parisRepository = paris.getBean(ProductRepository.class);

            // The application inserts some initial products at startup (DataInitializer),
            // so we compare the counts before and after instead of expecting an empty database.
            long lilleBefore = lilleRepository.count();
            long parisBefore = parisRepository.count();

            lilleRepository.save(new Product("Lille only product", "Saved in Lille", 1.0));

            assertEquals(lilleBefore + 1, lilleRepository.count());
            assertEquals(parisBefore, parisRepository.count());
        }
    }
}