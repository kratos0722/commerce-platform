"""Orders service.

Places orders by calling the catalog service to validate each SKU, look up its
price, and check stock — a real service-to-service dependency within the
commerce platform.
"""

__version__ = "0.1.0"
