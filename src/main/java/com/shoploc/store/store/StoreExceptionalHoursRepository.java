package com.shoploc.store.store;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface StoreExceptionalHoursRepository extends JpaRepository<StoreExceptionalHours, UUID> {
}
