package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ProductServiceImpl productServiceImpl;

    @BeforeEach
    void setUp() {

    }

    @Test
    void notifyDelay() {
        int loadTime = 10;
        var given = new Product(null, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);
        var result =  Product.builder().id(null).leadTime(loadTime).available(0).type("NORMAL").name("RJ45 Cable").build();
        given.setLeadTime(loadTime);

        Mockito.when(productRepository.save(given)).thenReturn(result);
        Mockito.doNothing().when(notificationService).sendDelayNotification(loadTime, given.getName());
        productServiceImpl.notifyDelay(loadTime, given);
        assertThat(given.getLeadTime()).isEqualTo(loadTime);
    }

    @Test
    void shouldNotHandleSeasonalProduct() {
        var given = Product.builder().available(30)
                .seasonStartDate(LocalDate.now().plusDays(26))
                .type("EXPIRABLE").name("Butter").build();
        assertThatThrownBy(() -> productServiceImpl.handleSeasonalProduct(given))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void handleExpiredProduct() {
    }
}