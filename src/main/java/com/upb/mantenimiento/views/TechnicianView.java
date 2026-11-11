
package com.upb.mantenimiento.views;

import com.upb.mantenimiento.MainLayout;
import com.upb.mantenimiento.models.Technician;
import com.upb.mantenimiento.storage.TechnicianFileManager;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "technicians", layout = MainLayout.class)
@PageTitle("Technicians | Equipment Maintenance System")
public class TechnicianView extends VerticalLayout {

    private final TechnicianFileManager fileManager;

    private final Grid<Technician> grid =
            new Grid<>(Technician.class, false);

    private final TextField searchField = new TextField();
    private final ComboBox<String> statusFilter = new ComboBox<>();

    public TechnicianView(TechnicianFileManager fileManager) {

        this.fileManager = fileManager;

        // Configure the main page
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        H2 title = new H2("Technician Management");

        Paragraph description = new Paragraph(
                "Register and manage maintenance technicians."
        );

        // Create the add technician button
        Button addButton = new Button("Add Technician");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        addButton.addClickListener(event -> openForm(null));

        // Configure live search
        searchField.setPlaceholder("Search technicians...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setWidthFull();

        searchField.addValueChangeListener(event -> refreshGrid());

        // Configure the status filter
        statusFilter.setPlaceholder("Filter by status");
        statusFilter.setItems("All", "Active", "Inactive");
        statusFilter.setValue("All");
        statusFilter.setWidthFull();

        statusFilter.addValueChangeListener(event -> refreshGrid());

        FormLayout filters = new FormLayout();
        filters.setWidthFull();
        filters.add(searchField, statusFilter);

        // Create the technician table
        configureGrid();

        add(title, description, addButton, filters, grid);

        refreshGrid();
    }

    // Configure the columns of the technician table
    private void configureGrid() {

        // Display the technician's avatar and name together
        grid.addComponentColumn(technician -> {

            Avatar avatar = new Avatar(technician.getName());

            avatar.getStyle().set(
                    "--vaadin-avatar-size", "36px"
            );

            Span name = new Span(technician.getName());

            HorizontalLayout technicianInfo =
                    new HorizontalLayout(avatar, name);

            technicianInfo.setAlignItems(
                    Alignment.CENTER
            );

            technicianInfo.setSpacing(true);

            return technicianInfo;

        }).setHeader("Technician").setAutoWidth(true);

        grid.addColumn(Technician::getId)
                .setHeader("ID")
                .setAutoWidth(true);

        grid.addColumn(Technician::getSpecialty)
                .setHeader("Specialty")
                .setAutoWidth(true);

        grid.addColumn(Technician::getPhone)
                .setHeader("Phone")
                .setAutoWidth(true);

        grid.addColumn(Technician::getStatus)
                .setHeader("Status")
                .setAutoWidth(true);

        // Add edit and delete buttons
        grid.addComponentColumn(technician -> {

            Button editButton = new Button("Edit");

            editButton.addClickListener(event ->
                    openForm(technician)
            );

            Button deleteButton = new Button("Delete");

            deleteButton.addThemeVariants(
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY
            );

            deleteButton.addClickListener(event ->
                    confirmDelete(technician)
            );

            return new HorizontalLayout(
                    editButton,
                    deleteButton
            );

        }).setHeader("Actions").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");
    }

    // Load technicians and apply the search and status filters
    private void refreshGrid() {

        List<Technician> technicians =
                fileManager.loadTechnicians();

        List<Technician> filtered = new ArrayList<>();

        String search =
                searchField.getValue().trim().toLowerCase();

        String status = statusFilter.getValue();

        for (Technician technician : technicians) {

            boolean matchesSearch =
                    technician.getId().toLowerCase().contains(search)
                    || technician.getName().toLowerCase().contains(search)
                    || technician.getSpecialty().toLowerCase().contains(search)
                    || technician.getPhone().toLowerCase().contains(search);

            boolean matchesStatus =
                    status == null
                    || status.equals("All")
                    || technician.getStatus().equals(status);

            if (matchesSearch && matchesStatus) {
                filtered.add(technician);
            }
        }

        grid.setItems(filtered);
    }

    // Open the form for adding or editing a technician
    private void openForm(Technician selectedTechnician) {

        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setMaxWidth("95vw");

        boolean editing = selectedTechnician != null;

        H2 title = new H2(
                editing ? "Edit Technician" : "Add Technician"
        );

        TextField idField = new TextField("Technician ID");
        idField.setRequiredIndicatorVisible(true);

        TextField nameField = new TextField("Full Name");
        nameField.setRequiredIndicatorVisible(true);

        TextField specialtyField = new TextField("Specialty");
        specialtyField.setRequiredIndicatorVisible(true);

        TextField phoneField = new TextField("Phone Number");
        phoneField.setRequiredIndicatorVisible(true);

        ComboBox<String> statusField =
                new ComboBox<>("Status");

        statusField.setItems("Active", "Inactive");
        statusField.setRequired(true);

        // Populate the form when editing
        if (editing) {

            idField.setValue(selectedTechnician.getId());
            idField.setReadOnly(true);

            nameField.setValue(selectedTechnician.getName());
            specialtyField.setValue(
                    selectedTechnician.getSpecialty()
            );
            phoneField.setValue(selectedTechnician.getPhone());
            statusField.setValue(selectedTechnician.getStatus());

        } else {
            statusField.setValue("Active");
        }

        FormLayout form = new FormLayout();

        form.add(
                idField,
                nameField,
                specialtyField,
                phoneField,
                statusField
        );

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Create the form buttons
        Button saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> dialog.close());

        saveButton.addClickListener(event -> {

            String id = idField.getValue().trim();
            String name = nameField.getValue().trim();
            String specialty = specialtyField.getValue().trim();
            String phone = phoneField.getValue().trim();
            String status = statusField.getValue();

            // Validate required information
            if (id.isEmpty()
                    || name.isEmpty()
                    || specialty.isEmpty()
                    || phone.isEmpty()
                    || status == null) {

                Notification.show(
                        "Please complete all required fields."
                );
                return;
            }

            // Validate the technician ID
            if (!id.matches("[A-Za-z0-9-]+")) {

                Notification.show(
                        "Technician ID can only contain letters, numbers, and hyphens."
                );
                return;
            }

            // Validate the phone number
            if (!phone.matches("[+]?[0-9]{7,15}")) {

                Notification.show(
                        "Enter a valid phone number (7 to 15 digits)."
                );
                return;
            }

            // Protect the text-file format
            String combinedText =
                    id + name + specialty + phone;

            if (combinedText.contains("|")
                    || combinedText.contains("\n")
                    || combinedText.contains("\r")) {

                Notification.show(
                        "Fields cannot contain | or line breaks."
                );
                return;
            }

            List<Technician> technicians =
                    fileManager.loadTechnicians();

            if (editing) {

                boolean found = false;

                for (int i = 0; i < technicians.size(); i++) {

                    Technician current = technicians.get(i);

                    if (current.getId().equals(
                            selectedTechnician.getId())) {

                        Technician updated = new Technician(
                                id,
                                name,
                                specialty,
                                phone,
                                status
                        );

                        technicians.set(i, updated);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Notification.show(
                            "Technician no longer exists."
                    );
                    return;
                }

            } else {

                // Prevent duplicate technician IDs
                for (Technician current : technicians) {

                    if (current.getId().equalsIgnoreCase(id)) {

                        Notification.show(
                                "This technician ID already exists."
                        );
                        return;
                    }
                }

                Technician newTechnician = new Technician(
                        id,
                        name,
                        specialty,
                        phone,
                        status
                );

                technicians.add(newTechnician);
            }

            // Save the updated technician list
            try {

                fileManager.saveTechnicians(technicians);

                dialog.close();
                refreshGrid();

                Notification.show(
                        editing
                                ? "Technician updated successfully."
                                : "Technician added successfully."
                );

            } catch (Exception error) {

                Notification.show(
                        "Error saving technician: "
                        + error.getMessage()
                );
            }
        });

        HorizontalLayout buttons =
                new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout content = new VerticalLayout(
                title,
                form,
                buttons
        );

        content.setPadding(false);
        content.setWidthFull();

        dialog.add(content);
        dialog.open();
    }

    // Ask for confirmation before deleting a technician
    private void confirmDelete(Technician technician) {

        Dialog dialog = new Dialog();

        H2 title = new H2("Delete Technician");

        Paragraph message = new Paragraph(
                "Are you sure you want to delete "
                + technician.getName() + "?"
        );

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> dialog.close());

        Button deleteButton = new Button("Delete");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        deleteButton.addClickListener(event -> {

            List<Technician> technicians =
                    fileManager.loadTechnicians();

            boolean removed = false;

            // Find and remove the selected technician
            for (int i = 0; i < technicians.size(); i++) {

                Technician current = technicians.get(i);

                if (current.getId().equals(technician.getId())) {
                    technicians.remove(i);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                Notification.show(
                        "Technician no longer exists."
                );
                dialog.close();
                refreshGrid();
                return;
            }

            try {

                fileManager.saveTechnicians(technicians);

                dialog.close();
                refreshGrid();

                Notification.show(
                        "Technician deleted successfully."
                );

            } catch (Exception error) {

                Notification.show(
                        "Error deleting technician: "
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
