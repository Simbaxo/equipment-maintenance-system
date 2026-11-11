
package com.upb.mantenimiento.models;

public class SparePart {

    // Spare part information
    private String code;
    private String name;
    private String description;
    private double unitPrice;
    private int stock;

    // Empty constructor
    public SparePart() {
    }

    // Constructor with all spare part information
    public SparePart(String code, String name,
                     String description, double unitPrice,
                     int stock) {

        this.code = code;
        this.name = name;
        this.description = description;
        this.unitPrice = unitPrice;
        this.stock = stock;
    }

    // Get the spare part code
    public String getCode() {
        return code;
    }

    // Update the spare part code
    public void setCode(String code) {
        this.code = code;
    }

    // Get the spare part name
    public String getName() {
        return name;
    }

    // Update the spare part name
    public void setName(String name) {
        this.name = name;
    }

    // Get the spare part description
    public String getDescription() {
        return description;
    }

    // Update the spare part description
    public void setDescription(String description) {
        this.description = description;
    }

    // Get the unit price
    public double getUnitPrice() {
        return unitPrice;
    }

    // Update the unit price
    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Get the available stock
    public int getStock() {
        return stock;
    }

    // Update the available stock
    public void setStock(int stock) {
        this.stock = stock;
    }
}
