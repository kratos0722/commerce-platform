package com.kratos0722.catalog.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CatalogApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    private void create(String sku) throws Exception {
        String body = """
            {"sku":"%s","name":"Widget","description":"d","price":9.99,"stock":10}
            """.formatted(sku);
        mvc.perform(post("/api/catalog/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @SuppressWarnings("unchecked")
    void create_thenGet_roundTrips() throws Exception {
        create("C-1");
        String res = mvc.perform(get("/api/catalog/products/C-1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Map<String, Object> node = json.readValue(res, Map.class);
        assertEquals("C-1", node.get("sku"));
        assertEquals(10, ((Number) node.get("stock")).intValue());
    }

    @Test
    void get_unknown_returns404() throws Exception {
        mvc.perform(get("/api/catalog/products/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void duplicate_returns409() throws Exception {
        create("C-2");
        String body = """
            {"sku":"C-2","name":"Widget","price":9.99,"stock":10}
            """;
        mvc.perform(post("/api/catalog/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidBody_returns400() throws Exception {
        String body = """
            {"sku":"","name":"","price":-1,"stock":-5}
            """;
        mvc.perform(post("/api/catalog/products").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
