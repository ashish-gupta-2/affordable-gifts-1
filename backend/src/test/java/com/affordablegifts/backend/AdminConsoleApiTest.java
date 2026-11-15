package com.affordablegifts.backend;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminConsoleApiTest {

    private static final String KEY = "X-Admin-Key";
    private static final String GOOD_KEY = "test-admin-key";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("delete from order_items");
        jdbc.update("delete from orders");
        jdbc.update("delete from product_packs");
        jdbc.update("delete from products");
        jdbc.update("delete from packaging_options");
        jdbc.update("update store_settings set min_order_value = 299, local_delivery_charge = 50,"
                + " india_delivery_charge = 110");

        jdbc.update("insert into packaging_options (id,name,kind,description,price,slot_count,slot_capacity)"
                + " values ('box-2','2 Partition Box','box','2 x 100g',30.00,2,100)");
        for (String[] p : new String[][] {{"almonds", "Almonds", "110"}, {"cashews", "Cashews", "130"}}) {
            jdbc.update("insert into products (id,name,category,emoji) values (?,?,?,?)",
                    p[0], p[1], "Dry Fruits", "X");
            jdbc.update("insert into product_packs (product_id,weight_grams,price) values (?,100,?)",
                    p[0], Double.parseDouble(p[2]));
        }
    }

    private static MockHttpServletRequestBuilder admin(MockHttpServletRequestBuilder request) {
        return request.header(KEY, GOOD_KEY);
    }

    private static MockHttpServletRequestBuilder adminJson(MockHttpServletRequestBuilder request, String body) {
        return admin(request).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private ResultActions placeOrder(String requestId) throws Exception {
        return mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"requestId":"%s","packagingId":"box-2","quantity":2,"productIds":["almonds","cashews"],
                 "customer":{"name":"Asha Rao","phone":"9876543210","email":"asha@example.com"},
                 "address":{"house":"12","street":"MG Road","area":"Indiranagar","city":"Bengaluru",
                            "state":"Karnataka","pincode":"560001","instructions":"Ring twice"}}"""
                .formatted(requestId)));
    }

    // ----------------------------------------------------------------- auth

    @Test
    void ordersAndSettingsRequireTheAdminKey() throws Exception {
        mvc.perform(get("/api/admin/orders")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/orders/AG-1").header(KEY, "wrong")).andExpect(status().isUnauthorized());
        mvc.perform(patch("/api/admin/orders/AG-1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/settings")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/admin/settings").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // --------------------------------------------------------------- orders

    @Test
    void listsOrdersNewestFirstAndFiltersByStatus() throws Exception {
        placeOrder("a1").andExpect(status().isCreated());
        placeOrder("a2").andExpect(status().isCreated());

        mvc.perform(admin(get("/api/admin/orders")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerName").value("Asha Rao"))
                .andExpect(jsonPath("$[0].total").value(590.00))
                .andExpect(jsonPath("$[0].status").value("ORDER_RECEIVED"));

        String number = jdbc.queryForObject(
                "select order_number from orders order by created_at asc limit 1", String.class);
        mvc.perform(adminJson(patch("/api/admin/orders/" + number), "{\"status\":\"PACKED\"}"))
                .andExpect(status().isOk());

        mvc.perform(admin(get("/api/admin/orders")).param("status", "PACKED"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].orderNumber").value(number));
        mvc.perform(admin(get("/api/admin/orders")).param("status", "DELIVERED"))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(admin(get("/api/admin/orders")).param("status", "NOPE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void showsFullOrderDetailIncludingContactAndInstructions() throws Exception {
        placeOrder("d1").andExpect(status().isCreated());
        String number = jdbc.queryForObject("select order_number from orders", String.class);

        mvc.perform(admin(get("/api/admin/orders/" + number)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.orderNumber").value(number))
                .andExpect(jsonPath("$.order.items.length()").value(2))
                .andExpect(jsonPath("$.order.addressLine").value(containsString("560001")))
                .andExpect(jsonPath("$.customerEmail").value("asha@example.com"))
                .andExpect(jsonPath("$.deliveryInstructions").value("Ring twice"));
        mvc.perform(admin(get("/api/admin/orders/AG-99999"))).andExpect(status().isNotFound());
    }

    @Test
    void movesAnOrderThroughStagesAndMarksItPaid() throws Exception {
        placeOrder("s1").andExpect(status().isCreated());
        String number = jdbc.queryForObject("select order_number from orders", String.class);

        mvc.perform(adminJson(patch("/api/admin/orders/" + number),
                        "{\"status\":\"PAYMENT_CONFIRMED\",\"paymentStatus\":\"PAID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.status").value("PAYMENT_CONFIRMED"))
                .andExpect(jsonPath("$.order.paymentStatus").value("PAID"));
        mvc.perform(adminJson(patch("/api/admin/orders/" + number), "{\"status\":\"DISPATCHED\"}"))
                .andExpect(jsonPath("$.order.status").value("DISPATCHED"))
                // untouched fields stay as they were
                .andExpect(jsonPath("$.order.paymentStatus").value("PAID"));
    }

    @Test
    void deliveredAndCancelledOrdersCannotChangeStatusAgain() throws Exception {
        placeOrder("c1").andExpect(status().isCreated());
        String number = jdbc.queryForObject("select order_number from orders", String.class);

        mvc.perform(adminJson(patch("/api/admin/orders/" + number), "{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk());
        mvc.perform(adminJson(patch("/api/admin/orders/" + number), "{\"status\":\"PACKED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("CANCELLED")));
    }

    @Test
    void rejectsEmptyOrInvalidOrderUpdates() throws Exception {
        placeOrder("v1").andExpect(status().isCreated());
        String number = jdbc.queryForObject("select order_number from orders", String.class);

        mvc.perform(adminJson(patch("/api/admin/orders/" + number), "{}")).andExpect(status().isBadRequest());
        mvc.perform(adminJson(patch("/api/admin/orders/" + number), "{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(adminJson(patch("/api/admin/orders/" + number), "{\"paymentStatus\":\"MAYBE\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(adminJson(patch("/api/admin/orders/AG-99999"), "{\"status\":\"PACKED\"}"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------- settings

    @Test
    void readsAndUpdatesSettingsAndCheckoutUsesTheNewValues() throws Exception {
        mvc.perform(admin(get("/api/admin/settings")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minOrderValue").value(299.00))
                .andExpect(jsonPath("$.localDeliveryCharge").value(50.00))
                .andExpect(jsonPath("$.indiaDeliveryCharge").value(110.00));

        mvc.perform(adminJson(put("/api/admin/settings"),
                        "{\"minOrderValue\":100,\"localDeliveryCharge\":40,\"indiaDeliveryCharge\":95.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localDeliveryCharge").value(40.00));

        // public endpoints and a real order pick up the change immediately
        mvc.perform(get("/api/settings")).andExpect(jsonPath("$.minOrderValue").value(100.00));
        mvc.perform(get("/api/delivery-quote").param("pincode", "400001"))
                .andExpect(jsonPath("$.charge").value(95.50));
        placeOrder("set1").andExpect(status().isCreated())
                .andExpect(jsonPath("$.deliveryCharge").value(40.00))
                .andExpect(jsonPath("$.total").value(580.00)); // 480 + 60 + 40
    }

    @Test
    void rejectsInvalidSettings() throws Exception {
        mvc.perform(adminJson(put("/api/admin/settings"),
                        "{\"minOrderValue\":-1,\"localDeliveryCharge\":40,\"indiaDeliveryCharge\":95}"))
                .andExpect(status().isBadRequest());
        mvc.perform(adminJson(put("/api/admin/settings"),
                        "{\"minOrderValue\":100,\"localDeliveryCharge\":40.123,\"indiaDeliveryCharge\":95}"))
                .andExpect(status().isBadRequest());
        mvc.perform(adminJson(put("/api/admin/settings"), "{\"minOrderValue\":100}"))
                .andExpect(status().isBadRequest());
    }
}
