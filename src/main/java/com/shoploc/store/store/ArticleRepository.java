package com.shoploc.store.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
    Page<Article> findByStoreId(UUID storeId, Pageable pageable);
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Article> findByIdAndStoreId(UUID id, UUID storeId);
}
