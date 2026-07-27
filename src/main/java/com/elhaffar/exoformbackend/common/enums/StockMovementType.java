package com.elhaffar.exoformbackend.common.enums;

public enum StockMovementType {
    SALE,        // décrémentation suite à une commande
    CANCELLATION, // restitution suite à une annulation de commande
    RESTOCK,     // réassort manuel (ADMIN + MANAGER)
    DAMAGE       // casse/perte manuelle (ADMIN uniquement)
}
