package com.learning.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Gọi sang product-service qua HTTP (đúng mũi tên "Service Connect / sync" trong sơ đồ).
 * 2 service KHÔNG dùng chung DB/Repository — order-service chỉ hỏi product-service qua API.
 */
@Component
public class ProductClient {

    private final RestClient restClient;

    // Dùng factory tĩnh RestClient.create(...) -> không cần bean RestClient.Builder
    // (bean này không được auto-config sẵn ở Spring Boot 4 trong module hiện tại).
    public ProductClient(@Value("${product-service.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    /** true nếu product tồn tại (HTTP 2xx), false nếu 404. */
    public boolean exists(UUID productId) {
        return restClient.get()
                .uri("/api/products/{id}", productId)
                // exchange: tự xử lý response, KHÔNG ném exception khi gặp 4xx
                .exchange((request, response) -> response.getStatusCode().is2xxSuccessful());
    }
}
