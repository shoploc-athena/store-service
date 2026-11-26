package com.shoploc.store.store;

import java.net.URI;
import java.sql.SQLException;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/stores/{storeId}/articles")
@Tag(name = "Articles", description = "Store owner catalogue management")
@SecurityRequirement(name = "bearerAuth")
public class ArticleController {
    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a product or service", responses = {
            @ApiResponse(responseCode = "201", description = "Article created"),
            @ApiResponse(responseCode = "400", description = "Invalid article"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Not the owner"),
            @ApiResponse(responseCode = "404", description = "Store not found"),
            @ApiResponse(responseCode = "409", description = "Article name already used")})
    public ResponseEntity<ArticleService.ArticleResponse> create(@PathVariable UUID storeId,
            @AuthenticationPrincipal Jwt jwt, @RequestBody CreateArticle request) {
        String name = validName(request.name());
        long price = nonNegativeLong(request.priceCents(), "priceCents");
        ArticleType type = articleType(request.type());
        if (type == ArticleType.SERVICE && request.initialStock() != null) {
            throw badRequest("initialStock is forbidden for services");
        }
        int stock = request.initialStock() == null ? 0 : nonNegativeInt(request.initialStock(), "initialStock");
        boolean published = request.published() != null && requiredBoolean(request.published(), "published");
        var details = new ArticleService.CreateDetails(name, optionalText(request.description()), price, type, published, stock);
        var article = service.create(storeId, UUID.fromString(jwt.getSubject()), details);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(article.id()).toUri();
        return ResponseEntity.created(location).body(article);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update or publish/unpublish an article", responses = {
            @ApiResponse(responseCode = "200", description = "Article updated"),
            @ApiResponse(responseCode = "400", description = "Invalid article"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Not the owner"),
            @ApiResponse(responseCode = "404", description = "Store or article not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate name or stale version")})
    public ArticleService.ArticleResponse update(@PathVariable UUID storeId, @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt, @RequestBody UpdateArticle request) {
        if (request.type() != null) {
            throw badRequest("type cannot be changed and published is required");
        }
        var details = new ArticleService.UpdateDetails(validName(request.name()),
                optionalText(request.description()), nonNegativeLong(request.priceCents(), "priceCents"),
                requiredBoolean(request.published(), "published"), nonNegativeInt(request.version(), "version"));
        return service.update(storeId, id, UUID.fromString(jwt.getSubject()), details);
    }

    @GetMapping
    @Operation(summary = "List published and unpublished articles for the owner", responses = {
            @ApiResponse(responseCode = "200", description = "Page of articles"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Not the owner"),
            @ApiResponse(responseCode = "404", description = "Store not found")})
    public Page<ArticleService.ArticleResponse> list(@PathVariable UUID storeId, @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.list(storeId, UUID.fromString(jwt.getSubject()), pageable);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Void> staleVersion() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> duplicateName(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())
                    && sql.getMessage().contains("article_name_unique_per_store")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        throw exception;
    }

    private static String validName(JsonNode value) {
        if (value == null || !value.isTextual()) {
            throw badRequest("name must be a string");
        }
        String name = value.textValue();
        if (name.isBlank() || name.codePointCount(0, name.length()) > 150) {
            throw badRequest("name must contain 1 to 150 characters");
        }
        return name;
    }

    private static String optionalText(JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        if (!value.isTextual()) {
            throw badRequest("description must be a string");
        }
        return value.textValue();
    }

    private static boolean requiredBoolean(JsonNode value, String field) {
        if (value == null || !value.isBoolean()) {
            throw badRequest(field + " must be a boolean");
        }
        return value.booleanValue();
    }

    private static ArticleType articleType(JsonNode value) {
        if (value != null && value.isTextual()) {
            if ("PRODUCT".equals(value.textValue())) {
                return ArticleType.PRODUCT;
            }
            if ("SERVICE".equals(value.textValue())) {
                return ArticleType.SERVICE;
            }
        }
        throw badRequest("type must be PRODUCT or SERVICE");
    }

    private static long nonNegativeLong(JsonNode value, String field) {
        if (value == null || !value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() < 0) {
            throw badRequest(field + " must be a non-negative integer");
        }
        return value.longValue();
    }

    private static int nonNegativeInt(JsonNode value, String field) {
        long number = nonNegativeLong(value, field);
        if (number > Integer.MAX_VALUE) {
            throw badRequest(field + " is too large");
        }
        return (int) number;
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    public record CreateArticle(@Schema(type = "string", minLength = 1, maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED) JsonNode name,
            @Schema(type = "string", nullable = true) JsonNode description,
            @Schema(type = "integer", format = "int64", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
            JsonNode priceCents,
            @Schema(type = "string", allowableValues = {"PRODUCT", "SERVICE"}, requiredMode = Schema.RequiredMode.REQUIRED)
            JsonNode type, @Schema(type = "boolean", defaultValue = "false") JsonNode published,
            @Schema(type = "integer", minimum = "0", description = "Products only; defaults to zero")
            JsonNode initialStock) {
    }

    public record UpdateArticle(@Schema(type = "string", minLength = 1, maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED) JsonNode name,
            @Schema(type = "string", nullable = true) JsonNode description,
            @Schema(type = "integer", format = "int64", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
            JsonNode priceCents, @Schema(type = "boolean", requiredMode = Schema.RequiredMode.REQUIRED) JsonNode published,
            @Schema(type = "integer", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED) JsonNode version,
            @Schema(hidden = true) JsonNode type) {
    }
}
