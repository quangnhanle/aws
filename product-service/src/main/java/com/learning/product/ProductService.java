package com.learning.product;

import com.learning.product.dto.CreateProductRequest;
import com.learning.product.dto.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product create(CreateProductRequest request) {
        Product product = new Product(
                request.getName().trim(),
                request.getPrice(),
                normalizeImageUrl(request.getImageUrl())
        );
        return productRepository.save(product);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + id));
    }

    @Transactional
    public Product update(UUID id, UpdateProductRequest request) {
        Product product = findById(id);
        product.update(
                request.getName().trim(),
                request.getPrice(),
                normalizeImageUrl(request.getImageUrl())
        );
        return productRepository.save(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = findById(id);
        productRepository.delete(product);
    }

    private String normalizeImageUrl(String imageUrl) {
        return imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim();
    }
}
