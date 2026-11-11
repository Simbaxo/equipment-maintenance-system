
package com.upb.mantenimiento.models;

import java.time.LocalDate;

public class WorkOrder {

    // Work order information
    private String id;
    private String equipmentCode;
    private String technicianId;
    private String maintenanceType;
    private String failureDescription;
    private LocalDate scheduledDate;
    private String status;
    private double partsCost;
    private double laborCost;
    private LocalDate completionDate;

    // Empty constructor
    public WorkOrder() {
    }

    // Constructor with the main work order information
    public WorkOrder(String id, String equipmentCode,
                     String technicianId, String maintenanceType,
                     String failureDescription, LocalDate scheduledDate,
                     String status, double partsCost, double laborCost,
                     LocalDate completionDate) {

        this.id = id;
        this.equipmentCode = equipmentCode;
        this.technicianId = technicianId;
        this.maintenanceType = maintenanceType;
        this.failureDescription = failureDescription;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.partsCost = partsCost;
        this.laborCost = laborCost;
        this.completionDate = completionDate;
    }

    // Get and update the work order ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Get and update the equipment code
    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    // Get and update the assigned technician
    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
    }

    // Get and update the maintenance type
    public String getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    // Get and update the failure description
    public String getFailureDescription() {
        return failureDescription;
    }

    public void setFailureDescription(String failureDescription) {
        this.failureDescription = failureDescription;
    }

    // Get and update the scheduled date
    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    // Get and update the order status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Get and update the spare parts cost
    public double getPartsCost() {
        return partsCost;
    }

    public void setPartsCost(double partsCost) {
        this.partsCost = partsCost;
    }

    // Get and update the labor cost
    public double getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(double laborCost) {
        this.laborCost = laborCost;
    }

    // Get and update the completion date
    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    // Calculate the total maintenance cost
    public double getTotalCost() {
        return partsCost + laborCost;
    }

    // Check if the work order has been completed
    public boolean isCompleted() {
        return "Completed".equals(status);
    }
}
