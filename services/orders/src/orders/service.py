"""Order placement logic — validates each line against the catalog service."""

from __future__ import annotations

import uuid

from .catalog_client import CatalogClient
from .errors import InsufficientStock, ProductNotFound
from .models import CreateOrderRequest, Order, OrderLine


class OrderStore:
    """In-memory order store (a real deployment would persist these)."""

    def __init__(self) -> None:
        self._orders: dict[str, Order] = {}

    def add(self, order: Order) -> None:
        self._orders[order.id] = order

    def get(self, order_id: str) -> Order | None:
        return self._orders.get(order_id)


class OrdersService:
    def __init__(self, catalog: CatalogClient, store: OrderStore) -> None:
        self._catalog = catalog
        self._store = store

    def place(self, request: CreateOrderRequest) -> Order:
        lines: list[OrderLine] = []
        total = 0.0

        for item in request.items:
            product = self._catalog.get_product(item.sku)
            if product is None:
                raise ProductNotFound(item.sku)

            available = int(product["stock"])
            if available < item.quantity:
                raise InsufficientStock(item.sku, item.quantity, available)

            unit_price = float(product["price"])
            line_total = round(unit_price * item.quantity, 2)
            total += line_total
            lines.append(OrderLine(
                sku=item.sku,
                name=product["name"],
                unit_price=unit_price,
                quantity=item.quantity,
                line_total=line_total,
            ))

        order = Order(id=str(uuid.uuid4()), status="CREATED", total=round(total, 2), lines=lines)
        self._store.add(order)
        return order

    def get(self, order_id: str) -> Order | None:
        return self._store.get(order_id)
