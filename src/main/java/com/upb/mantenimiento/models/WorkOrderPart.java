
package com.upb.mantenimiento.models;

public class WorkOrderPart {

    // Information about a spare part used in a work order
    private String workOrderId;
    private String partCode;
    private int quantity;
    private double unitPrice;

    // Empty constructor
    public WorkOrderPart() {
    }

    // Constructor with all information
    public WorkOrderPart(String workOrderId, String partCode,
                         int quantity, double unitPrice) {

        this.workOrderId = workOrderId;
        this.partCode = partCode;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Get and update the work order ID
    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    // Get and update the spare part code
    public String getPartCode() {
        return partCode;
    }

    public void setPartCode(String partCode) {
        this.partCode = partCode;
    }

    // Get and update the quantity used
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Get and update the price at the time of use
    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    // Calculate the total cost of this spare part usage
    public double getTotalCost() {
        return quantity * unitPrice;
    }
}
