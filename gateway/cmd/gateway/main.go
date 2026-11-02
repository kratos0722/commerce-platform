// Command gateway runs the commerce-platform API gateway.
package main

import (
	"log"
	"net/http"
	"os"

	"github.com/kratos0722/commerce-platform/gateway/internal/router"
)

func main() {
	h, err := router.New(router.Config{
		CatalogURL: env("CATALOG_URL", "http://localhost:8080"),
		OrdersURL:  env("ORDERS_URL", "http://localhost:8000"),
	})
	if err != nil {
		log.Fatalf("gateway: %v", err)
	}

	addr := ":" + env("PORT", "8090")
	log.Printf("gateway listening on %s", addr)
	log.Fatal(http.ListenAndServe(addr, h))
}

func env(key, def string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return def
}
