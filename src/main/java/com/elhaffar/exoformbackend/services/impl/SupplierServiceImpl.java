package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.supplier.SupplierRequestDTO;
import com.elhaffar.exoformbackend.dto.supplier.SupplierResponseDTO;
import com.elhaffar.exoformbackend.entities.Supplier;
import com.elhaffar.exoformbackend.exceptions.BusinessException;
import com.elhaffar.exoformbackend.exceptions.ResourceNotFoundException;
import com.elhaffar.exoformbackend.repository.SupplierRepository;
import com.elhaffar.exoformbackend.services.SupplierService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SupplierResponseDTO> getAllSuppliers(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        boolean hasSearch = search != null && !search.isBlank();
        Page<Supplier> result = hasSearch
                ? supplierRepository.searchByName(search, pageable)
                : supplierRepository.findAll(pageable);
        return PageResponseDTO.from(result.map(this::toResponseDTO));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponseDTO getSupplierById(Integer id) {
        return supplierRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", id));
    }

    @Override
    @Transactional
    public SupplierResponseDTO createSupplier(SupplierRequestDTO dto) {
        if (dto.email() != null && !dto.email().isBlank()) {
            supplierRepository.findByEmail(dto.email()).ifPresent(s -> {
                throw new BusinessException("Un fournisseur avec cet email existe déjà");
            });
        }
        Supplier saved = supplierRepository.save(toEntity(dto));
        return toResponseDTO(saved);
    }

    @Override
    @Transactional
    public SupplierResponseDTO updateSupplier(Integer id, SupplierRequestDTO dto) {
        Supplier existing = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur", id));

        if (dto.email() != null && !dto.email().isBlank()) {
            supplierRepository.findByEmail(dto.email())
                    .filter(s -> !s.getId().equals(id))
                    .ifPresent(s -> { throw new BusinessException("Un fournisseur avec cet email existe déjà"); });
        }

        existing.setName(dto.name());
        existing.setContactName(dto.contactName());
        existing.setEmail(dto.email());
        existing.setPhone(dto.phone());
        existing.setAddress(dto.address());

        return toResponseDTO(supplierRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteSupplier(Integer id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fournisseur", id);
        }
        supplierRepository.deleteById(id);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Supplier toEntity(SupplierRequestDTO dto) {
        return Supplier.builder()
                .name(dto.name())
                .contactName(dto.contactName())
                .email(dto.email())
                .phone(dto.phone())
                .address(dto.address())
                .build();
    }

    private SupplierResponseDTO toResponseDTO(Supplier s) {
        return new SupplierResponseDTO(
                s.getId(),
                s.getName(),
                s.getContactName(),
                s.getEmail(),
                s.getPhone(),
                s.getAddress(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
