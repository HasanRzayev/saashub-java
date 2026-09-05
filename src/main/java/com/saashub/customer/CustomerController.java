package com.saashub.customer;

import com.saashub.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Tenant-isolated customer directory and CRM operations")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    @Operation(summary = "Create customer within current tenant")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        return new ResponseEntity<>(ApiResponse.ok("Customer created", customer), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID (Tenant isolated)")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.ok(customer));
    }

    @GetMapping
    @Operation(summary = "Get paginated customers for current tenant")
    public ResponseEntity<ApiResponse<Page<Customer>>> getCustomers(@PageableDefault(size = 20) Pageable pageable) {
        Page<Customer> customers = customerService.getCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(customers));
    }
}
