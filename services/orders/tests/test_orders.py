from fastapi.testclient import TestClient

from orders.app import app, get_catalog_client, store
from orders.service import OrderStore

_CATALOG = {
    "SKU-1": {"sku": "SKU-1", "name": "Widget", "price": 10.0, "stock": 5},
    "SKU-2": {"sku": "SKU-2", "name": "Gadget", "price": 2.5, "stock": 100},
}


class FakeCatalog:
    def get_product(self, sku: str):
        return _CATALOG.get(sku)


def _override_catalog():
    return FakeCatalog()


app.dependency_overrides[get_catalog_client] = _override_catalog
client = TestClient(app)


def setup_function():
    # Fresh store per test so ids/totals don't leak between tests.
    store.__dict__.update(OrderStore().__dict__)


def test_place_order_computes_total_from_catalog():
    resp = client.post("/api/orders", json={"items": [
        {"sku": "SKU-1", "quantity": 2},
        {"sku": "SKU-2", "quantity": 4},
    ]})
    assert resp.status_code == 201
    body = resp.json()
    assert body["status"] == "CREATED"
    assert body["total"] == 30.0  # 2*10 + 4*2.5
    assert body["lines"][0]["name"] == "Widget"


def test_unknown_sku_returns_404():
    resp = client.post("/api/orders", json={"items": [{"sku": "NOPE", "quantity": 1}]})
    assert resp.status_code == 404


def test_insufficient_stock_returns_409():
    resp = client.post("/api/orders", json={"items": [{"sku": "SKU-1", "quantity": 99}]})
    assert resp.status_code == 409


def test_empty_items_is_422():
    resp = client.post("/api/orders", json={"items": []})
    assert resp.status_code == 422


def test_get_order_round_trips():
    created = client.post("/api/orders", json={"items": [{"sku": "SKU-1", "quantity": 1}]}).json()
    fetched = client.get(f"/api/orders/{created['id']}")
    assert fetched.status_code == 200
    assert fetched.json()["id"] == created["id"]


def test_get_unknown_order_404():
    assert client.get("/api/orders/does-not-exist").status_code == 404
