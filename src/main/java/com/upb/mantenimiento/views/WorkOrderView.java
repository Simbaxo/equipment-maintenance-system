
package com.upb.mantenimiento.views;

import com.upb.mantenimiento.MainLayout;
import com.upb.mantenimiento.models.Equipment;
import com.upb.mantenimiento.models.Technician;
import com.upb.mantenimiento.models.WorkOrder;
import com.upb.mantenimiento.storage.EquipmentFileManager;
import com.upb.mantenimiento.storage.TechnicianFileManager;
import com.upb.mantenimiento.storage.WorkOrderFileManager;
import com.upb.mantenimiento.models.SparePart;
import com.upb.mantenimiento.models.WorkOrderPart;
import com.upb.mantenimiento.storage.SparePartFileManager;
import com.upb.mantenimiento.storage.WorkOrderPartFileManager;
import com.upb.mantenimiento.services.WorkOrderInventoryService;
import com.vaadin.flow.component.textfield.IntegerField;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Route(value = "work-orders", layout = MainLayout.class)
@PageTitle("Work Orders | Equipment Maintenance System")
public class WorkOrderView extends VerticalLayout {

    private final WorkOrderFileManager orderManager;
    // Managers for spare part consumption
private final SparePartFileManager sparePartManager;
private final WorkOrderPartFileManager usageManager;
private final WorkOrderInventoryService inventoryService;
    private final EquipmentFileManager equipmentManager;
    private final TechnicianFileManager technicianManager;

    private final Grid<WorkOrder> grid =
            new Grid<>(WorkOrder.class, false);

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    public WorkOrderView(
        WorkOrderFileManager orderManager,
        EquipmentFileManager equipmentManager,
        TechnicianFileManager technicianManager,
        SparePartFileManager sparePartManager,
        WorkOrderPartFileManager usageManager,
        WorkOrderInventoryService inventoryService) {

    this.orderManager = orderManager;
    this.equipmentManager = equipmentManager;
    this.technicianManager = technicianManager;
    this.sparePartManager = sparePartManager;
    this.usageManager = usageManager;
    this.inventoryService = inventoryService;

        setPadding(true);
        setSpacing(true);
        setWidthFull();

        H2 title = new H2("Work Order Management");

        Paragraph description = new Paragraph(
                "Schedule preventive and corrective maintenance."
        );

        Button addButton = new Button("Create Work Order");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(event -> openForm(null));

        // Configure live search
        searchField.setPlaceholder("Search work orders...");
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> refreshGrid());

        // Configure status filtering
        statusFilter.setItems(
                "All", "Scheduled", "In Progress", "Completed"
        );
        statusFilter.setValue("All");
        statusFilter.setWidthFull();
        statusFilter.addValueChangeListener(event -> refreshGrid());

        FormLayout filters = new FormLayout();
        filters.add(searchField, statusFilter);
        filters.setWidthFull();

        configureGrid();

        add(title, description, addButton, filters, grid);

        refreshGrid();
    }

    // Find the name of an equipment record
    private String getEquipmentName(String code) {
        for (Equipment equipment : equipmentManager.loadEquipment()) {
            if (equipment.getCode().equals(code)) {
                return equipment.getName();
            }
        }
        return code;
    }

    // Find the name of a technician
    private String getTechnicianName(String id) {
        for (Technician technician : technicianManager.loadTechnicians()) {
            if (technician.getId().equals(id)) {
                return technician.getName();
            }
        }
        return id;
    }

    // Configure the work order table
    private void configureGrid() {

        grid.addColumn(WorkOrder::getId)
                .setHeader("Order ID").setAutoWidth(true);

        grid.addColumn(order ->
                getEquipmentName(order.getEquipmentCode()))
                .setHeader("Equipment").setAutoWidth(true);

        grid.addColumn(WorkOrder::getMaintenanceType)
                .setHeader("Type").setAutoWidth(true);

        // Show the assigned technician with an Avatar
        grid.addComponentColumn(order -> {

            String name = getTechnicianName(
                    order.getTechnicianId()
            );

            Avatar avatar = new Avatar(name);
            Span label = new Span(name);

            HorizontalLayout layout =
                    new HorizontalLayout(avatar, label);

            layout.setAlignItems(Alignment.CENTER);
            return layout;

        }).setHeader("Technician").setAutoWidth(true);

        grid.addColumn(WorkOrder::getScheduledDate)
                .setHeader("Scheduled Date").setAutoWidth(true);

        grid.addColumn(WorkOrder::getStatus)
                .setHeader("Status").setAutoWidth(true);

        grid.addColumn(order ->
                String.format("$%,.0f", order.getTotalCost()))
                .setHeader("Total Cost").setAutoWidth(true);

        grid.addComponentColumn(order -> {

            Button editButton = new Button("Edit");
            editButton.addClickListener(event -> openForm(order));

            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY
            );
            deleteButton.addClickListener(event ->
                    confirmDelete(order)
            );

            // Open the spare part consumption form
Button usePartButton = new Button("Use Part");

usePartButton.setEnabled(
        order.getStatus().equals("In Progress")
);

usePartButton.addClickListener(event ->
        openUsePartDialog(order)
);

return new HorizontalLayout(
        usePartButton,
        editButton,
        deleteButton
);

        }).setHeader("Actions").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");
    }

    // Reload orders and apply the search and status filters
    private void refreshGrid() {

        List<WorkOrder> filtered = new ArrayList<>();

        String search =
                searchField.getValue().trim().toLowerCase();

        String status = statusFilter.getValue();

        for (WorkOrder order : orderManager.loadWorkOrders()) {

            boolean matchesSearch =
                    order.getId().toLowerCase().contains(search)
                    || getEquipmentName(order.getEquipmentCode())
                            .toLowerCase().contains(search)
                    || getTechnicianName(order.getTechnicianId())
                            .toLowerCase().contains(search)
                    || order.getMaintenanceType()
                            .toLowerCase().contains(search);

            boolean matchesStatus =
                    status == null || status.equals("All")
                    || order.getStatus().equals(status);

            if (matchesSearch && matchesStatus) {
                filtered.add(order);
            }
        }

        grid.setItems(filtered);
    }

    // Create or edit a maintenance work order
    private void openForm(WorkOrder selectedOrder) {

        boolean editing = selectedOrder != null;

        Dialog dialog = new Dialog();
        dialog.setWidth("700px");
        dialog.setMaxWidth("95vw");

        H2 title = new H2(
                editing ? "Edit Work Order" : "Create Work Order"
        );

        TextField idField = new TextField("Order ID");

        // Choose equipment from existing records
        Select<String> equipmentField = new Select<>();
        equipmentField.setLabel("Equipment");

        List<String> equipmentCodes = new ArrayList<>();

        for (Equipment equipment : equipmentManager.loadEquipment()) {
            equipmentCodes.add(equipment.getCode());
        }

        equipmentField.setItems(equipmentCodes);
        equipmentField.setItemLabelGenerator(code ->
                code + " - " + getEquipmentName(code)
        );

        // Required research component: Select
        Select<String> typeField = new Select<>();
        typeField.setLabel("Maintenance Type");
        typeField.setItems("Preventive", "Corrective");

        Select<String> technicianField = new Select<>();
        technicianField.setLabel("Assigned Technician");

        List<String> technicianIds = new ArrayList<>();

        for (Technician technician :
                technicianManager.loadTechnicians()) {

            if (technician.getStatus().equals("Active")
                    || (editing && technician.getId().equals(
                            selectedOrder.getTechnicianId()))) {

                technicianIds.add(technician.getId());
            }
        }

        technicianField.setItems(technicianIds);
        technicianField.setItemLabelGenerator(id ->
                getTechnicianName(id)
        );

        TextArea failureField = new TextArea(
                "Failure Description / Maintenance Details"
        );

        DatePicker dateField = new DatePicker("Scheduled Date");

        Select<String> statusField = new Select<>();
        statusField.setLabel("Order Status");
        statusField.setItems(
                "Scheduled", "In Progress", "Completed"
        );

        NumberField laborField = new NumberField("Labor Cost");
        laborField.setMin(0);
        laborField.setValue(0.0);

        // Parts costs will be calculated from inventory usage
        NumberField partsField = new NumberField("Parts Cost");
        partsField.setValue(0.0);
        partsField.setReadOnly(true);

        if (editing) {

            idField.setValue(selectedOrder.getId());
            idField.setReadOnly(true);

            equipmentField.setValue(
                    selectedOrder.getEquipmentCode()
            );
            typeField.setValue(
                    selectedOrder.getMaintenanceType()
            );
            technicianField.setValue(
                    selectedOrder.getTechnicianId()
            );
            failureField.setValue(
                    selectedOrder.getFailureDescription()
            );
            dateField.setValue(selectedOrder.getScheduledDate());
            statusField.setValue(selectedOrder.getStatus());
            laborField.setValue(selectedOrder.getLaborCost());
            partsField.setValue(selectedOrder.getPartsCost());

        } else {
            typeField.setValue("Preventive");
            statusField.setValue("Scheduled");
            dateField.setValue(LocalDate.now());
        }

        FormLayout form = new FormLayout();

        form.add(
                idField, equipmentField, typeField,
                technicianField, failureField, dateField,
                statusField, laborField, partsField
        );

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> dialog.close());

        saveButton.addClickListener(event -> {

            String id = idField.getValue().trim();
            String equipmentCode = equipmentField.getValue();
            String type = typeField.getValue();
            String technicianId = technicianField.getValue();
            String details = failureField.getValue().trim();
            LocalDate date = dateField.getValue();
            String status = statusField.getValue();
            Double labor = laborField.getValue();

            // Check required fields
            if (id.isEmpty()
                    || equipmentCode == null
                    || type == null
                    || technicianId == null
                    || details.isEmpty()
                    || date == null
                    || status == null
                    || labor == null) {

                Notification.show(
                        "Please complete all required fields."
                );
                return;
            }

            if (!id.matches("[A-Za-z0-9-]+")) {
                Notification.show("Invalid work order ID.");
                return;
            }

            if (details.contains("|")
                    || details.contains("\n")
                    || details.contains("\r")) {

                Notification.show(
                        "Description cannot contain | or line breaks."
                );
                return;
            }

            if (!Double.isFinite(labor) || labor < 0) {
                Notification.show(
                        "Labor cost must be a valid positive or zero amount."
                );
                return;
            }

            List<WorkOrder> orders =
                    orderManager.loadWorkOrders();

            // Set completion date when an order is completed
            LocalDate completionDate = null;

            if (status.equals("Completed")) {
                if (editing && selectedOrder.isCompleted()) {
                    completionDate = selectedOrder.getCompletionDate();
                } else {
                    completionDate = LocalDate.now();
                }
            }

            if (editing) {

                boolean found = false;

                for (int i = 0; i < orders.size(); i++) {

                    WorkOrder current = orders.get(i);

                    if (current.getId().equals(selectedOrder.getId())) {

                        // Preserve existing parts cost
                        WorkOrder updated = new WorkOrder(
                                id, equipmentCode, technicianId,
                                type, details, date, status,
                                current.getPartsCost(), labor,
                                completionDate
                        );

                        orders.set(i, updated);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Notification.show(
                            "Work order no longer exists."
                    );
                    return;
                }

            } else {

                for (WorkOrder current : orders) {
                    if (current.getId().equalsIgnoreCase(id)) {
                        Notification.show(
                                "This work order ID already exists."
                        );
                        return;
                    }
                }

                orders.add(new WorkOrder(
                        id, equipmentCode, technicianId,
                        type, details, date, status,
                        0, labor, completionDate
                ));
            }

            try {
                orderManager.saveWorkOrders(orders);
                dialog.close();
                refreshGrid();

                Notification.show(
                        editing
                                ? "Work order updated successfully."
                                : "Work order created successfully."
                );

            } catch (Exception error) {
                Notification.show(
                        "Error saving work order: "
                        + error.getMessage()
                );
            }
        });

        VerticalLayout content = new VerticalLayout(
                title, form,
                new HorizontalLayout(saveButton, cancelButton)
        );
        content.setPadding(false);
        content.setWidthFull();

        dialog.add(content);
        dialog.open();
    }

    // Ask for confirmation before deleting a work order
    private void confirmDelete(WorkOrder order) {

        Dialog dialog = new Dialog();

        Paragraph message = new Paragraph(
                "Are you sure you want to delete " + order.getId() + "?"
        );

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> dialog.close());

        Button deleteButton = new Button("Delete");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        deleteButton.addClickListener(event -> {

            List<WorkOrder> orders =
                    orderManager.loadWorkOrders();

            // Do not delete completed maintenance history
            if (order.isCompleted()) {
                Notification.show(
                        "Completed work orders must be kept for maintenance history."
                );
                dialog.close();
                return;
            }

            boolean removed = false;

            // Find and remove the selected work order
            for (int i = 0; i < orders.size(); i++) {

                WorkOrder current = orders.get(i);

                if (current.getId().equals(order.getId())) {
                    orders.remove(i);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                Notification.show("Work order no longer exists.");
                dialog.close();
                refreshGrid();
                return;
            }

            try {
                orderManager.saveWorkOrders(orders);
                dialog.close();
                refreshGrid();
                Notification.show(
                        "Work order deleted successfully."
                );

            } catch (Exception error) {
                Notification.show(
                        "Error deleting work order: "
                        + error.getMessage()
                );
            }
        });

        dialog.add(
                new H2("Delete Work Order"),
                message,
                new HorizontalLayout(cancelButton, deleteButton)
        );
        dialog.open();
    }

    // Open a dialog to use a spare part in a work order
    private void openUsePartDialog(WorkOrder selectedOrder) {

        // Reload the order to verify its current status
        WorkOrder currentOrder = null;

        for (WorkOrder order : orderManager.loadWorkOrders()) {
            if (order.getId().equals(selectedOrder.getId())) {
                currentOrder = order;
                break;
            }
        }

        if (currentOrder == null
                || !currentOrder.getStatus().equals("In Progress")) {

            Notification.show(
                    "This work order is not available for parts usage."
            );
            refreshGrid();
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");

        H2 title = new H2("Use Spare Part");

        Paragraph description = new Paragraph(
                "Work Order: " + selectedOrder.getId()
                + " - " + getEquipmentName(
                        selectedOrder.getEquipmentCode())
        );

        // Create the spare part selector
        Select<String> partField = new Select<>();
        partField.setLabel("Spare Part");
        partField.setWidthFull();

        List<String> availableCodes = new ArrayList<>();

        // Do not offer parts already used in this work order
        List<WorkOrderPart> existingUsage =
                usageManager.getPartsForOrder(selectedOrder.getId());

        for (SparePart part : sparePartManager.loadSpareParts()) {

            boolean alreadyUsed = false;

            for (WorkOrderPart record : existingUsage) {
                if (record.getPartCode().equals(part.getCode())) {
                    alreadyUsed = true;
                    break;
                }
            }

            if (part.getStock() > 0 && !alreadyUsed) {
                availableCodes.add(part.getCode());
            }
        }

        partField.setItems(availableCodes);

        // Display readable names and prices
        partField.setItemLabelGenerator(code -> {

            for (SparePart part : sparePartManager.loadSpareParts()) {

                if (part.getCode().equals(code)) {
                    return part.getName()
                            + " - $"
                            + String.format("%,.0f", part.getUnitPrice());
                }
            }

            return code;
        });

        // Quantity of parts being consumed
        IntegerField quantityField = new IntegerField("Quantity");
        quantityField.setMin(1);
        quantityField.setValue(1);
        quantityField.setStepButtonsVisible(true);
        quantityField.setWidthFull();

        Paragraph stockMessage = new Paragraph(
                "Select a spare part to view available stock."
        );

        Paragraph totalMessage = new Paragraph(
                "Total Cost: $0"
        );

        // Update the available stock and total cost
        Runnable updateSummary = () -> {

            String selectedCode = partField.getValue();
            Integer quantity = quantityField.getValue();

            if (selectedCode == null || quantity == null) {
                stockMessage.setText(
                        "Select a spare part and quantity."
                );
                totalMessage.setText("Total Cost: $0");
                return;
            }

            for (SparePart part : sparePartManager.loadSpareParts()) {

                if (part.getCode().equals(selectedCode)) {

                    stockMessage.setText(
                            "Available Stock: " + part.getStock()
                    );

                    double total = part.getUnitPrice() * quantity;

                    totalMessage.setText(
                            "Total Cost: $"
                            + String.format("%,.0f", total)
                    );
                    return;
                }
            }

            stockMessage.setText("Spare part not found.");
            totalMessage.setText("Total Cost: $0");
        };

        partField.addValueChangeListener(event ->
                updateSummary.run()
        );

        quantityField.addValueChangeListener(event ->
                updateSummary.run()
        );

        // Save the spare part usage
        Button saveButton = new Button("Use Spare Part");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> dialog.close());

        saveButton.addClickListener(event -> {

            String partCode = partField.getValue();
            Integer quantity = quantityField.getValue();

            if (partCode == null) {
                Notification.show(
                        "Please select a spare part."
                );
                return;
            }

            if (quantity == null || quantity <= 0) {
                Notification.show(
                        "Quantity must be greater than zero."
                );
                return;
            }

            try {

                // Deduct stock and record the cost
                inventoryService.useSparePart(
                        selectedOrder.getId(),
                        partCode,
                        quantity
                );

                dialog.close();
                refreshGrid();

                Notification.show(
                        "Spare part used successfully. "
                        + "Inventory and work order costs updated."
                );

            } catch (Exception error) {

                Notification.show(
                        "Could not use spare part: "
                        + error.getMessage(),
                        5000,
                        Notification.Position.MIDDLE
                );
            }
        });

        if (availableCodes.isEmpty()) {
            saveButton.setEnabled(false);
            stockMessage.setText(
                    "No available spare parts for this work order."
            );
        }

        FormLayout form = new FormLayout();
        form.add(partField, quantityField);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        VerticalLayout content = new VerticalLayout(
                title,
                description,
                form,
                stockMessage,
                totalMessage,
                new HorizontalLayout(saveButton, cancelButton)
        );

        content.setPadding(false);
        content.setWidthFull();

        dialog.add(content);
        dialog.open();
    }
}
