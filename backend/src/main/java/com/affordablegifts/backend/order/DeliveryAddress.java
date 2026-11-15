package com.affordablegifts.backend.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DeliveryAddress {

    @Column(name = "address_house", nullable = false)
    private String house;

    @Column(name = "address_street", nullable = false)
    private String street;

    @Column(name = "address_area", nullable = false)
    private String area;

    @Column(name = "address_city", nullable = false)
    private String city;

    @Column(name = "address_state", nullable = false)
    private String state;

    @Column(name = "address_pincode", nullable = false)
    private String pincode;

    @Column(name = "delivery_instructions")
    private String instructions;

    protected DeliveryAddress() {
    }

    public DeliveryAddress(String house, String street, String area, String city, String state,
                           String pincode, String instructions) {
        this.house = house;
        this.street = street;
        this.area = area;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.instructions = instructions;
    }

    public String getHouse() { return house; }
    public String getStreet() { return street; }
    public String getArea() { return area; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public String getInstructions() { return instructions; }
}
