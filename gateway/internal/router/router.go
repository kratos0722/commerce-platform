// Package router builds the commerce-platform API gateway: it reverse-proxies
// to the catalog and orders services and exposes an aggregated storefront view.
package router

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/http/httputil"
	"net/url"
	"time"
)

// Config holds the upstream service base URLs.
type Config struct {
	CatalogURL string
	OrdersURL  string
}

// New returns the gateway HTTP handler wiring together the upstreams.
func New(cfg Config) (http.Handler, error) {
	catalog, err := url.Parse(cfg.CatalogURL)
	if err != nil {
		return nil, fmt.Errorf("invalid catalog url: %w", err)
	}
	orders, err := url.Parse(cfg.OrdersURL)
	if err != nil {
		return nil, fmt.Errorf("invalid orders url: %w", err)
	}

	mux := http.NewServeMux()
	// Transparent reverse proxies: the path is preserved so each service owns
	// its own route namespace.
	mux.Handle("/api/catalog/", httputil.NewSingleHostReverseProxy(catalog))
	mux.Handle("/api/orders/", httputil.NewSingleHostReverseProxy(orders))
	mux.HandleFunc("/api/storefront/", storefront(cfg.CatalogURL))
	mux.HandleFunc("/health", func(w http.ResponseWriter, _ *http.Request) {
		writeJSON(w, http.StatusOK, map[string]string{"status": "UP"})
	})
	return mux, nil
}

// storefront aggregates data from downstream services into one response. Here
// it enriches a catalog product lookup; this is the BFF seam where more
// upstreams (pricing, reviews, inventory) would be composed.
func storefront(catalogURL string) http.HandlerFunc {
	client := &http.Client{Timeout: 5 * time.Second}
	return func(w http.ResponseWriter, r *http.Request) {
		sku := r.PathValue("sku")
		if sku == "" {
			// Fallback for older mux path handling.
			sku = r.URL.Path[len("/api/storefront/"):]
		}
		if sku == "" {
			writeError(w, http.StatusBadRequest, "sku is required")
			return
		}

		resp, err := client.Get(catalogURL + "/api/catalog/products/" + url.PathEscape(sku))
		if err != nil {
			writeError(w, http.StatusBadGateway, "catalog service unreachable")
			return
		}
		defer resp.Body.Close()

		if resp.StatusCode == http.StatusNotFound {
			writeError(w, http.StatusNotFound, "product not found: "+sku)
			return
		}
		if resp.StatusCode != http.StatusOK {
			writeError(w, http.StatusBadGateway, "catalog service error")
			return
		}

		var product map[string]any
		if err := json.NewDecoder(resp.Body).Decode(&product); err != nil {
			writeError(w, http.StatusBadGateway, "invalid catalog response")
			return
		}
		writeJSON(w, http.StatusOK, map[string]any{
			"product": product,
			"servedBy": "gateway",
		})
	}
}

func writeJSON(w http.ResponseWriter, status int, body any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(body)
}

func writeError(w http.ResponseWriter, status int, detail string) {
	w.Header().Set("Content-Type", "application/problem+json")
	w.WriteHeader(status)
	_, _ = io.WriteString(w, fmt.Sprintf(`{"status":%d,"detail":%q}`, status, detail))
}
