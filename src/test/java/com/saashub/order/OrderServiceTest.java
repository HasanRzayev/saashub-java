package com.saashub.order;

import com.saashub.customer.Customer;
import com.saashub.customer.CustomerRepository;
import com.saashub.multitenancy.TenantContext;
import com.saashub.order.dto.CreateTenantOrderRequest;
import com.saashub.order.dto.OrderItemDTO;
import com.saashub.product.Product;
import com.saashub.product.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("tenant-test");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createOrder_Success() {
        Customer customer = Customer.builder().id(10L).firstName("Bob").lastName("Dylan").build();
        customer.setTenantId("tenant-test");

        Product product = Product.builder().id(20L).sku("PROD-1").name("Widget").price(new BigDecimal("50.00")).stock(100).build();
        product.setTenantId("tenant-test");

        CreateTenantOrderRequest request = CreateTenantOrderRequest.builder()
                .customerId(10L)
                .items(List.of(OrderItemDTO.builder().productId(20L).quantity(2).build()))
                .build();

        when(customerRepository.findByIdAndTenantId(10L, "tenant-test")).thenReturn(Optional.of(customer));
        when(productRepository.findByIdAndTenantId(20L, "tenant-test")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setId(1001L);
            return o;
        });

        Order order = orderService.createOrder(request);

        assertNotNull(order);
        assertEquals(new BigDecimal("100.00"), order.getTotalAmount());
        assertEquals("tenant-test", order.getTenantId());
        assertEquals(98, product.getStock());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(productRepository, times(1)).save(product);
    }
}
