package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.exceptions.OrderNotFoundException;
import com.nimbleways.springboilerplate.exceptions.OrderShouldHaveItemsException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductService productService;
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        Long orderId = 1L;
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderServiceImpl.processOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order not found");
    }

    @Test
    void shouldThrowExceptionWhenOrderNotHaveItems() {
        Long orderId = 1L;
        var order = new Order();
        order.setItems(null);
        order.setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        assertThatThrownBy(() -> orderServiceImpl.processOrder(orderId))
                .isInstanceOf(OrderShouldHaveItemsException.class)
                .hasMessage("Order should have items");
    }

    @Test
    void shouldProcessOrderProducts(){
        Long orderId = 1L;
        var order = new Order();
        order.setItems(Set.of(Product.builder().type("NORMAL").leadTime(4).available(3).build(), Product.builder().available(8).leadTime(4).type("SEASONAL").seasonStartDate(LocalDate.now()).seasonEndDate(LocalDate.now().plusDays(40)).build(), Product.builder().leadTime(4).available(5).expiryDate(LocalDate.now()).type("EXPIRABLE").build()));
        order.setId(orderId);
        Mockito.when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        Mockito.when(productRepository.save(Mockito.any())).thenReturn(Product.builder().available(0).build());
        Mockito.doNothing().when(productService).handleSeasonalProduct(Mockito.any());
        Mockito.doNothing().when(productService).handleExpiredProduct(Mockito.any());
        var result = orderServiceImpl.processOrder(orderId);
        assertThat(result.id()).isEqualTo(orderId);
    }
}