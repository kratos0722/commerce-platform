"""Request/response models for the orders service."""

from __future__ import annotations

from datetime import datetime, timezone

from pydantic import BaseModel, Field


class OrderItemRequest(BaseModel):
    sku: str = Field(min_length=1)
    quantity: int = Field(gt=0)


class CreateOrderRequest(BaseModel):
    items: list[OrderItemRequest] = Field(min_length=1)


class OrderLine(BaseModel):
    sku: str
    name: str
    unit_price: float
    quantity: int
    line_total: float


class Order(BaseModel):
    id: str
    status: str
    total: float
    lines: list[OrderLine]
    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
