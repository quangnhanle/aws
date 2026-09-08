package com.learning.order;

import com.learning.order.client.ProductClient;
import com.learning.order.constant.OrderStatus;
import com.learning.order.dto.CreateOrderRequest;
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
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;

    @Transactional
    public Order create(CreateOrderRequest request) {
        if (!productClient.exists(request.getProductId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found: " + request.getProductId());
        }

        return orderRepository.save(new Order(request.getProductId(), request.getQuantity()));
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found: " + id));
    }

    @Transactional
    public Order updateStatus(UUID id, OrderStatus status) {
        Order order = findById(id);
        order.changeStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public void delete(UUID id) {
        Order order = findById(id);
        orderRepository.delete(order);
    }
}
