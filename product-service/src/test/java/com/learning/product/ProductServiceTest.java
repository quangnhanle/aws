package com.learning.product;

import com.learning.product.dto.CreateProductRequest;
import com.learning.product.dto.UpdateProductRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    // ----- create -----

    @Test
    void create_trimsName_normalizesImageUrl_andSaves() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("  Laptop  ");
        request.setPrice(new BigDecimal("1200.00"));
        request.setImageUrl("  http://img/laptop.png  ");

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Laptop");
        assertThat(saved.getPrice()).isEqualByComparingTo("1200.00");
        assertThat(saved.getImageUrl()).isEqualTo("http://img/laptop.png");
        assertThat(result).isSameAs(saved);
    }

    @Test
    void create_blankImageUrl_becomesNull() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Phone");
        request.setPrice(new BigDecimal("999.99"));
        request.setImageUrl("   ");

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.create(request);

        assertThat(result.getImageUrl()).isNull();
    }

    @Test
    void create_nullImageUrl_becomesNull() {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("Phone");
        request.setPrice(new BigDecimal("10.00"));
        request.setImageUrl(null);

        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.create(request);

        assertThat(result.getImageUrl()).isNull();
    }

    // ----- findAll -----

    @Test
    void findAll_returnsAllProducts() {
        List<Product> products = List.of(
                new Product("A", new BigDecimal("1.00"), null),
                new Product("B", new BigDecimal("2.00"), null));
        when(productRepository.findAll()).thenReturn(products);

        assertThat(productService.findAll()).isEqualTo(products);
    }

    // ----- findById -----

    @Test
    void findById_existing_returnsProduct() {
        UUID id = UUID.randomUUID();
        Product product = new Product("A", new BigDecimal("1.00"), null);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        assertThat(productService.findById(id)).isSameAs(product);
    }

    @Test
    void findById_missing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ----- update -----

    @Test
    void update_existing_updatesFields_andSaves() {
        UUID id = UUID.randomUUID();
        Product product = new Product("Old", new BigDecimal("1.00"), "old.png");
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("  New  ");
        request.setPrice(new BigDecimal("5.50"));
        request.setImageUrl("  new.png  ");

        Product result = productService.update(id, request);

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getPrice()).isEqualByComparingTo("5.50");
        assertThat(result.getImageUrl()).isEqualTo("new.png");
        verify(productRepository).save(product);
    }

    @Test
    void update_missing_throwsNotFound_andDoesNotSave() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("New");
        request.setPrice(new BigDecimal("5.50"));

        assertThatThrownBy(() -> productService.update(id, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(productRepository, never()).save(any());
    }

    // ----- delete -----

    @Test
    void delete_existing_deletesProduct() {
        UUID id = UUID.randomUUID();
        Product product = new Product("A", new BigDecimal("1.00"), null);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        productService.delete(id);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_missing_throwsNotFound_andDoesNotDelete() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(productRepository, never()).delete(any());
    }
}
