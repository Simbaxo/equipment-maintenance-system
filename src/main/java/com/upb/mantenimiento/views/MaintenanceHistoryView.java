
package com.upb.mantenimiento.views;

import com.upb.mantenimiento.MainLayout;
import com.upb.mantenimiento.models.Equipment;
import com.upb.mantenimiento.models.Technician;
import com.upb.mantenimiento.models.WorkOrder;
import com.upb.mantenimiento.storage.EquipmentFileManager;
import com.upb.mantenimiento.storage.TechnicianFileManager;
import com.upb.mantenimiento.storage.WorkOrderFileManager;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "maintenance-history", layout = MainLayout.class)
@PageTitle("Maintenance History | Equipment Maintenance System")
public class MaintenanceHistoryView extends VerticalLayout {

    private final EquipmentFileManager equipmentManager;
    private final TechnicianFileManager technicianManager;
    private final WorkOrderFileManager orderManager;

    private final VerticalLayout historyContent =
            new VerticalLayout();

    public MaintenanceHistoryView(
            EquipmentFileManager equipmentManager,
            TechnicianFileManager technicianManager,
            WorkOrderFileManager orderManager) {

        this.equipmentManager = equipmentManager;
        this.technicianManager = technicianManager;
        this.orderManager = orderManager;

        // Configure the page layout
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        H2 title = new H2("Maintenance History");

        Paragraph description = new Paragraph(
                "View preventive and corrective maintenance by equipment."
        );

        // Select the equipment to view
        Select<String> equipmentSelect = new Select<>();
        equipmentSelect.setLabel("Select Equipment");
        equipmentSelect.setWidthFull();

        List<String> equipmentCodes = new ArrayList<>();

        for (Equipment equipment : equipmentManager.loadEquipment()) {
            equipmentCodes.add(equipment.getCode());
        }

        equipmentSelect.setItems(equipmentCodes);

        equipmentSelect.setItemLabelGenerator(code ->
                code + " - " + getEquipmentName(code)
        );

        // Load history when equipment is selected
        equipmentSelect.addValueChangeListener(event ->
                showHistory(event.getValue())
        );

        historyContent.setPadding(false);
        historyContent.setWidthFull();

        add(title, description, equipmentSelect, historyContent);

        // Automatically display the first equipment
        if (!equipmentCodes.isEmpty()) {
            equipmentSelect.setValue(equipmentCodes.get(0));
        } else {
            historyContent.add(
                    new Paragraph("No equipment registered.")
            );
        }
    }

    // Get the equipment name from its code
    private String getEquipmentName(String code) {

        for (Equipment equipment : equipmentManager.loadEquipment()) {
            if (equipment.getCode().equals(code)) {
                return equipment.getName();
            }
        }

        return code;
    }

    // Get the technician name from its ID
    private String getTechnicianName(String id) {

        for (Technician technician : technicianManager.loadTechnicians()) {
            if (technician.getId().equals(id)) {
                return technician.getName();
            }
        }

        return id;
    }

    // Display maintenance records for one equipment
    private void showHistory(String equipmentCode) {

        historyContent.removeAll();

        if (equipmentCode == null) {
            return;
        }

        List<WorkOrder> preventive = new ArrayList<>();
        List<WorkOrder> corrective = new ArrayList<>();

        // Separate work orders by maintenance type
        for (WorkOrder order : orderManager.loadWorkOrders()) {

            if (!order.getEquipmentCode().equals(equipmentCode)) {
                continue;
            }

            if (order.getMaintenanceType().equals("Preventive")) {
                preventive.add(order);
            } else if (order.getMaintenanceType().equals("Corrective")) {
                corrective.add(order);
            }
        }

        H3 equipmentTitle = new H3(
                getEquipmentName(equipmentCode)
        );

        // Required component: Accordion
        Accordion accordion = new Accordion();
        accordion.setWidthFull();

        // Create the preventive maintenance section
        VerticalLayout preventiveContent =
                createOrderList(preventive);

        accordion.add(
                "Preventive Maintenance (" + preventive.size() + ")",
                preventiveContent
        );

        // Create the corrective maintenance section
        VerticalLayout correctiveContent =
                createOrderList(corrective);

        accordion.add(
                "Corrective Maintenance (" + corrective.size() + ")",
                correctiveContent
        );

        historyContent.add(equipmentTitle, accordion);
    }

    // Create a list of work orders for an Accordion section
    private VerticalLayout createOrderList(List<WorkOrder> orders) {

        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setWidthFull();

        if (orders.isEmpty()) {
            layout.add(
                    new Paragraph("No maintenance records found.")
            );
            return layout;
        }

        for (WorkOrder order : orders) {

            VerticalLayout orderDetails = new VerticalLayout();
            orderDetails.setSpacing(false);
            orderDetails.setPadding(true);
            orderDetails.setWidthFull();

            orderDetails.getStyle()
                    .set("border", "1px solid #dddddd")
                    .set("border-radius", "8px")
                    .set("margin-bottom", "10px");

            H3 orderTitle = new H3(
                    "Work Order: " + order.getId()
            );

            Paragraph technician = new Paragraph(
                    "Technician: "
                    + getTechnicianName(order.getTechnicianId())
            );

            Paragraph scheduledDate = new Paragraph(
                    "Scheduled Date: " + order.getScheduledDate()
            );

            Paragraph status = new Paragraph(
                    "Status: " + order.getStatus()
            );

            Paragraph details = new Paragraph(
                    "Description: " + order.getFailureDescription()
            );

            Paragraph partsCost = new Paragraph(
                    String.format(
                            "Parts Cost: $%,.0f",
                            order.getPartsCost()
                    )
            );

            Paragraph laborCost = new Paragraph(
                    String.format(
                            "Labor Cost: $%,.0f",
                            order.getLaborCost()
                    )
            );

            Paragraph totalCost = new Paragraph(
                    String.format(
                            "Total Cost: $%,.0f",
                            order.getTotalCost()
                    )
            );

            orderDetails.add(
                    orderTitle,
                    technician,
                    scheduledDate,
                    status,
                    details,
                    partsCost,
                    laborCost,
                    totalCost
            );

            if (order.getCompletionDate() != null) {
                orderDetails.add(
                        new Paragraph(
                                "Completed: " + order.getCompletionDate()
                        )
                );
            }

            layout.add(orderDetails);
        }

        return layout;
    }
}
