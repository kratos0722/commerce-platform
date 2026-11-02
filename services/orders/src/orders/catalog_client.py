"""HTTP client for the catalog service."""

from __future__ import annotations

from typing import Protocol

import httpx

from .errors import CatalogUnavailable


class CatalogClient(Protocol):
    """Port the orders service depends on; real HTTP in prod, a fake in tests."""

    def get_product(self, sku: str) -> dict | None:
        ...


class HttpCatalogClient:
    def __init__(self, base_url: str, timeout: float = 5.0) -> None:
        self._base_url = base_url.rstrip("/")
        self._timeout = timeout

    def get_product(self, sku: str) -> dict | None:
        url = f"{self._base_url}/api/catalog/products/{sku}"
        try:
            resp = httpx.get(url, timeout=self._timeout)
        except httpx.HTTPError as exc:  # pragma: no cover - network failure path
            raise CatalogUnavailable(str(exc)) from exc
        if resp.status_code == 404:
            return None
        if resp.status_code != 200:
            raise CatalogUnavailable(f"catalog returned {resp.status_code}")
        return resp.json()
