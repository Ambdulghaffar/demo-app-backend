package com.elhaffar.exoformbackend.controllers;

import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.supplier.SupplierOrderRequestDTO;
import com.elhaffar.exoformbackend.dto.supplier.SupplierOrderResponseDTO;
import com.elhaffar.exoformbackend.dto.supplier.UpdateSupplierOrderStatusDTO;
import com.elhaffar.exoformbackend.services.SupplierOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplier-orders")
public class SupplierOrderController {

    private final SupplierOrderService supplierOrderService;

    public SupplierOrderController(SupplierOrderService supplierOrderService) {
        this.supplierOrderService = supplierOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<PageResponseDTO<SupplierOrderResponseDTO>> getAllSupplierOrders(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)    String status
    ) {
        return ResponseEntity.ok(supplierOrderService.getAllSupplierOrders(page, size, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<SupplierOrderResponseDTO> getSupplierOrderById(@PathVariable Integer id) {
        return ResponseEntity.ok(supplierOrderService.getSupplierOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<SupplierOrderResponseDTO> createSupplierOrder(
            @Valid @RequestBody SupplierOrderRequestDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierOrderService.createSupplierOrder(dto));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<SupplierOrderResponseDTO> updateSupplierOrderStatus(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateSupplierOrderStatusDTO dto
    ) {
        return ResponseEntity.ok(supplierOrderService.updateSupplierOrderStatus(id, dto));
    }
}
