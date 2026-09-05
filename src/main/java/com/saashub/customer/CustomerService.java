package com.saashub.customer;

import com.saashub.common.exception.BusinessException;
import com.saashub.common.exception.ResourceNotFoundException;
import com.saashub.customer.dto.CreateCustomerRequest;
import com.saashub.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public Customer createCustomer(CreateCustomerRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        if (customerRepository.existsByTenantIdAndEmail(tenantId, request.getEmail())) {
            throw new BusinessException("Customer with this email already exists in organization", HttpStatus.CONFLICT);
        }

        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone())
                .company(request.getCompany())
                .build();
        customer.setTenantId(tenantId);

        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        return customerRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Customer> getCustomers(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return customerRepository.findAllByTenantId(tenantId, pageable);
    }
}
