
package com.upb.mantenimiento.services;

import com.upb.mantenimiento.models.SparePart;
import com.upb.mantenimiento.models.WorkOrder;
import com.upb.mantenimiento.models.WorkOrderPart;

import com.upb.mantenimiento.storage.SparePartFileManager;
import com.upb.mantenimiento.storage.WorkOrderFileManager;
import com.upb.mantenimiento.storage.WorkOrderPartFileManager;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkOrderInventoryService {

    private final SparePartFileManager partManager;
    private final WorkOrderFileManager orderManager;
    private final WorkOrderPartFileManager usageManager;

    // Constructor
    public WorkOrderInventoryService(
            SparePartFileManager partManager,
            WorkOrderFileManager orderManager,
            WorkOrderPartFileManager usageManager) {

        this.partManager = partManager;
        this.orderManager = orderManager;
        this.usageManager = usageManager;
    }

    // Use a spare part in a work order
    public void useSparePart(
            String orderId, String partCode, int quantity) {

        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }

        // Load saved information
        List<SparePart> parts = partManager.loadSpareParts();
        List<WorkOrder> orders = orderManager.loadWorkOrders();
        List<WorkOrderPart> usage = usageManager.loadWorkOrderParts();

        // Find the work order
        WorkOrder selectedOrder = null;

        for (WorkOrder order : orders) {

            if (order.getId().equals(orderId)) {
                selectedOrder = order;
                break;
            }
        }

        if (selectedOrder == null) {
            throw new IllegalArgumentException(
                    "Work order not found."
            );
        }

        // Check the work order status
        if (!selectedOrder.getStatus().equals("In Progress")) {
            throw new IllegalArgumentException(
                    "Work order must be In Progress."
            );
        }

        // Find the spare part
        SparePart selectedPart = null;

        for (SparePart part : parts) {

            if (part.getCode().equals(partCode)) {
                selectedPart = part;
                break;
            }
        }

        if (selectedPart == null) {
            throw new IllegalArgumentException(
                    "Spare part not found."
            );
        }

        // Check available stock
        if (quantity > selectedPart.getStock()) {
            throw new IllegalArgumentException(
                    "Not enough stock. Available: "
                    + selectedPart.getStock()
            );
        }

        // Keep the current interface's one-record-per-part behavior
        for (WorkOrderPart record : usage) {

            if (record.getWorkOrderId().equals(orderId)
                    && record.getPartCode().equals(partCode)) {

                throw new IllegalArgumentException(
                    "This part is already recorded in the work order."
                );
            }
        }

        // Reduce inventory stock
        int newStock = selectedPart.getStock() - quantity;
        selectedPart.setStock(newStock);

        // Record the spare part used
        WorkOrderPart newRecord = new WorkOrderPart(
                orderId,
                partCode,
                quantity,
                selectedPart.getUnitPrice()
        );

        usage.add(newRecord);

        // Calculate the parts cost
        double totalPartsCost = 0;

        for (WorkOrderPart record : usage) {

            if (record.getWorkOrderId().equals(orderId)) {
                totalPartsCost += record.getTotalCost();
            }
        }

        // Update the work order cost
        selectedOrder.setPartsCost(totalPartsCost);

        // Save the updated information
        try {

            partManager.saveSpareParts(parts);
            usageManager.saveWorkOrderParts(usage);
            orderManager.saveWorkOrders(orders);

        } catch (Exception error) {

            throw new IllegalStateException(
                    "Error saving spare part usage.",
                    error
            );
        }
    }
}

