package com.shoploc.store.security;

import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import com.shoploc.store.exception.StoreNotFoundException;
import com.shoploc.store.store.Store;
import com.shoploc.store.store.StoreRepository;

/**
 * Reusable check: 404 if the store does not exist, 403 if the caller is not its owner.
 * Usage in a controller: storeOwnershipChecker.requireOwnedStore(storeId, jwt)
 * (jwt comes from a method parameter annotated with @AuthenticationPrincipal).
 */
@Component
public class StoreOwnershipChecker {

    private final StoreRepository storeRepository;

    public StoreOwnershipChecker(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Store requireOwnedStore(UUID storeId, Jwt jwt) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId));

        // "sub" of the token is the id of the user: it must be the owner of the store
        if (!store.getOwnerId().toString().equals(jwt.getSubject())) {
            throw new AccessDeniedException("The caller does not own this store");
        }
        return store;
    }
}
