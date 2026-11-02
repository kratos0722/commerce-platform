package router

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestHealth(t *testing.T) {
	h, err := New(Config{CatalogURL: "http://catalog", OrdersURL: "http://orders"})
	if err != nil {
		t.Fatalf("New: %v", err)
	}
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/health", nil))
	if rec.Code != http.StatusOK {
		t.Fatalf("health status = %d, want 200", rec.Code)
	}
}

func TestCatalogProxyForwardsPath(t *testing.T) {
	var gotPath string
	catalog := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		gotPath = r.URL.Path
		w.WriteHeader(http.StatusOK)
	}))
	defer catalog.Close()

	h, _ := New(Config{CatalogURL: catalog.URL, OrdersURL: "http://orders"})
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/api/catalog/products/ABC", nil))

	if rec.Code != http.StatusOK {
		t.Fatalf("proxy status = %d, want 200", rec.Code)
	}
	if gotPath != "/api/catalog/products/ABC" {
		t.Fatalf("catalog received path %q, want /api/catalog/products/ABC", gotPath)
	}
}

func TestStorefrontAggregatesCatalog(t *testing.T) {
	catalog := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != "/api/catalog/products/SKU-1" {
			http.NotFound(w, r)
			return
		}
		w.Header().Set("Content-Type", "application/json")
		_ = json.NewEncoder(w).Encode(map[string]any{"sku": "SKU-1", "name": "Widget", "price": 9.99})
	}))
	defer catalog.Close()

	h, _ := New(Config{CatalogURL: catalog.URL, OrdersURL: "http://orders"})
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/api/storefront/SKU-1", nil))

	if rec.Code != http.StatusOK {
		t.Fatalf("storefront status = %d, want 200", rec.Code)
	}
	var body struct {
		Product  map[string]any `json:"product"`
		ServedBy string         `json:"servedBy"`
	}
	if err := json.NewDecoder(rec.Body).Decode(&body); err != nil {
		t.Fatalf("decode: %v", err)
	}
	if body.ServedBy != "gateway" {
		t.Fatalf("servedBy = %q, want gateway", body.ServedBy)
	}
	if body.Product["sku"] != "SKU-1" {
		t.Fatalf("aggregated product sku = %v, want SKU-1", body.Product["sku"])
	}
}

func TestStorefrontMissingProductReturns404(t *testing.T) {
	catalog := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		http.NotFound(w, r)
	}))
	defer catalog.Close()

	h, _ := New(Config{CatalogURL: catalog.URL, OrdersURL: "http://orders"})
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, httptest.NewRequest(http.MethodGet, "/api/storefront/NOPE", nil))

	if rec.Code != http.StatusNotFound {
		t.Fatalf("status = %d, want 404", rec.Code)
	}
}
