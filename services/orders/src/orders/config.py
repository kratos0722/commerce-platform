"""Runtime configuration from the environment."""

from __future__ import annotations

import os


class Settings:
    def __init__(self) -> None:
        self.catalog_url = os.getenv("CATALOG_URL", "http://localhost:8080")
        self.port = int(os.getenv("PORT", "8000"))


settings = Settings()
