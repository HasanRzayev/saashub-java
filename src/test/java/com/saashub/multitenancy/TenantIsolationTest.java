package com.saashub.multitenancy;

import com.saashub.common.exception.ResourceNotFoundException;
import com.saashub.customer.Customer;
import com.saashub.customer.CustomerRepository;
import com.saashub.customer.CustomerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantIsolationTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant("tenant-alpha");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void getCustomerById_SameTenant_ReturnsCustomer() {
        Customer customerAlpha = Customer.builder()
                .id(101L)
                .firstName("John")
                .lastName("Doe")
                .email("john@alpha.internal")
                .build();
        customerAlpha.setTenantId("tenant-alpha");

        when(customerRepository.findByIdAndTenantId(101L, "tenant-alpha"))
                .thenReturn(Optional.of(customerAlpha));

        Customer result = customerService.getCustomerById(101L);

        assertNotNull(result);
        assertEquals("tenant-alpha", result.getTenantId());
        assertEquals("john@alpha.internal", result.getEmail());
    }

    @Test
    void getCustomerById_CrossTenantAttempt_ThrowsResourceNotFound() {
        when(customerRepository.findByIdAndTenantId(101L, "tenant-alpha"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                customerService.getCustomerById(101L));

        verify(customerRepository, times(1)).findByIdAndTenantId(101L, "tenant-alpha");
    }
}
