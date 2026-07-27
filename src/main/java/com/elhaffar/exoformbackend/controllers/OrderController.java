package com.elhaffar.exoformbackend.controllers;

import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.order.OrderRequestDTO;
import com.elhaffar.exoformbackend.dto.order.OrderResponseDTO;
import com.elhaffar.exoformbackend.dto.order.UpdateOrderStatusDTO;
import com.elhaffar.exoformbackend.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(authentication.getName(), dto));
    }

    // /me avant /{id} : Spring priorise les segments littéraux, pas d'ambiguïté
    @GetMapping("/me")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getMyOrders(authentication.getName(), page, size));
    }

    @GetMapping("/me/{id}")
    public ResponseEntity<OrderResponseDTO> getMyOrderById(
            Authentication authentication,
            @PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getMyOrderById(authentication.getName(), id));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getAllOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    String status) {
        return ResponseEntity.ok(orderService.getAllOrders(page, size, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateOrderStatusDTO dto) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, dto));
    }
}
