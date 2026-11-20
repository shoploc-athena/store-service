package com.shoploc.store.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import com.shoploc.store.TestcontainersConfiguration;

import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StoreOpeningHoursConstraintTest {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreOpeningHoursRepository openingHoursRepository;

    private UUID storeId;

    @BeforeEach
    void setUp() {
        Store store = new Store(UUID.randomUUID(), "Test Store", "123 Main St", StoreStatus.ACTIVE, 30);
        storeId = storeRepository.saveAndFlush(store).getId();
    }

    @Test
    void invalidDayOfWeekIsRejected() {
        StoreOpeningHours hours = new StoreOpeningHours(storeId, 8, LocalTime.of(9, 0), LocalTime.of(17, 0));

        assertThrows(DataIntegrityViolationException.class, () -> {
            openingHoursRepository.saveAndFlush(hours);
        });
    }

    @Test
    void closeTimeBeforeOpenTimeIsRejected() {
        StoreOpeningHours hours = new StoreOpeningHours(storeId, 1, LocalTime.of(17, 0), LocalTime.of(9, 0));

        assertThrows(DataIntegrityViolationException.class, () -> {
            openingHoursRepository.saveAndFlush(hours);
        });
    }

    @Test
    void validOpeningHoursAreSaved() {
        StoreOpeningHours hours = new StoreOpeningHours(storeId, 1, LocalTime.of(9, 0), LocalTime.of(17, 0));

        StoreOpeningHours saved = openingHoursRepository.saveAndFlush(hours);
        assertNotNull(saved.getId());
    }
}
