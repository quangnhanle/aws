package com.learning.order;

import com.learning.order.constant.OrderStatus;
import com.learning.order.dto.CreateOrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    // ----- POST /api/orders -----

    @Test
    void create_valid_returns201WithBody() throws Exception {
        UUID productId = UUID.randomUUID();
        when(orderService.create(any(CreateOrderRequest.class)))
                .thenReturn(new Order(productId, 3));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\",\"quantity\":3}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.status").value("CREATED"));

        verify(orderService).create(any(CreateOrderRequest.class));
    }

    @Test
    void create_productNotFound_returns404() throws Exception {
        UUID productId = UUID.randomUUID();
        when(orderService.create(any(CreateOrderRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + productId + "\",\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_missingProductId_returns400_andSkipsService() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":null,\"quantity\":1}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void create_quantityBelowMin_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"" + UUID.randomUUID() + "\",\"quantity\":0}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    // ----- GET /api/orders -----

    @Test
    void findAll_returns200WithList() throws Exception {
        when(orderService.findAll()).thenReturn(List.of(
                new Order(UUID.randomUUID(), 1),
                new Order(UUID.randomUUID(), 2)));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].quantity").value(1))
                .andExpect(jsonPath("$[1].quantity").value(2));
    }

    // ----- GET /api/orders/{id} -----

    @Test
    void findById_existing_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.findById(id)).thenReturn(new Order(UUID.randomUUID(), 5));

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void findById_missing_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.findById(id))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));

        mockMvc.perform(get("/api/orders/{id}", id))
                .andExpect(status().isNotFound());
    }

    // ----- PATCH /api/orders/{id}/status -----

    @Test
    void updateStatus_valid_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        Order order = new Order(UUID.randomUUID(), 1);
        order.changeStatus(OrderStatus.COMPLETED);
        when(orderService.updateStatus(eq(id), eq(OrderStatus.COMPLETED))).thenReturn(order);

        mockMvc.perform(patch("/api/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(orderService).updateStatus(eq(id), eq(OrderStatus.COMPLETED));
    }

    @Test
    void updateStatus_missingStatus_returns400() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(patch("/api/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":null}"""))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateStatus(any(), any());
    }

    @Test
    void updateStatus_invalidEnum_returns400() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/orders/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NOPE\"}"))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).updateStatus(any(), any());
    }

    // ----- DELETE /api/orders/{id} -----

    @Test
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/orders/{id}", id))
                .andExpect(status().isNoContent());

        verify(orderService).delete(id);
    }
}
