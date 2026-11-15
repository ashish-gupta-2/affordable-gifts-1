package com.affordablegifts.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogApiTest {

    private static final String KEY = "X-Admin-Key";
    private static final String GOOD_KEY = "test-admin-key";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.update("delete from product_packs");
        jdbc.update("delete from products");
        jdbc.update("delete from packaging_options");
    }

    private static MockHttpServletRequestBuilder admin(MockHttpServletRequestBuilder request, String body) {
        return request.header(KEY, GOOD_KEY).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static final String PISTACHIOS = """
            {"id":"pistachios","name":"Pistachios","category":"Dry Fruits","emoji":"P","displayOrder":2,
             "packs":[{"weight":200,"price":300},{"weight":100,"price":160.50}]}""";

    // ---------------------------------------------------------------- public

    @Test
    void publicProductsAreEmptyWhenNothingIsConfigured() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void publicProductsMatchStorefrontShapeWithPacksSortedBySize() throws Exception {
        mvc.perform(admin(post("/api/admin/products"), PISTACHIOS)).andExpect(status().isCreated());

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("pistachios"))
                .andExpect(jsonPath("$[0].category").value("Dry Fruits"))
                .andExpect(jsonPath("$[0].packs[0].weight").value(100))
                .andExpect(jsonPath("$[0].packs[0].price").value(160.50))
                .andExpect(jsonPath("$[0].packs[1].weight").value(200))
                // internal fields never reach the storefront
                .andExpect(jsonPath("$[0].active").doesNotExist())
                .andExpect(jsonPath("$[0].displayOrder").doesNotExist());
    }

    @Test
    void publicListFollowsDisplayOrderAndHidesInactiveItems() throws Exception {
        mvc.perform(admin(post("/api/admin/products"), PISTACHIOS)); // order 2
        mvc.perform(admin(post("/api/admin/products"), """
                {"id":"walnuts","name":"Walnuts","category":"Dry Fruits","emoji":"W","displayOrder":1,
                 "packs":[{"weight":100,"price":90}]}"""));
        mvc.perform(admin(post("/api/admin/products"), """
                {"id":"chana","name":"Chana","category":"Snacks","emoji":"C","displayOrder":3,
                 "packs":[{"weight":100,"price":30}]}"""));
        mvc.perform(admin(delete("/api/admin/products/chana"), "")).andExpect(status().isNoContent());

        mvc.perform(get("/api/products"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("walnuts"))
                .andExpect(jsonPath("$[1].id").value("pistachios"));
    }

    @Test
    void publicPackagingMatchesStorefrontShape() throws Exception {
        mvc.perform(admin(post("/api/admin/packaging"), """
                {"id":"box-3","name":"3 Partition Box","kind":"box","description":"3 x 100g",
                 "price":45,"slotCount":3,"slotCapacity":100,"displayOrder":1}""")).andExpect(status().isCreated());

        mvc.perform(get("/api/packaging"))
                .andExpect(jsonPath("$[0].kind").value("box"))
                .andExpect(jsonPath("$[0].slotCount").value(3))
                .andExpect(jsonPath("$[0].slotCapacity").value(100))
                .andExpect(jsonPath("$[0].price").value(45))
                .andExpect(jsonPath("$[0].active").doesNotExist());
    }

    // ------------------------------------------------------------------ auth

    @Test
    void adminEndpointsRejectMissingAndWrongKeys() throws Exception {
        mvc.perform(get("/api/admin/products")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/products").header(KEY, "wrong")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content(PISTACHIOS))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/packaging")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/admin/packaging/box-2")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectedWritesDoNotChangeData() throws Exception {
        mvc.perform(post("/api/admin/products").contentType(MediaType.APPLICATION_JSON).content(PISTACHIOS))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/products")).andExpect(content().json("[]"));
    }

    // ------------------------------------------------------ product writes

    @Test
    void createProductThenUpdatePriceAndPacks() throws Exception {
        mvc.perform(admin(post("/api/admin/products"), PISTACHIOS))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true));

        mvc.perform(admin(put("/api/admin/products/pistachios"), """
                {"name":"Pistachios","category":"Dry Fruits","emoji":"P","displayOrder":2,"active":true,
                 "packs":[{"weight":100,"price":170}]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packs.length()").value(1))
                .andExpect(jsonPath("$.packs[0].price").value(170));

        mvc.perform(get("/api/products")).andExpect(jsonPath("$[0].packs.length()").value(1));
    }

    @Test
    void deactivateIsASoftDeleteAndCanBeReversedByUpdate() throws Exception {
        mvc.perform(admin(post("/api/admin/products"), PISTACHIOS));
        mvc.perform(admin(delete("/api/admin/products/pistachios"), "")).andExpect(status().isNoContent());

        mvc.perform(get("/api/products")).andExpect(content().json("[]"));
        mvc.perform(admin(get("/api/admin/products"), ""))
                .andExpect(jsonPath("$[0].id").value("pistachios"))
                .andExpect(jsonPath("$[0].active").value(false));

        mvc.perform(admin(put("/api/admin/products/pistachios"), """
                {"name":"Pistachios","category":"Dry Fruits","emoji":"P","active":true,
                 "packs":[{"weight":100,"price":160}]}""")).andExpect(status().isOk());
        mvc.perform(get("/api/products")).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void duplicateProductIdIsAConflict() throws Exception {
        mvc.perform(admin(post("/api/admin/products"), PISTACHIOS)).andExpect(status().isCreated());
        mvc.perform(admin(post("/api/admin/products"), PISTACHIOS)).andExpect(status().isConflict());
    }

    @Test
    void unknownProductIsNotFound() throws Exception {
        mvc.perform(admin(delete("/api/admin/products/ghost"), "")).andExpect(status().isNotFound());
        mvc.perform(admin(put("/api/admin/products/ghost"), """
                {"name":"G","category":"Snacks","emoji":"g","active":true,
                 "packs":[{"weight":100,"price":1}]}""")).andExpect(status().isNotFound());
    }

    @Test
    void invalidProductPayloadsAreRejectedWithFieldMessages() throws Exception {
        // bad id, blank name, zero weight and negative price all reported together
        mvc.perform(admin(post("/api/admin/products"), """
                {"id":"Bad Id","name":"","category":"Snacks","emoji":"x",
                 "packs":[{"weight":0,"price":-5}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.allOf(
                        org.hamcrest.Matchers.containsString("id:"),
                        org.hamcrest.Matchers.containsString("name:"),
                        org.hamcrest.Matchers.containsString("packs[0].weight:"),
                        org.hamcrest.Matchers.containsString("packs[0].price:"))));

        // no packs at all
        mvc.perform(admin(post("/api/admin/products"), """
                {"id":"x","name":"X","category":"Snacks","emoji":"x","packs":[]}"""))
                .andExpect(status().isBadRequest());

        // three decimal places
        mvc.perform(admin(post("/api/admin/products"), """
                {"id":"x","name":"X","category":"Snacks","emoji":"x","packs":[{"weight":100,"price":1.234}]}"""))
                .andExpect(status().isBadRequest());

        // nothing was stored by any of the failed attempts
        mvc.perform(admin(get("/api/admin/products"), "")).andExpect(content().json("[]"));
    }

    @Test
    void unknownCategoryAndDuplicatePackSizesAreRejected() throws Exception {
        mvc.perform(admin(post("/api/admin/products"), """
                {"id":"a","name":"A","category":"Candy","emoji":"x","packs":[{"weight":100,"price":1}]}"""))
                .andExpect(status().isBadRequest());

        mvc.perform(admin(post("/api/admin/products"), """
                {"id":"a","name":"A","category":"Snacks","emoji":"x",
                 "packs":[{"weight":100,"price":1},{"weight":100,"price":2}]}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Duplicate pack size 100g"));
    }

    // ---------------------------------------------------- packaging writes

    @Test
    void jarSetsMustBeTwoByTwoHundredOrThreeByHundred() throws Exception {
        String jar = """
                {"id":"%s","name":"Jar","kind":"jar","description":"d","price":50,
                 "slotCount":%d,"slotCapacity":%d}""";

        mvc.perform(admin(post("/api/admin/packaging"), jar.formatted("jar-200", 2, 200)))
                .andExpect(status().isCreated());
        mvc.perform(admin(post("/api/admin/packaging"), jar.formatted("jar-100", 3, 100)))
                .andExpect(status().isCreated());

        mvc.perform(admin(post("/api/admin/packaging"), jar.formatted("jar-bad", 2, 300)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A jar set must be 2 x 200g jars or 3 x 100g jars"));
        mvc.perform(admin(post("/api/admin/packaging"), jar.formatted("jar-bad2", 4, 100)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void boxesMayHaveAnyPartitionCountAndCanBeUpdatedAndDeactivated() throws Exception {
        mvc.perform(admin(post("/api/admin/packaging"), """
                {"id":"box-5","name":"5 Partition Box","kind":"box","description":"5 x 100g",
                 "price":75,"slotCount":5,"slotCapacity":100}""")).andExpect(status().isCreated());

        mvc.perform(admin(put("/api/admin/packaging/box-5"), """
                {"name":"5 Partition Box","kind":"box","description":"5 x 100g",
                 "price":80,"slotCount":5,"slotCapacity":100,"active":true}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(80));

        mvc.perform(admin(delete("/api/admin/packaging/box-5"), "")).andExpect(status().isNoContent());
        mvc.perform(get("/api/packaging")).andExpect(content().json("[]"));
    }

    @Test
    void invalidPackagingPayloadsAreRejected() throws Exception {
        mvc.perform(admin(post("/api/admin/packaging"), """
                {"id":"b","name":"B","kind":"bag","description":"d","price":5,"slotCount":1,"slotCapacity":100}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(admin(post("/api/admin/packaging"), """
                {"id":"b","name":"B","kind":"box","description":"d","price":-1,"slotCount":0,"slotCapacity":100}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(admin(post("/api/admin/packaging"), """
                {"id":"b","name":"B","kind":"box","description":"d","price":5,"slotCapacity":100}"""))
                .andExpect(status().isBadRequest());
        mvc.perform(admin(get("/api/admin/packaging"), "")).andExpect(content().json("[]"));
    }
}
