
package com.upb.mantenimiento.views;

import com.upb.mantenimiento.MainLayout;
import com.upb.mantenimiento.models.Equipment;
import com.upb.mantenimiento.storage.EquipmentFileManager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Route(value = "equipment", layout = MainLayout.class)
@PageTitle("Equipment | Equipment Maintenance System")
public class EquipmentView extends VerticalLayout {

    private final EquipmentFileManager fileManager;
    private final Grid<Equipment> grid = new Grid<>(Equipment.class, false);

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    public EquipmentView(EquipmentFileManager fileManager) {

        this.fileManager = fileManager;

        // Configure the page layout
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        // Page heading
        H2 title = new H2("Equipment Management");

        Paragraph description = new Paragraph(
                "Register, search, update, and manage equipment."
        );

        // Button to register new equipment
        Button addButton = new Button("Add Equipment");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(event -> openEquipmentForm(null));

        // Configure the search field
        searchField.setPlaceholder("Search equipment...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setWidthFull();

        // Refresh the list whenever the user types
        searchField.addValueChangeListener(event -> refreshGrid());

        // Configure the status filter
        statusFilter.setPlaceholder("Filter by status");
        statusFilter.setItems(
                "All",
                "Operational",
                "Under Maintenance",
                "Out of Service"
        );
        statusFilter.setValue("All");
        statusFilter.setWidthFull();

        statusFilter.addValueChangeListener(event -> refreshGrid());

        // Place filters next to each other when space permits
        FormLayout filters = new FormLayout();
        filters.setWidthFull();
        filters.add(searchField, statusFilter);

        // Create the equipment table
        configureGrid();

        // Add all components to the view
        add(title, description, addButton, filters, grid);

        // Display equipment from the text file
        refreshGrid();
    }

    // Configure the equipment table columns
    private void configureGrid() {

        grid.addColumn(Equipment::getCode)
                .setHeader("Code")
                .setAutoWidth(true);

        grid.addColumn(Equipment::getName)
                .setHeader("Name")
                .setAutoWidth(true);

        grid.addColumn(Equipment::getLocation)
                .setHeader("Location")
                .setAutoWidth(true);

        grid.addColumn(Equipment::getModel)
                .setHeader("Model")
                .setAutoWidth(true);

        grid.addColumn(Equipment::getAcquisitionDate)
                .setHeader("Acquisition Date")
                .setAutoWidth(true);

        grid.addColumn(Equipment::getStatus)
                .setHeader("Status")
                .setAutoWidth(true);

        // Add edit and delete buttons to each equipment row
        grid.addComponentColumn(equipment -> {

            Button editButton = new Button("Edit");
            editButton.addClickListener(event ->
                    openEquipmentForm(equipment)
            );

            Button deleteButton = new Button("Delete");
            deleteButton.addThemeVariants(
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY
            );

            deleteButton.addClickListener(event ->
                    confirmDelete(equipment)
            );

            HorizontalLayout actions = new HorizontalLayout(
                    editButton,
                    deleteButton
            );

            return actions;

        }).setHeader("Actions").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");
    }

    // Reload equipment and apply search and status filters
    private void refreshGrid() {

        List<Equipment> equipmentList = fileManager.loadEquipment();
        List<Equipment> filteredList = new ArrayList<>();

        String search = searchField.getValue().trim().toLowerCase();
        String selectedStatus = statusFilter.getValue();

        for (Equipment equipment : equipmentList) {

            boolean matchesSearch =
                    equipment.getCode().toLowerCase().contains(search)
                    || equipment.getName().toLowerCase().contains(search)
                    || equipment.getLocation().toLowerCase().contains(search)
                    || equipment.getModel().toLowerCase().contains(search);

            boolean matchesStatus =
                    selectedStatus == null
                    || selectedStatus.equals("All")
                    || equipment.getStatus().equals(selectedStatus);

            if (matchesSearch && matchesStatus) {
                filteredList.add(equipment);
            }
        }

        grid.setItems(filteredList);
    }

    // Open a form to add or edit equipment
    private void openEquipmentForm(Equipment selectedEquipment) {

        Dialog dialog = new Dialog();
        dialog.setWidth("650px");
        dialog.setMaxWidth("95vw");

        boolean editing = selectedEquipment != null;

        H2 formTitle = new H2(
                editing ? "Edit Equipment" : "Add Equipment"
        );

        TextField codeField = new TextField("Equipment Code");
        codeField.setRequiredIndicatorVisible(true);

        TextField nameField = new TextField("Equipment Name");
        nameField.setRequiredIndicatorVisible(true);

        TextField locationField = new TextField("Location");
        locationField.setRequiredIndicatorVisible(true);

        TextField modelField = new TextField("Model");
        modelField.setRequiredIndicatorVisible(true);

        DatePicker acquisitionDate = new DatePicker(
                "Acquisition Date"
        );
        acquisitionDate.setRequiredIndicatorVisible(true);
        acquisitionDate.setMax(LocalDate.now());

        ComboBox<String> statusField = new ComboBox<>("Status");
        statusField.setItems(
                "Operational",
                "Under Maintenance",
                "Out of Service"
        );
        statusField.setRequired(true);

        // Fill the form when editing an existing record
        if (editing) {

            codeField.setValue(selectedEquipment.getCode());
            codeField.setReadOnly(true);

            nameField.setValue(selectedEquipment.getName());
            locationField.setValue(selectedEquipment.getLocation());
            modelField.setValue(selectedEquipment.getModel());
            acquisitionDate.setValue(
                    selectedEquipment.getAcquisitionDate()
            );
            statusField.setValue(selectedEquipment.getStatus());

        } else {
            statusField.setValue("Operational");
        }

        // Arrange the form fields
        FormLayout form = new FormLayout();

        form.add(
                codeField,
                nameField,
                locationField,
                modelField,
                acquisitionDate,
                statusField
        );

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Form action buttons
        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> dialog.close());

        saveButton.addClickListener(event -> {

            String code = codeField.getValue().trim();
            String name = nameField.getValue().trim();
            String location = locationField.getValue().trim();
            String model = modelField.getValue().trim();
            LocalDate date = acquisitionDate.getValue();
            String status = statusField.getValue();

            // Check required fields
            if (code.isEmpty()
                    || name.isEmpty()
                    || location.isEmpty()
                    || model.isEmpty()
                    || date == null
                    || status == null) {

                Notification.show(
                        "Please complete all required fields."
                );
                return;
            }

            // Validate equipment code format
            if (!code.matches("[A-Za-z0-9-]+")) {
                Notification.show(
                        "Equipment code can only contain letters, numbers, and hyphens."
                );
                return;
            }

            // Validate the acquisition date
            if (date.isAfter(LocalDate.now())) {
                Notification.show(
                        "Acquisition date cannot be in the future."
                );
                return;
            }

            // Prevent text-file separators and line breaks
            String allText = code + name + location + model;

            if (allText.contains("|")
                    || allText.contains("\n")
                    || allText.contains("\r")) {

                Notification.show(
                        "Fields cannot contain | or line breaks."
                );
                return;
            }

            List<Equipment> equipmentList =
                    fileManager.loadEquipment();

            if (editing) {

                // Replace the existing equipment record
                boolean found = false;

                for (int i = 0; i < equipmentList.size(); i++) {

                    Equipment current = equipmentList.get(i);

                    if (current.getCode().equals(
                            selectedEquipment.getCode())) {

                        Equipment updated = new Equipment(
                                code, name, location, model,
                                date, status
                        );

                        equipmentList.set(i, updated);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Notification.show(
                            "Equipment no longer exists."
                    );
                    return;
                }

            } else {

                // Prevent duplicate equipment codes
                for (Equipment current : equipmentList) {

                    if (current.getCode().equalsIgnoreCase(code)) {
                        Notification.show(
                                "This equipment code already exists."
                        );
                        return;
                    }
                }

                Equipment newEquipment = new Equipment(
                        code, name, location, model,
                        date, status
                );

                equipmentList.add(newEquipment);
            }

            // Save changes to the text file
            try {

                fileManager.saveEquipment(equipmentList);

                dialog.close();
                refreshGrid();

                Notification.show(
                        editing
                                ? "Equipment updated successfully."
                                : "Equipment added successfully."
                );

            } catch (Exception error) {

                Notification.show(
                        "Error saving equipment: "
                        + error.getMessage()
                );
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(
                saveButton,
                cancelButton
        );

        VerticalLayout content = new VerticalLayout(
                formTitle,
                form,
                buttons
        );

        content.setPadding(false);
        content.setWidthFull();

        dialog.add(content);
        dialog.open();
    }

    // Require confirmation before deleting equipment
    private void confirmDelete(Equipment equipment) {

        Dialog dialog = new Dialog();

        H2 title = new H2("Delete Equipment");

        Paragraph message = new Paragraph(
                "Are you sure you want to delete "
                + equipment.getName() + "?"
        );

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> dialog.close());

        Button deleteButton = new Button("Delete");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        deleteButton.addClickListener(event -> {

            List<Equipment> equipmentList =
                    fileManager.loadEquipment();

            boolean removed = false;

            // Find and remove the selected equipment
            for (int i = 0; i < equipmentList.size(); i++) {

                Equipment current = equipmentList.get(i);

                if (current.getCode().equals(equipment.getCode())) {
                    equipmentList.remove(i);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                Notification.show("Equipment no longer exists.");
                dialog.close();
                refreshGrid();
                return;
            }

            try {

                fileManager.saveEquipment(equipmentList);

                dialog.close();
                refreshGrid();

                Notification.show(
                        "Equipment deleted successfully."
                );

            } catch (Exception error) {

                Notification.show(
                        "Error deleting equipment: "
                        + error.getMessage()
                );
            }
        });

        dialog.add(
                title,
                message,
                new HorizontalLayout(cancelButton, deleteButton)
        );

        dialog.open();
    }
}
