package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.supplier.SupplierOrderRequestDTO;
import com.elhaffar.exoformbackend.dto.supplier.SupplierOrderResponseDTO;
import com.elhaffar.exoformbackend.dto.supplier.UpdateSupplierOrderStatusDTO;

public interface SupplierOrderService {

    PageResponseDTO<SupplierOrderResponseDTO> getAllSupplierOrders(int page, int size, String status);

    SupplierOrderResponseDTO getSupplierOrderById(Integer id);

    SupplierOrderResponseDTO createSupplierOrder(SupplierOrderRequestDTO dto);

    SupplierOrderResponseDTO updateSupplierOrderStatus(Integer id, UpdateSupplierOrderStatusDTO dto);
}
