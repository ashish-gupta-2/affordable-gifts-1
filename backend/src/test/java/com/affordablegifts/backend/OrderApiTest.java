package com.affordablegifts.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void seedCatalogue() {
        jdbc.update("delete from order_items");
        jdbc.update("delete from orders");
        jdbc.update("delete from product_packs");
        jdbc.update("delete from products");
        jdbc.update("delete from packaging_options");

        // box-2: two 100g partitions, Rs 30. almonds 100g = 110, cashews 100g = 130.
        jdbc.update("insert into packaging_options (id,name,kind,description,price,slot_count,slot_capacity)"
                + " values ('box-2','2 Partition Box','box','2 x 100g',30.00,2,100)");
        jdbc.update("insert into packaging_options (id,name,kind,description,price,slot_count,slot_capacity,active)"
                + " values ('box-old','Old Box','box','retired',10.00,2,100,false)");
        insertProduct("almonds", "Almonds", true, 110.00, 210.00);
        insertProduct("cashews", "Cashews", true, 130.00, 250.00);
        insertProduct("retired", "Retired", false, 50.00, 90.00);
        // only sold in 200g
        jdbc.update("insert into products (id,name,category,emoji) values ('bulk','Bulk Mix','Snacks','B')");
        jdbc.update("insert into product_packs (product_id,weight_grams,price) values ('bulk',200,99.00)");
    }

    private void insertProduct(String id, String name, boolean active, double p100, double p200) {
        jdbc.update("insert into products (id,name,category,emoji,active) values (?,?,?,?,?)",
                id, name, "Dry Fruits", "X", active);
        jdbc.update("insert into product_packs (product_id,weight_grams,price) values (?,100,?)", id, p100);
        jdbc.update("insert into product_packs (product_id,weight_grams,price) values (?,200,?)", id, p200);
    }

    private static String order(String requestId, String packagingId, int quantity,
                                String productIdsJson, String pincode) {
        return """
                {"requestId":"%s","packagingId":"%s","quantity":%d,"productIds":%s,
                 "customer":{"name":"Asha Rao","phone":"9876543210","email":""},
                 "address":{"house":"12","street":"MG Road","area":"Indiranagar","city":"Bengaluru",
                            "state":"Karnataka","pincode":"%s","instructions":""}}"""
                .formatted(requestId, packagingId, quantity, productIdsJson, pincode);
    }

    private org.springframework.test.web.servlet.ResultActions place(String body) throws Exception {
        return mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private int orderCount() {
        return jdbc.queryForObject("select count(*) from orders", Integer.class);
    }

    // --------------------------------------------------------- happy path

    @Test
    void placesOrderPricedByServerWithSnapshotAndLocalDelivery() throws Exception {
        // products 240, x2 sets = 480; packaging 30 x2 = 60; local delivery 50 => 590
        place(order("r1", "box-2", 2, "[\"almonds\",\"cashews\"]", "560001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("AG-" + jdbc.queryForObject(
                        "select last_value from order_number_seq", Long.class)))
                .andExpect(jsonPath("$.productsSubtotal").value(480.00))
                .andExpect(jsonPath("$.packagingTotal").value(60.00))
                .andExpect(jsonPath("$.deliveryCharge").value(50.00))
                .andExpect(jsonPath("$.total").value(590.00))
                .andExpect(jsonPath("$.totalWeightGrams").value(400))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.status").value("ORDER_RECEIVED"))
                .andExpect(jsonPath("$.items[0].productName").value("Almonds"))
                .andExpect(jsonPath("$.items[1].unitPrice").value(130.00));

        // Changing the catalogue afterwards must not change the stored order.
        jdbc.update("update product_packs set price = 999 where product_id = 'almonds'");
        jdbc.update("update products set name = 'Renamed' where id = 'almonds'");
        assertEquals(110.00, jdbc.queryForObject(
                "select unit_price from order_items where product_id = 'almonds'", Double.class));
        assertEquals("Almonds", jdbc.queryForObject(
                "select product_name from order_items where product_id = 'almonds'", String.class));
    }

    @Test
    void chargesIndiaWideDeliveryOutsideBengaluru() throws Exception {
        place(order("r2", "box-2", 2, "[\"almonds\",\"cashews\"]", "110001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deliveryCharge").value(110.00))
                .andExpect(jsonPath("$.total").value(650.00)); // 480 + 60 + 110
    }

    @Test
    void ignoresAnyPricesSentByTheBrowser() throws Exception {
        String sneaky = order("r3", "box-2", 2, "[\"almonds\",\"cashews\"]", "560001")
                .replaceFirst("\\{", "{\"total\":1,\"productsSubtotal\":1,\"price\":1,");
        place(sneaky).andExpect(status().isCreated()).andExpect(jsonPath("$.total").value(590.00));
    }

    // --------------------------------------------------------- idempotency

    @Test
    void retryingTheSameRequestIdReturnsTheOriginalOrder() throws Exception {
        String body = order("same-attempt", "box-2", 2, "[\"almonds\",\"cashews\"]", "560001");
        String first = place(body).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String second = place(body).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertEquals(first, second);
        assertEquals(1, orderCount());
    }

    // --------------------------------------------------------- rules

    @Test
    void rejectsOrderBelowMinimumProductValue() throws Exception {
        jdbc.update("update store_settings set min_order_value = 500");
        try {
            place(order("r4", "box-2", 1, "[\"almonds\",\"cashews\"]", "560001"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("at least Rs 500")));
            assertEquals(0, orderCount());
        } finally {
            jdbc.update("update store_settings set min_order_value = 299");
        }
    }

    @Test
    void minimumIsCheckedAgainstQuantityTimesSet() throws Exception {
        jdbc.update("update store_settings set min_order_value = 400");
        try {
            place(order("r5", "box-2", 1, "[\"almonds\",\"cashews\"]", "560001"))
                    .andExpect(status().isBadRequest());
            place(order("r6", "box-2", 2, "[\"almonds\",\"cashews\"]", "560001"))
                    .andExpect(status().isCreated());
        } finally {
            jdbc.update("update store_settings set min_order_value = 299");
        }
    }

    @Test
    void rejectsWrongNumberOfProducts() throws Exception {
        place(order("r7", "box-2", 1, "[\"almonds\"]", "560001")).andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownOrInactiveProductsAndPackaging() throws Exception {
        place(order("r8", "box-2", 1, "[\"almonds\",\"nope\"]", "560001")).andExpect(status().isBadRequest());
        place(order("r9", "box-2", 1, "[\"almonds\",\"retired\"]", "560001")).andExpect(status().isBadRequest());
        place(order("r10", "box-old", 1, "[\"almonds\",\"cashews\"]", "560001")).andExpect(status().isNotFound());
        place(order("r11", "missing", 1, "[\"almonds\",\"cashews\"]", "560001")).andExpect(status().isNotFound());
        assertEquals(0, orderCount());
    }

    @Test
    void rejectsProductWithoutThePackSizeOfTheSlot() throws Exception {
        place(order("r12", "box-2", 1, "[\"almonds\",\"bulk\"]", "560001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("no 100g pack")));
    }

    @Test
    void validatesCustomerAndAddress() throws Exception {
        String base = order("r13", "box-2", 1, "[\"almonds\",\"cashews\"]", "560001");
        place(base.replace("9876543210", "12345")).andExpect(status().isBadRequest());
        place(base.replace("\"email\":\"\"", "\"email\":\"not-an-email\"")).andExpect(status().isBadRequest());
        place(base.replace("560001", "56001")).andExpect(status().isBadRequest());
        place(base.replace("\"quantity\":1", "\"quantity\":0")).andExpect(status().isBadRequest());
        place(base.replace("\"quantity\":1", "\"quantity\":-3")).andExpect(status().isBadRequest());
        place(base.replace("Asha Rao", "")).andExpect(status().isBadRequest());
        assertEquals(0, orderCount());
    }

    // --------------------------------------------------------- settings + quote

    @Test
    void exposesMinimumOrderAndDeliveryQuotes() throws Exception {
        mvc.perform(get("/api/settings")).andExpect(status().isOk())
                .andExpect(jsonPath("$.minOrderValue").value(299.00));
        mvc.perform(get("/api/delivery-quote").param("pincode", "560034"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zone").value("local"))
                .andExpect(jsonPath("$.charge").value(50.00));
        mvc.perform(get("/api/delivery-quote").param("pincode", "400001"))
                .andExpect(jsonPath("$.zone").value("india"))
                .andExpect(jsonPath("$.charge").value(110.00));
        mvc.perform(get("/api/delivery-quote").param("pincode", "abc")).andExpect(status().isBadRequest());
    }
}
