package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.common.enums.StockMovementType;
import com.elhaffar.exoformbackend.common.enums.UserRole;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.stock.StockAdjustmentRequestDTO;
import com.elhaffar.exoformbackend.dto.stock.StockMovementResponseDTO;
import com.elhaffar.exoformbackend.entities.Product;
import com.elhaffar.exoformbackend.entities.StockMovement;
import com.elhaffar.exoformbackend.entities.User;
import com.elhaffar.exoformbackend.exceptions.BusinessException;
import com.elhaffar.exoformbackend.exceptions.ResourceNotFoundException;
import com.elhaffar.exoformbackend.repository.ProductRepository;
import com.elhaffar.exoformbackend.repository.StockMovementRepository;
import com.elhaffar.exoformbackend.repository.UserRepository;
import com.elhaffar.exoformbackend.services.StockMovementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public StockMovementServiceImpl(StockMovementRepository stockMovementRepository,
                                    UserRepository userRepository,
                                    ProductRepository productRepository) {
        this.stockMovementRepository = stockMovementRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<StockMovementResponseDTO> getMovements(int page, int size, Integer productId, StockMovementType type) {
        Pageable pageable = PageRequest.of(page, size);
        Page<StockMovement> movements = stockMovementRepository.findWithFilters(productId, type, pageable);
        return PageResponseDTO.from(movements.map(this::toResponseDTO));
    }

    @Override
    @Transactional
    public StockMovementResponseDTO adjustStock(String requesterEmail, StockAdjustmentRequestDTO dto) {
        if (dto.type() != StockMovementType.RESTOCK && dto.type() != StockMovementType.DAMAGE) {
            throw new BusinessException("Seuls les types RESTOCK et DAMAGE sont autorisés pour un ajustement manuel.");
        }

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", requesterEmail));

        if (dto.type() == StockMovementType.DAMAGE && requester.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Seul un administrateur peut effectuer un ajustement de type casse.");
        }

        Product product = productRepository.findById(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit", dto.productId()));

        if (dto.type() == StockMovementType.DAMAGE && product.getStock() < dto.quantity()) {
            throw new BusinessException(
                    "Quantité de casse supérieure au stock disponible (stock actuel : " + product.getStock() + ")."
            );
        }

        int newStock = dto.type() == StockMovementType.RESTOCK
                ? product.getStock() + dto.quantity()
                : product.getStock() - dto.quantity();
        product.setStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(dto.type())
                .quantity(dto.quantity())
                .reason(dto.reason())
                .build();
        stockMovementRepository.save(movement);

        return toResponseDTO(movement);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private StockMovementResponseDTO toResponseDTO(StockMovement sm) {
        return new StockMovementResponseDTO(
                sm.getId(),
                sm.getProduct().getId(),
                sm.getProduct().getName(),
                sm.getType(),
                sm.getQuantity(),
                sm.getReason(),
                sm.getOrderId(),
                sm.getCreatedAt()
        );
    }
}
