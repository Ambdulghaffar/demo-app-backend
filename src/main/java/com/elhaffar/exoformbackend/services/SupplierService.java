package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.supplier.SupplierRequestDTO;
import com.elhaffar.exoformbackend.dto.supplier.SupplierResponseDTO;

public interface SupplierService {

    PageResponseDTO<SupplierResponseDTO> getAllSuppliers(int page, int size, String search);

    SupplierResponseDTO getSupplierById(Integer id);

    SupplierResponseDTO createSupplier(SupplierRequestDTO dto);

    SupplierResponseDTO updateSupplier(Integer id, SupplierRequestDTO dto);

    void deleteSupplier(Integer id);
}
