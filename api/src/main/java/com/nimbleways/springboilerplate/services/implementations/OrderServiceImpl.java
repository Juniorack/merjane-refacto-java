package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.exceptions.OrderShouldHaveItemsException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.OrderService;
import com.nimbleways.springboilerplate.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public ProcessOrderResponse processOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);
        if (orderOptional.isEmpty()) {
            throw new OrderNotFoundException("Order not found");
        }
        var order = orderOptional.get();
        if (order.getItems() == null || order.getItems().isEmpty()) {
            log.error("Order has no items");
            throw new OrderShouldHaveItemsException("Order should have items");
        }
        log.info("Processing order {}", order.getId());
        processOrderProducts(order.getItems());
        orderRepository.save(order);
        log.info("Order {} processed successfully", order.getId());
        return new ProcessOrderResponse(order.getId());
    }

    private void processOrderProducts(Set<Product> products) {
        for (Product p : products) {
            switch (p.getType()) {
                case "NORMAL" -> processNormalProduct(p);
                case "SEASONAL" -> processSeasonalProduct(p);
                case "EXPIRABLE" -> processExpiredProduct(p);
                default -> log.error("Invalid product type {}, product id : {}", p.getType(), p.getId());
            }
        }
    }

    private void processNormalProduct(Product p) {
        if (p.getAvailable() > 0) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            if (p.getLeadTime() > 0) {
                productService.notifyDelay(p.getLeadTime(), p);
            }
        }
    }

    private void processSeasonalProduct(Product p) {
        if ((LocalDate.now().isAfter(p.getSeasonStartDate()) && LocalDate.now().isBefore(p.getSeasonEndDate())
                && p.getAvailable() > 0)) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            productService.handleSeasonalProduct(p);
        }
    }

    private void processExpiredProduct(Product p) {
        if (p.getAvailable() > 0 && p.getExpiryDate().isAfter(LocalDate.now())) {
            p.setAvailable(p.getAvailable() - 1);
            productRepository.save(p);
        } else {
            productService.handleExpiredProduct(p);
        }
    }
}
