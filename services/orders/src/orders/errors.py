"""Domain errors for the orders service."""

from __future__ import annotations


class OrdersError(Exception):
    """Base class for orders domain errors."""


class ProductNotFound(OrdersError):
    def __init__(self, sku: str) -> None:
        super().__init__(f"Unknown product SKU '{sku}'")
        self.sku = sku


class InsufficientStock(OrdersError):
    def __init__(self, sku: str, requested: int, available: int) -> None:
        super().__init__(
            f"Insufficient stock for '{sku}': requested {requested}, available {available}"
        )
        self.sku = sku


class CatalogUnavailable(OrdersError):
    """Raised when the catalog service cannot be reached."""
