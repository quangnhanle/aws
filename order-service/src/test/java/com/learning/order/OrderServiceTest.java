package com.learning.order;

import com.learning.order.client.ProductClient;
import com.learning.order.constant.OrderStatus;
import com.learning.order.dto.CreateOrderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    // ----- create -----

    @Test
    void create_productExists_savesOrderWithCreatedStatus() {
        UUID productId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(productId);
        request.setQuantity(3);

        when(productClient.exists(productId)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.create(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();

        assertThat(saved.getProductId()).isEqualTo(productId);
        assertThat(saved.getQuantity()).isEqualTo(3);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(result).isSameAs(saved);
    }

    @Test
    void create_productMissing_throwsNotFound_andDoesNotSave() {
        UUID productId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest();
        request.setProductId(productId);
        request.setQuantity(1);

        when(productClient.exists(productId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verifyNoInteractions(orderRepository);
    }

    // ----- findAll -----

    @Test
    void findAll_returnsAllOrders() {
        List<Order> orders = List.of(
                new Order(UUID.randomUUID(), 1),
                new Order(UUID.randomUUID(), 2));
        when(orderRepository.findAll()).thenReturn(orders);

        assertThat(orderService.findAll()).isEqualTo(orders);
    }

    // ----- findById -----

    @Test
    void findById_existing_returnsOrder() {
        UUID id = UUID.randomUUID();
        Order order = new Order(UUID.randomUUID(), 1);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThat(orderService.findById(id)).isSameAs(order);
    }

    @Test
    void findById_missing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    // ----- updateStatus -----

    @Test
    void updateStatus_existing_changesStatus_andSaves() {
        UUID id = UUID.randomUUID();
        Order order = new Order(UUID.randomUUID(), 1);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.updateStatus(id, OrderStatus.COMPLETED);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_missing_throwsNotFound_andDoesNotSave() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(id, OrderStatus.PROCESSING))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(orderRepository, never()).save(any());
    }

    // ----- delete -----

    @Test
    void delete_existing_deletesOrder() {
        UUID id = UUID.randomUUID();
        Order order = new Order(UUID.randomUUID(), 1);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        orderService.delete(id);

        verify(orderRepository).delete(order);
    }

    @Test
    void delete_missing_throwsNotFound_andDoesNotDelete() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.delete(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));

        verify(orderRepository, never()).delete(any());
    }
}
