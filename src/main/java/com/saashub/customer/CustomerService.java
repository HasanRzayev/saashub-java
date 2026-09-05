package com.saashub.customer;

import com.saashub.common.exception.BusinessException;
import com.saashub.common.exception.ResourceNotFoundException;
import com.saashub.multitenancy.TenantContext;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CreateCustomerRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    private String phone;
    private String company;
}

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
