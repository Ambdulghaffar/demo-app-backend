package com.elhaffar.exoformbackend.services.impl;

import com.elhaffar.exoformbackend.common.utils.SortUtils;
import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.product.ProductRequestDTO;
import com.elhaffar.exoformbackend.dto.product.ProductResponseDTO;
import com.elhaffar.exoformbackend.entities.Category;
import com.elhaffar.exoformbackend.entities.Product;
import com.elhaffar.exoformbackend.common.enums.ProductStatus;
import com.elhaffar.exoformbackend.exceptions.ResourceNotFoundException;
import com.elhaffar.exoformbackend.mapper.ProductMapper;
import com.elhaffar.exoformbackend.repository.CategoryRepository;
import com.elhaffar.exoformbackend.repository.ProductRepository;
import com.elhaffar.exoformbackend.services.ProductService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    public PageResponseDTO<ProductResponseDTO> getAllProducts(
            int page, int size, String sortBy, String sortDir,
            String status, Integer categoryId, String search,
            BigDecimal minPrice, BigDecimal maxPrice, boolean excludeInactive) {

        Pageable pageable = PageRequest.of(page, size, SortUtils.buildSort(sortBy, sortDir));

        boolean hasSearch     = search     != null && !search.isBlank();
        boolean hasStatus     = status     != null && !status.isBlank() && !status.equalsIgnoreCase("all");
        boolean hasCategoryId = categoryId != null;
        boolean hasNewFilters = minPrice != null || maxPrice != null || excludeInactive;

        Page<Product> result;

        if (hasSearch) {
            // Recherche texte — priorité sur les autres filtres
            result = productRepository.searchProducts(search, pageable);
        } else if (hasNewFilters) {
            // Filtre prix et/ou exclusion INACTIVE — requête JPQL flexible
            ProductStatus statusFilter = null;
            if (hasStatus) {
                try {
                    statusFilter = ProductStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }
            ProductStatus excludedStatus = excludeInactive ? ProductStatus.INACTIVE : null;
            result = productRepository.findWithFilters(
                    statusFilter,
                    hasCategoryId ? categoryId : null,
                    minPrice,
                    maxPrice,
                    excludedStatus,
                    pageable);
        } else if (hasStatus && hasCategoryId) {
            try {
                ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
                result = productRepository.findByStatusAndCategoryId(productStatus, categoryId, pageable);
            } catch (IllegalArgumentException e) {
                result = productRepository.findByCategoryId(categoryId, pageable);
            }
        } else if (hasStatus) {
            try {
                ProductStatus productStatus = ProductStatus.valueOf(status.toUpperCase());
                result = productRepository.findByStatus(productStatus, pageable);
            } catch (IllegalArgumentException e) {
                result = productRepository.findAll(pageable);
            }
        } else if (hasCategoryId) {
            result = productRepository.findByCategoryId(categoryId, pageable);
        } else {
            result = productRepository.findAll(pageable);
        }

        return PageResponseDTO.from(result.map(productMapper::toResponseDTO));
    }

    @Override
    public ProductResponseDTO getProductById(Integer id) {
        return productRepository.findById(id)
                .map(productMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));
    }

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO dto) {
        // Vérifie que la catégorie existe
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", dto.categoryId()));

        Product product = productMapper.toEntity(dto);
        product.setCategory(category); // résolution manuelle de la relation

        return productMapper.toResponseDTO(productRepository.save(product));
    }

    @Override
    public ProductResponseDTO updateProduct(Integer id, ProductRequestDTO dto) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));

        // Vérifie que la nouvelle catégorie existe
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie", dto.categoryId()));

        productMapper.updateProductFromDto(dto, existing);
        existing.setCategory(category);

        return productMapper.toResponseDTO(productRepository.save(existing));
    }

    @Override
    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produit", id);
        }
        productRepository.deleteById(id);
    }
}
