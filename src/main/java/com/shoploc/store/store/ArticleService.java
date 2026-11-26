package com.shoploc.store.store;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ArticleService {
    private final StoreRepository stores;
    private final ArticleRepository articles;
    private final StockRepository stocks;

    public ArticleService(StoreRepository stores, ArticleRepository articles, StockRepository stocks) {
        this.stores = stores;
        this.articles = articles;
        this.stocks = stocks;
    }

    @Transactional
    public ArticleResponse create(UUID storeId, UUID ownerId, CreateDetails details) {
        requireOwner(storeId, ownerId);
        Article article = new Article(storeId, details.name(), details.priceCents(), details.type());
        article.setDescription(details.description());
        article.setPublished(details.published());
        article = articles.saveAndFlush(article);
        Stock stock = null;
        if (details.type() == ArticleType.PRODUCT) {
            stock = new Stock(article.getId());
            stock.setAvailableQuantity(details.initialStock());
            stock = stocks.saveAndFlush(stock);
        }
        return response(article, stock);
    }

    @Transactional
    public ArticleResponse update(UUID storeId, UUID articleId, UUID ownerId, UpdateDetails details) {
        requireOwner(storeId, ownerId);
        Article article = articles.findByIdAndStoreId(articleId, storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Article not found"));
        if (!article.getVersion().equals(details.version())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Stale article version");
        }
        article.setName(details.name());
        article.setDescription(details.description());
        article.setPriceCents(details.priceCents());
        article.setPublished(details.published());
        article = articles.saveAndFlush(article);
        return response(article, stockFor(article));
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> list(UUID storeId, UUID ownerId, Pageable pageable) {
        requireOwner(storeId, ownerId);
        Page<Article> page = articles.findByStoreId(storeId, pageable);
        var productIds = page.getContent().stream().filter(a -> a.getType() == ArticleType.PRODUCT)
                .map(Article::getId).toList();
        Map<UUID, Stock> stockByArticle = stocks.findAllById(productIds).stream()
                .collect(Collectors.toMap(Stock::getArticleId, Function.identity()));
        return page.map(article -> response(article, stockByArticle.get(article.getId())));
    }

    private void requireOwner(UUID storeId, UUID ownerId) {
        Store store = stores.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!store.getOwnerId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not the store owner");
        }
    }

    private Stock stockFor(Article article) {
        return article.getType() == ArticleType.PRODUCT ? stocks.findById(article.getId()).orElse(null) : null;
    }

    private ArticleResponse response(Article article, Stock stock) {
        if (article.getType() == ArticleType.PRODUCT && stock == null) {
            throw new IllegalStateException("Product has no stock: " + article.getId());
        }
        return new ArticleResponse(article.getId(), article.getStoreId(), article.getName(),
                article.getDescription(), article.getPriceCents(), article.getType(), article.getPublished(),
                article.getVersion(), stock == null ? null : stock.getAvailableQuantity(),
                stock == null ? null : stock.getReservedQuantity());
    }

    public record CreateDetails(String name, String description, long priceCents, ArticleType type,
                                boolean published, int initialStock) {
    }

    public record UpdateDetails(String name, String description, long priceCents, boolean published, int version) {
    }

    public record ArticleResponse(UUID id, UUID storeId, String name, String description, long priceCents,
                                  ArticleType type, boolean published, int version,
                                  Integer availableQuantity, Integer reservedQuantity) {
    }
}
