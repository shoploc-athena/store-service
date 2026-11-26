package com.shoploc.store.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.crypto.spec.SecretKeySpec;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.shoploc.store.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ArticleApiTest {
    private static final String SECRET = "integration-test-key-not-for-production";
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final String PRODUCT = """
            {"name":"Bread","description":"Fresh","priceCents":125,"type":"PRODUCT","initialStock":7}
            """;
    private static final String SERVICE = """
            {"name":"Delivery","priceCents":0,"type":"SERVICE"}
            """;

    @Autowired MockMvc mvc;
    @Autowired StoreRepository stores;
    @Autowired ArticleRepository articles;
    @Autowired StockRepository stocks;
    @MockitoSpyBean StockRepository stockSpy;

    private UUID owner;
    private UUID storeId;
    private String ownerToken;
    private String path;

    @BeforeEach
    void setUp() {
        owner = UUID.randomUUID();
        storeId = stores.saveAndFlush(new Store(owner, "Test store", "Address", StoreStatus.ACTIVE, 30)).getId();
        ownerToken = token(owner, "MERCHANT", "lille", Instant.now().plusSeconds(600), SECRET);
        path = "/api/stores/" + storeId + "/articles";
    }

    @Test
    void createsProductsAndServicesAndListsStockWithPagination() throws Exception {
        JsonNode product = create(PRODUCT);
        JsonNode service = create(SERVICE);
        assertThat(product.path("published").booleanValue()).isFalse();
        assertThat(product.path("availableQuantity").intValue()).isEqualTo(7);
        assertThat(product.path("reservedQuantity").intValue()).isZero();
        assertThat(stocks.findById(UUID.fromString(product.path("id").asString()))).isPresent();
        assertThat(stocks.findById(UUID.fromString(service.path("id").asString()))).isEmpty();
        var result = mvc.perform(get(path).param("page", "0").param("size", "1").param("sort", "name,asc")
                .header("Authorization", bearer(ownerToken))).andExpect(status().isOk()).andReturn();
        JsonNode page = JSON.readTree(result.getResponse().getContentAsString());
        assertThat(page.path("content").size()).isEqualTo(1);
        assertThat(page.path("totalElements").intValue()).isEqualTo(2);
        assertThat(page.path("content").get(0).path("availableQuantity").intValue()).isEqualTo(7);
    }

    @Test
    void productStockDefaultsToZeroAndNamesAreUniquePerStore() throws Exception {
        JsonNode first = create("{\"name\":\"Bread\",\"priceCents\":0,\"type\":\"PRODUCT\"}");
        assertThat(first.path("availableQuantity").intValue()).isZero();
        mvc.perform(post(path).header("Authorization", bearer(ownerToken)).contentType(MediaType.APPLICATION_JSON)
                .content(SERVICE.replace("Delivery", "Bread"))).andExpect(status().isConflict());
        UUID other = stores.saveAndFlush(new Store(owner, "Other", "Address", StoreStatus.ACTIVE, 30)).getId();
        mvc.perform(post("/api/stores/" + other + "/articles").header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCT)).andExpect(status().isCreated());
    }

    @Test
    void validatesPriceStockNameAndType() throws Exception {
        for (String invalid : new String[] {
                PRODUCT.replace("125", "-1"), PRODUCT.replace("125", "1.5"),
                PRODUCT.replace("125", "1.0"), PRODUCT.replace("125", "\"1\""),
                PRODUCT.replace("7", "-1"), PRODUCT.replace("7", "1.5"),
                SERVICE.replace("\"SERVICE\"", "\"SERVICE\",\"initialStock\":0"),
                SERVICE.replace("\"SERVICE\"", "\"SERVICE\",\"initialStock\":null"),
                PRODUCT.replace("Bread", ""), PRODUCT.replace("Bread", "x".repeat(151)),
                PRODUCT.replace("\"Bread\"", "42"), PRODUCT.replace("\"Fresh\"", "7"),
                PRODUCT.replace("\"PRODUCT\"", "0"), PRODUCT.replace("PRODUCT", "OTHER"),
                PRODUCT.replace("\"type\":\"PRODUCT\"", "\"type\":\"PRODUCT\",\"published\":\"true\"") }) {
            mvc.perform(post(path).header("Authorization", bearer(ownerToken))
                    .contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void failedStockCreationRollsBackArticle() {
        doThrow(new DataIntegrityViolationException("simulated stock failure")).when(stockSpy).saveAndFlush(any(Stock.class));
        assertThatThrownBy(() -> mvc.perform(post(path).header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON).content(PRODUCT)))
                .hasRootCauseInstanceOf(DataIntegrityViolationException.class);
        assertThat(articles.findByStoreId(storeId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements())
                .isZero();
    }

    @Test
    void updatesPublishesAndUnpublishesAndRejectsStaleVersion() throws Exception {
        JsonNode created = create(PRODUCT);
        String articlePath = path + "/" + created.path("id").asString();
        String update = "{\"name\":\"New bread\",\"description\":\"Updated\",\"priceCents\":250,\"published\":true,\"version\":0}";
        var result = mvc.perform(put(articlePath).header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isOk()).andReturn();
        JsonNode changed = JSON.readTree(result.getResponse().getContentAsString());
        assertThat(changed.path("published").booleanValue()).isTrue();
        assertThat(changed.path("priceCents").longValue()).isEqualTo(250);
        assertThat(changed.path("availableQuantity").intValue()).isEqualTo(7);
        assertThat(changed.path("version").intValue()).isEqualTo(1);
        mvc.perform(put(articlePath).header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isConflict());
        String unpublish = update.replace("true", "false").replace("\"version\":0", "\"version\":1");
        mvc.perform(put(articlePath).header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON).content(unpublish)).andExpect(status().isOk());
        mvc.perform(put(articlePath).header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON).content(unpublish.replace("\"version\":1", "\"version\":2,\"type\":\"SERVICE\"")))
                .andExpect(status().isBadRequest());
        create(SERVICE);
        mvc.perform(put(articlePath).header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(unpublish.replace("New bread", "Delivery").replace("\"version\":1", "\"version\":2")))
                .andExpect(status().isConflict());
        mvc.perform(delete(articlePath).header("Authorization", bearer(ownerToken))).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void protectsAllOperationsAndChecksStoreAndArticleExistence() throws Exception {
        JsonNode created = create(PRODUCT);
        String articlePath = path + "/" + created.path("id").asString();
        String other = token(UUID.randomUUID(), "MERCHANT", "lille", Instant.now().plusSeconds(600), SECRET);
        String update = "{\"name\":\"Bread\",\"priceCents\":125,\"published\":false,\"version\":0}";
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(PRODUCT)).andExpect(status().isUnauthorized());
        mvc.perform(get(path).header("Authorization", bearer(other))).andExpect(status().isForbidden());
        mvc.perform(post(path).header("Authorization", bearer(other)).contentType(MediaType.APPLICATION_JSON)
                .content(PRODUCT)).andExpect(status().isForbidden());
        mvc.perform(put(articlePath).header("Authorization", bearer(other)).contentType(MediaType.APPLICATION_JSON)
                .content(update)).andExpect(status().isForbidden());
        mvc.perform(get("/api/stores/" + UUID.randomUUID() + "/articles").header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNotFound());
        mvc.perform(put(path + "/" + UUID.randomUUID()).header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON).content(update)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsExpiredWrongCityWrongSignatureAndNonMerchantTokens() throws Exception {
        for (String invalid : new String[] {
                token(owner, "MERCHANT", "paris", Instant.now().plusSeconds(600), SECRET),
                token(owner, "MERCHANT", "lille", Instant.now().minusSeconds(3600), SECRET),
                token(owner, "MERCHANT", "lille", Instant.now().plusSeconds(600), "another-test-key-not-for-production-123")
        }) {
            mvc.perform(get(path).header("Authorization", bearer(invalid))).andExpect(status().isUnauthorized());
        }
        mvc.perform(get(path).header("Authorization", bearer(token(owner, "CUSTOMER", "lille",
                Instant.now().plusSeconds(600), SECRET)))).andExpect(status().isForbidden());
    }

    @Test
    void cookiesSessionsAndQueryParametersCannotAuthenticateArticleRequests() throws Exception {
        var session = new MockHttpSession();
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                owner.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_MERCHANT"))));
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);

        mvc.perform(post(path).session(session).cookie(new Cookie("access_token", ownerToken))
                .param("access_token", ownerToken).contentType(MediaType.APPLICATION_JSON).content(PRODUCT))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(path).cookie(new Cookie("access_token", ownerToken)).param("access_token", ownerToken))
                .andExpect(status().isUnauthorized());
        assertThat(articles.findByStoreId(storeId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements())
                .isZero();
    }

    @Test
    void documentsArticleEndpointsWithoutRestrictingExistingPublicRoutes() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk());
        var result = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();
        JsonNode docs = JSON.readTree(result.getResponse().getContentAsString());
        JsonNode endpoints = docs.path("paths").path("/api/stores/{storeId}/articles");
        assertThat(endpoints.has("get")).isTrue();
        assertThat(endpoints.has("post")).isTrue();
        assertThat(endpoints.path("get").path("parameters").toString()).contains("\"page\"", "\"size\"");
        assertThat(docs.path("paths").path("/api/stores/{storeId}/articles/{id}").has("put")).isTrue();
        assertThat(docs.path("components").path("securitySchemes").has("bearerAuth")).isTrue();
    }

    private JsonNode create(String content) throws Exception {
        var result = mvc.perform(post(path).header("Authorization", bearer(ownerToken))
                .contentType(MediaType.APPLICATION_JSON).content(content))
                .andExpect(status().isCreated()).andReturn();
        assertThat(result.getResponse().getHeader("Location")).isNotBlank();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String token(UUID user, String role, String city, Instant expires, String secret) {
        var key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var claims = JwtClaimsSet.builder().subject(user.toString()).claim("role", role).claim("city", city)
                .issuedAt(Instant.now().minusSeconds(7200)).expiresAt(expires).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
