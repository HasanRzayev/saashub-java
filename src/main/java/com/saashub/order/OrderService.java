package com.saashub.order;

import com.saashub.common.exception.BusinessException;
import com.saashub.common.exception.ResourceNotFoundException;
import com.saashub.customer.Customer;
import com.saashub.customer.CustomerRepository;
import com.saashub.multitenancy.TenantContext;
import com.saashub.order.dto.CreateTenantOrderRequest;
import com.saashub.order.dto.OrderItemDTO;
import com.saashub.product.Product;
import com.saashub.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrder(CreateTenantOrderRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Customer customer = customerRepository.findByIdAndTenantId(request.getCustomerId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found in this organization: " + request.getCustomerId()));

        String orderNumber = "SO-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();
        order.setTenantId(tenantId);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO itemDto : request.getItems()) {
            Product product = productRepository.findByIdAndTenantId(itemDto.getProductId(), tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found in this organization: " + itemDto.getProductId()));

            if (product.getStock() < itemDto.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - itemDto.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            total = total.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDto.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            orderItem.setTenantId(tenantId);

            order.addItem(orderItem);
        }

        order.setTotalAmount(total);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        return orderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrders(Pageable pageable) {
        String tenantId = TenantContext.getCurrentTenant();
        return orderRepository.findAllByTenantId(tenantId, pageable);
    }
}
