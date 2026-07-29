package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.common.enums.StockMovementType;
import com.elhaffar.exoformbackend.common.enums.SupplierOrderStatus;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.supplier.*;
import com.elhaffar.exoformbackend.entities.*;
import com.elhaffar.exoformbackend.exceptions.BusinessException;
import com.elhaffar.exoformbackend.exceptions.ResourceNotFoundException;
import com.elhaffar.exoformbackend.repository.ProductRepository;
import com.elhaffar.exoformbackend.repository.StockMovementRepository;
import com.elhaffar.exoformbackend.repository.SupplierOrderRepository;
import com.elhaffar.exoformbackend.repository.SupplierRepository;
import com.elhaffar.exoformbackend.services.SupplierOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SupplierOrderServiceImpl implements SupplierOrderService {

    private final SupplierOrderRepository supplierOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public SupplierOrderServiceImpl(SupplierOrderRepository supplierOrderRepository,
                                    SupplierRepository supplierRepository,
                                    ProductRepository productRepository,
                                    StockMovementRepository stockMovementRepository) {
        this.supplierOrderRepository = supplierOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SupplierOrderResponseDTO> getAllSupplierOrders(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SupplierOrder> orders;
        if (status != null && !status.isBlank()) {
            try {
                SupplierOrderStatus orderStatus = SupplierOrderStatus.valueOf(status.toUpperCase());
                orders = supplierOrderRepository.findByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                orders = supplierOrderRepository.findAll(pageable);
            }
        } else {
            orders = supplierOrderRepository.findAll(pageable);
        }
        return PageResponseDTO.from(orders.map(this::toResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierOrderResponseDTO getSupplierOrderById(Integer id) {
        SupplierOrder order = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande fournisseur", id));
        return toResponseDTO(order);
    }

    @Override
    @Transactional
    public SupplierOrderResponseDTO createSupplierOrder(SupplierOrderRequestDTO dto) {
        Supplier supplier = supplierRepository.findById(dto.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", dto.supplierId()));

        List<SupplierOrderItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (SupplierOrderItemRequestDTO itemDto : dto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit", itemDto.productId()));

            BigDecimal subtotal = itemDto.unitCost().multiply(BigDecimal.valueOf(itemDto.quantity()));
            totalAmount = totalAmount.add(subtotal);

            items.add(SupplierOrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitCost(itemDto.unitCost())
                    .quantity(itemDto.quantity())
                    .build());
        }

        SupplierOrder order = SupplierOrder.builder()
                .supplier(supplier)
                .status(SupplierOrderStatus.PENDING)
                .totalAmount(totalAmount)
                .items(items)
                .build();

        items.forEach(item -> item.setSupplierOrder(order));
        SupplierOrder saved = supplierOrderRepository.save(order);
        return toResponseDTO(saved);
    }

    @Override
    @Transactional
    public SupplierOrderResponseDTO updateSupplierOrderStatus(Integer id, UpdateSupplierOrderStatusDTO dto) {
        SupplierOrder order = supplierOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande fournisseur", id));

        SupplierOrderStatus current = order.getStatus();
        SupplierOrderStatus next = dto.status();

        validateTransition(current, next);

        if (next == SupplierOrderStatus.RECEIVED) {
            for (SupplierOrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);

                stockMovementRepository.save(StockMovement.builder()
                        .product(product)
                        .type(StockMovementType.RESTOCK)
                        .quantity(item.getQuantity())
                        .orderId(null)
                        .reason("Réception commande fournisseur #" + order.getId())
                        .build());
            }
        }

        order.setStatus(next);
        SupplierOrder saved = supplierOrderRepository.save(order);
        return toResponseDTO(saved);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validateTransition(SupplierOrderStatus current, SupplierOrderStatus next) {
        boolean valid = switch (current) {
            case PENDING   -> next == SupplierOrderStatus.ORDERED   || next == SupplierOrderStatus.CANCELLED;
            case ORDERED   -> next == SupplierOrderStatus.RECEIVED  || next == SupplierOrderStatus.CANCELLED;
            case RECEIVED  -> false;
            case CANCELLED -> false;
        };
        if (!valid) {
            throw new BusinessException(
                    "Impossible de passer de " + current + " à " + next
            );
        }
    }

    private SupplierOrderResponseDTO toResponseDTO(SupplierOrder order) {
        List<SupplierOrderItemResponseDTO> items = order.getItems().stream()
                .map(item -> new SupplierOrderItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getUnitCost(),
                        item.getQuantity(),
                        item.getUnitCost().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        return new SupplierOrderResponseDTO(
                order.getId(),
                order.getSupplier().getId(),
                order.getSupplier().getName(),
                order.getStatus(),
                items,
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
