"""FastAPI application wiring routes, dependencies and error handling."""

from __future__ import annotations

from fastapi import Depends, FastAPI, HTTPException

from .catalog_client import CatalogClient, HttpCatalogClient
from .config import settings
from .errors import CatalogUnavailable, InsufficientStock, ProductNotFound
from .models import CreateOrderRequest, Order
from .service import OrderStore, OrdersService

# A single process-wide order store. Swappable for a database-backed store.
store = OrderStore()


def get_catalog_client() -> CatalogClient:
    """Dependency: the catalog client. Overridden in tests with a fake."""
    return HttpCatalogClient(settings.catalog_url)


def get_service(catalog: CatalogClient = Depends(get_catalog_client)) -> OrdersService:
    return OrdersService(catalog, store)


app = FastAPI(title="orders-service", version="0.1.0")


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post("/api/orders", response_model=Order, status_code=201)
def place_order(request: CreateOrderRequest,
                service: OrdersService = Depends(get_service)) -> Order:
    try:
        return service.place(request)
    except ProductNotFound as exc:
        raise HTTPException(status_code=404, detail=str(exc)) from exc
    except InsufficientStock as exc:
        raise HTTPException(status_code=409, detail=str(exc)) from exc
    except CatalogUnavailable as exc:
        raise HTTPException(status_code=502, detail=str(exc)) from exc


@app.get("/api/orders/{order_id}", response_model=Order)
def get_order(order_id: str, service: OrdersService = Depends(get_service)) -> Order:
    order = service.get(order_id)
    if order is None:
        raise HTTPException(status_code=404, detail="order not found")
    return order
