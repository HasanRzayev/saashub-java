package com.saashub.order;

import com.saashub.common.dto.ApiResponse;
import com.saashub.order.dto.CreateTenantOrderRequest;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Multi-tenant B2B order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'EMPLOYEE')")
    @Operation(summary = "Create sales order for current tenant")
    public ResponseEntity<ApiResponse<Order>> createOrder(@Valid @RequestBody CreateTenantOrderRequest request) {
        Order order = orderService.createOrder(request);
        return new ResponseEntity<>(ApiResponse.ok("Order created", order), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID (Strict tenant boundary check)")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @GetMapping
    @Operation(summary = "Get paginated orders for authenticated organization")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrders(@PageableDefault(size = 20) Pageable pageable) {
        Page<Order> orders = orderService.getOrders(pageable);
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }
}
