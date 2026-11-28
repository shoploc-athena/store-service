package com.shoploc.store.security;

import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Test only: two fake routes used to check the security rules (this class is not in src/main). */
@RestController
class SecurityProbeController {

    private final StoreOwnershipChecker storeOwnershipChecker;

    SecurityProbeController(StoreOwnershipChecker storeOwnershipChecker) {
        this.storeOwnershipChecker = storeOwnershipChecker;
    }

    /** A merchant route: needs ROLE_MERCHANT and a store that belongs to the caller. */
    @GetMapping("/api/stores/{storeId}/probe")
    String merchantRoute(@PathVariable UUID storeId, @AuthenticationPrincipal Jwt jwt) {
        storeOwnershipChecker.requireOwnedStore(storeId, jwt);
        return "ok";
    }

    /** A route that only needs a valid token. */
    @GetMapping("/api/private-probe")
    String privateRoute() {
        return "ok";
    }
}
