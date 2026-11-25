package com.shoploc.store.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.shoploc.store.config.SecurityConfig;
import com.shoploc.store.product.ProductController;
import com.shoploc.store.product.ProductRepository;
import com.shoploc.store.store.Store;
import com.shoploc.store.store.StoreRepository;
import com.shoploc.store.store.StoreStatus;

/**
 * Tests the security rules on the HTTP layer, without a database
 * (the repositories are fakes made with Mockito).
 */
@WebMvcTest(controllers = {ProductController.class, SecurityProbeController.class})
@Import({SecurityConfig.class, ProblemJsonHandlers.class, StoreOwnershipChecker.class})
class SecurityRulesTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private StoreRepository storeRepository;

    private final UUID user = UUID.randomUUID();

    private String bearer(String token) {
        return "Bearer " + token;
    }

    // ---- public routes ----

    @Test
    void productsArePublic() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isOk());
    }

    @Test
    void storeProfileAndOpeningHoursArePublic() throws Exception {
        // No controller exists yet for these routes: 404 (not 401) proves that security lets them through.
        UUID storeId = UUID.randomUUID();
        mockMvc.perform(get("/api/stores/" + storeId)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/stores/" + storeId + "/opening-hours")).andExpect(status().isNotFound());
    }

    // ---- 401: no valid token ----

    @Test
    void noTokenGives401WithProblemJson() throws Exception {
        mockMvc.perform(get("/api/private-probe"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void expiredTokenGives401() throws Exception {
        String token = TestTokens.token("lille", TestTokens.SECRET, user, "MERCHANT",
                Instant.now().minus(1, ChronoUnit.HOURS));

        mockMvc.perform(get("/api/private-probe").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void modifiedTokenGives401() throws Exception {
        String token = TestTokens.valid(user, "MERCHANT");
        int lastDot = token.lastIndexOf('.');
        char first = token.charAt(lastDot + 1);
        String modified = token.substring(0, lastDot + 1) + (first == 'A' ? 'B' : 'A') + token.substring(lastDot + 2);

        mockMvc.perform(get("/api/private-probe").header("Authorization", bearer(modified)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenFromAnotherCityGives401() throws Exception {
        String token = TestTokens.token("paris", TestTokens.SECRET, user, "MERCHANT",
                Instant.now().plus(10, ChronoUnit.MINUTES));

        mockMvc.perform(get("/api/private-probe").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    // ---- 200: valid token ----

    @Test
    void validTokenOpensAPrivateRoute() throws Exception {
        mockMvc.perform(get("/api/private-probe").header("Authorization", bearer(TestTokens.valid(user, "CUSTOMER"))))
                .andExpect(status().isOk());
    }

    // ---- 403, 404, 200: merchant routes ----

    @Test
    void customerOnAMerchantRouteGives403() throws Exception {
        mockMvc.perform(get("/api/stores/" + UUID.randomUUID() + "/probe")
                        .header("Authorization", bearer(TestTokens.valid(user, "CUSTOMER"))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void merchantOnAStoreOfSomeoneElseGives403() throws Exception {
        UUID storeId = UUID.randomUUID();
        Store store = new Store(UUID.randomUUID(), "Shop", "1 rue de Lille", StoreStatus.ACTIVE, 30);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        mockMvc.perform(get("/api/stores/" + storeId + "/probe")
                        .header("Authorization", bearer(TestTokens.valid(user, "MERCHANT"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void merchantOnAnUnknownStoreGives404() throws Exception {
        UUID storeId = UUID.randomUUID();
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/stores/" + storeId + "/probe")
                        .header("Authorization", bearer(TestTokens.valid(user, "MERCHANT"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("STORE_NOT_FOUND"));
    }

    @Test
    void merchantOnHisOwnStoreGets200() throws Exception {
        UUID storeId = UUID.randomUUID();
        Store store = new Store(user, "Shop", "1 rue de Lille", StoreStatus.ACTIVE, 30);
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        mockMvc.perform(get("/api/stores/" + storeId + "/probe")
                        .header("Authorization", bearer(TestTokens.valid(user, "MERCHANT"))))
                .andExpect(status().isOk());
    }
}
