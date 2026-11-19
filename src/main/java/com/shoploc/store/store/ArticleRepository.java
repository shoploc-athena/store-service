package com.shoploc.store.store;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID> {
}
