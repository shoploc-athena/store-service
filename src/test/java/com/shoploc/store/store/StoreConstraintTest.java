package com.shoploc.store.store;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import com.shoploc.store.TestcontainersConfiguration;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StoreConstraintTest {

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void storeLatitudeOutOfBoundsIsRejected() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        store.setLatitude(new BigDecimal("91.0")); // Invalid: > 90
        store.setLongitude(new BigDecimal("0.0"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            storeRepository.saveAndFlush(store);
        });
    }

    @Test
    void storeLongitudeOutOfBoundsIsRejected() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        store.setLatitude(new BigDecimal("0.0"));
        store.setLongitude(new BigDecimal("-181.0")); // Invalid: < -180

        assertThrows(DataIntegrityViolationException.class, () -> {
            storeRepository.saveAndFlush(store);
        });
    }

    @Test
    void storeIncompleteCoordinatesAreRejected() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        store.setLatitude(new BigDecimal("45.0"));
        // longitude is null

        assertThrows(DataIntegrityViolationException.class, () -> {
            storeRepository.saveAndFlush(store);
        });
    }

    @Test
    void storeValidCoordinatesAreSaved() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        store.setLatitude(new BigDecimal("45.5"));
        store.setLongitude(new BigDecimal("2.3"));

        Store saved = storeRepository.saveAndFlush(store);
        assertNotNull(saved.getId());
    }

    @Test
    void storeNullCoordinatesAreSaved() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        // Both latitude and longitude remain null

        Store saved = storeRepository.saveAndFlush(store);
        assertNotNull(saved.getId());
        assertNull(saved.getLatitude());
        assertNull(saved.getLongitude());
    }
}
