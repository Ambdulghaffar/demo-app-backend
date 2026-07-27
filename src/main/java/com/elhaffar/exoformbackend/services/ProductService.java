package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.product.ProductRequestDTO;
import com.elhaffar.exoformbackend.dto.product.ProductResponseDTO;

import java.math.BigDecimal;

public interface ProductService {
    PageResponseDTO<ProductResponseDTO> getAllProducts(int page, int size, String sortBy, String sortDir, String status, Integer categoryId, String search, BigDecimal minPrice, BigDecimal maxPrice, boolean excludeInactive);
    ProductResponseDTO getProductById(Integer id);
    ProductResponseDTO createProduct(ProductRequestDTO dto);
    ProductResponseDTO updateProduct(Integer id, ProductRequestDTO dto);
    void deleteProduct(Integer id);
}
