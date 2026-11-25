package com.shoploc.store.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import com.shoploc.store.exception.StoreNotFoundException;
import com.shoploc.store.store.Store;
import com.shoploc.store.store.StoreRepository;
import com.shoploc.store.store.StoreStatus;

/** Unit test: no Spring context and no database (the repository is a fake made with Mockito). */
class StoreOwnershipCheckerTest {

    private final StoreRepository storeRepository = mock(StoreRepository.class);
    private final StoreOwnershipChecker checker = new StoreOwnershipChecker(storeRepository);

    private Jwt tokenOf(UUID user) {
        return Jwt.withTokenValue("not-a-real-token").header("alg", "HS256").subject(user.toString()).build();
    }

    @Test
    void unknownStoreGivesNotFound() {
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checker.requireOwnedStore(storeId, tokenOf(UUID.randomUUID())))
                .isInstanceOf(StoreNotFoundException.class);
    }

    @Test
    void storeOfAnotherOwnerGivesAccessDenied() {
        UUID storeId = UUID.randomUUID();
        Store store = new Store(UUID.randomUUID(), "Shop", "1 rue de Lille", StoreStatus.ACTIVE, 30);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        assertThatThrownBy(() -> checker.requireOwnedStore(storeId, tokenOf(UUID.randomUUID())))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void ownerGetsHisStore() {
        UUID storeId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Store store = new Store(owner, "Shop", "1 rue de Lille", StoreStatus.ACTIVE, 30);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        assertThat(checker.requireOwnedStore(storeId, tokenOf(owner))).isSameAs(store);
    }
}