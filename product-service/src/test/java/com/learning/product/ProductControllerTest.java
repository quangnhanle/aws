package com.learning.product;

import com.learning.product.dto.CreateProductRequest;
import com.learning.product.dto.UpdateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    // ----- POST /api/products -----

    @Test
    void create_valid_returns201WithBody() throws Exception {
        when(productService.create(any(CreateProductRequest.class)))
                .thenReturn(new Product("Laptop", new BigDecimal("1200.00"), "http://img/laptop.png"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Laptop","price":1200.00,"imageUrl":"http://img/laptop.png"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(1200.00))
                .andExpect(jsonPath("$.imageUrl").value("http://img/laptop.png"));

        verify(productService).create(any(CreateProductRequest.class));
    }

    @Test
    void create_blankName_returns400_andSkipsService() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"   ","price":10.00}"""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void create_nonPositivePrice_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Laptop","price":0.00}"""))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    // ----- GET /api/products -----

    @Test
    void findAll_returns200WithList() throws Exception {
        when(productService.findAll()).thenReturn(List.of(
                new Product("A", new BigDecimal("1.00"), null),
                new Product("B", new BigDecimal("2.00"), null)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    // ----- GET /api/products/{id} -----

    @Test
    void findById_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id))
                .thenReturn(new Product("Laptop", new BigDecimal("1200.00"), null));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void findById_missing_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + id));

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_invalidUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/products/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    // ----- PUT /api/products/{id} -----

    @Test
    void update_valid_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.update(eq(id), any(UpdateProductRequest.class)))
                .thenReturn(new Product("New", new BigDecimal("5.50"), null));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New","price":5.50}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"))
                .andExpect(jsonPath("$.price").value(5.50));

        verify(productService).update(eq(id), any(UpdateProductRequest.class));
    }

    @Test
    void update_invalidBody_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","price":null}"""))
                .andExpect(status().isBadRequest());

        verify(productService, never()).update(any(), any());
    }

    // ----- DELETE /api/products/{id} -----

    @Test
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        verify(productService).delete(id);
    }
}
