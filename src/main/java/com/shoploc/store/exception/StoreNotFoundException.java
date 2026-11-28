package com.shoploc.store.exception;

import java.util.UUID;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(UUID storeId) {
        super("Store not found: " + storeId);
    }
}
