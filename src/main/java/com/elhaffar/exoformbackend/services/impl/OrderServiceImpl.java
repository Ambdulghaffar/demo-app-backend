package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.common.enums.OrderStatus;
import com.elhaffar.exoformbackend.common.enums.StockMovementType;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.order.*;
import com.elhaffar.exoformbackend.entities.*;
import com.elhaffar.exoformbackend.exceptions.BusinessException;
import com.elhaffar.exoformbackend.exceptions.ResourceNotFoundException;
import com.elhaffar.exoformbackend.repository.*;
import com.elhaffar.exoformbackend.services.OrderService;
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
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            StockMovementRepository stockMovementRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponseDTO> getMyOrders(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", email));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders = orderRepository.findByUser(user, pageable);
        return PageResponseDTO.from(orders.map(this::toOrderResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getMyOrderById(String email, Integer orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", email));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", orderId));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Commande", orderId);
        }
        return toOrderResponseDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<OrderResponseDTO> getAllOrders(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByStatus(orderStatus, pageable);
            } catch (IllegalArgumentException e) {
                orders = orderRepository.findAll(pageable);
            }
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return PageResponseDTO.from(orders.map(this::toOrderResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", orderId));
        return toOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO createOrder(String email, OrderRequestDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", email));

        // Phase 1 : valider TOUS les items avant toute modification
        List<Product> resolvedProducts = new ArrayList<>();
        for (OrderItemRequestDTO itemDto : dto.items()) {
            Product product = productRepository.findById(itemDto.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit", itemDto.productId()));
            if (product.getStock() < itemDto.quantity()) {
                throw new BusinessException(
                        "Stock insuffisant pour le produit \"" + product.getName() + "\" " +
                        "(disponible : " + product.getStock() + ", demandé : " + itemDto.quantity() + ")"
                );
            }
            resolvedProducts.add(product);
        }

        // Phase 2 : construire les OrderItems (snapshots) et calculer le total
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < dto.items().size(); i++) {
            OrderItemRequestDTO itemDto = dto.items().get(i);
            Product product = resolvedProducts.get(i);
            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.quantity()));
            totalAmount = totalAmount.add(subtotal);
            orderItems.add(OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(unitPrice)
                    .quantity(itemDto.quantity())
                    .build());
        }

        // Phase 3 : créer et sauvegarder la commande (les items sont liés via cascade)
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingAddress(dto.shippingAddress())
                .totalAmount(totalAmount)
                .items(orderItems)
                .build();
        // lier chaque item à la commande
        orderItems.forEach(item -> item.setOrder(order));
        Order savedOrder = orderRepository.save(order);

        // Phase 4 : décrémenter le stock et tracer les mouvements
        for (int i = 0; i < dto.items().size(); i++) {
            OrderItemRequestDTO itemDto = dto.items().get(i);
            Product product = resolvedProducts.get(i);
            product.setStock(product.getStock() - itemDto.quantity());
            productRepository.save(product);
            stockMovementRepository.save(StockMovement.builder()
                    .product(product)
                    .type(StockMovementType.SALE)
                    .quantity(itemDto.quantity())
                    .orderId(savedOrder.getId())
                    .reason("Commande #" + savedOrder.getId())
                    .build());
        }

        return toOrderResponseDTO(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Integer orderId, UpdateOrderStatusDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", orderId));

        OrderStatus current = order.getStatus();
        OrderStatus next = dto.status();

        validateTransition(current, next);

        if (next == OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
                stockMovementRepository.save(StockMovement.builder()
                        .product(product)
                        .type(StockMovementType.CANCELLATION)
                        .quantity(item.getQuantity())
                        .orderId(order.getId())
                        .reason("Annulation commande #" + order.getId())
                        .build());
            }
        }

        order.setStatus(next);
        Order savedOrder = orderRepository.save(order);
        return toOrderResponseDTO(savedOrder);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING   -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.SHIPPED   || next == OrderStatus.CANCELLED;
            case SHIPPED   -> next == OrderStatus.DELIVERED;
            case DELIVERED -> false;
            case CANCELLED -> false;
        };
        if (!valid) {
            throw new BusinessException(
                    "Impossible de passer de " + current + " à " + next
            );
        }
    }

    private OrderResponseDTO toOrderResponseDTO(Order order) {
        List<OrderItemResponseDTO> items = order.getItems().stream()
                .map(item -> new OrderItemResponseDTO(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        return new OrderResponseDTO(
                order.getId(),
                order.getUser().getUsername(),
                order.getShippingAddress(),
                order.getStatus(),
                items,
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
