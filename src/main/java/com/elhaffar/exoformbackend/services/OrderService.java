package com.elhaffar.exoformbackend.services;

import com.elhaffar.exoformbackend.dto.common.PageResponseDTO;
import com.elhaffar.exoformbackend.dto.order.OrderRequestDTO;
import com.elhaffar.exoformbackend.dto.order.OrderResponseDTO;
import com.elhaffar.exoformbackend.dto.order.UpdateOrderStatusDTO;

public interface OrderService {

    PageResponseDTO<OrderResponseDTO> getMyOrders(String email, int page, int size);

    OrderResponseDTO getMyOrderById(String email, Integer orderId);

    PageResponseDTO<OrderResponseDTO> getAllOrders(int page, int size, String status);

    OrderResponseDTO getOrderById(Integer orderId);

    OrderResponseDTO createOrder(String email, OrderRequestDTO dto);

    OrderResponseDTO updateOrderStatus(Integer orderId, UpdateOrderStatusDTO dto);
}
