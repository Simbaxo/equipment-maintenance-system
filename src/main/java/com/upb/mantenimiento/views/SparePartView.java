
package com.upb.mantenimiento.views;

import com.upb.mantenimiento.MainLayout;
import com.upb.mantenimiento.models.SparePart;
import com.upb.mantenimiento.storage.SparePartFileManager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "spare-parts", layout = MainLayout.class)
@PageTitle("Spare Parts | Equipment Maintenance System")
public class SparePartView extends VerticalLayout {

    private final SparePartFileManager fileManager;

    private final Grid<SparePart> grid =
            new Grid<>(SparePart.class, false);

    private final TextField searchField = new TextField();
    private final ComboBox<String> stockFilter = new ComboBox<>();

    public SparePartView(SparePartFileManager fileManager) {

        this.fileManager = fileManager;

        // Configure the page
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        H2 title = new H2("Spare Parts Inventory");

        Paragraph description = new Paragraph(
                "Manage replacement parts, prices, and inventory stock."
        );

        // Create the Add Spare Part button
        Button addButton = new Button("Add Spare Part");
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(event -> openForm(null));

        // Create the live search field
        searchField.setPlaceholder("Search spare parts...");
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.setWidthFull();
        searchField.addValueChangeListener(event -> refreshGrid());

        // Create the inventory stock filter
        stockFilter.setPlaceholder("Filter by stock");
        stockFilter.setItems(
                "All",
                "In Stock",
                "Out of Stock"
        );
        stockFilter.setValue("All");
        stockFilter.setWidthFull();
        stockFilter.addValueChangeListener(event -> refreshGrid());

        // Arrange the search and filter fields
        FormLayout filters = new FormLayout();
        filters.setWidthFull();
        filters.add(searchField, stockFilter);

        // Create the inventory table
        configureGrid();

        add(title, description, addButton, filters, grid);

        refreshGrid();
    }

    // Configure the spare parts table
    private void configureGrid() {

        grid.addColumn(SparePart::getCode)
                .setHeader("Code")
                .setAutoWidth(true);

        grid.addColumn(SparePart::getName)
                .setHeader("Name")
                .setAutoWidth(true);

        grid.addColumn(SparePart::getDescription)
                .setHeader("Description")
                .setAutoWidth(true);

        grid.addColumn(part ->
                String.format("$%,.0f", part.getUnitPrice()))
                .setHeader("Unit Price")
                .setAutoWidth(true);

        grid.addColumn(SparePart::getStock)
                .setHeader("Stock")
                .setAutoWidth(true);

        // Add Edit and Delete actions
        grid.addComponentColumn(part -> {

            Button editButton = new Button("Edit");

            editButton.addClickListener(event ->
                    openForm(part)
            );

            Button deleteButton = new Button("Delete");

            deleteButton.addThemeVariants(
                    ButtonVariant.LUMO_ERROR,
                    ButtonVariant.LUMO_TERTIARY
            );

            deleteButton.addClickListener(event ->
                    confirmDelete(part)
            );

            return new HorizontalLayout(
                    editButton,
                    deleteButton
            );

        }).setHeader("Actions").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");
    }

    // Reload spare parts and apply filters
    private void refreshGrid() {

        List<SparePart> parts = fileManager.loadSpareParts();
        List<SparePart> filtered = new ArrayList<>();

        String search =
                searchField.getValue().trim().toLowerCase();

        String selectedFilter = stockFilter.getValue();

        for (SparePart part : parts) {

            boolean matchesSearch =
                    part.getCode().toLowerCase().contains(search)
                    || part.getName().toLowerCase().contains(search)
                    || part.getDescription().toLowerCase().contains(search);

            boolean matchesStock = true;

            if ("In Stock".equals(selectedFilter)) {
                matchesStock = part.getStock() > 0;
            }

            if ("Out of Stock".equals(selectedFilter)) {
                matchesStock = part.getStock() == 0;
            }

            if (matchesSearch && matchesStock) {
                filtered.add(part);
            }
        }

        grid.setItems(filtered);
    }

    // Open a dialog to add or edit a spare part
    private void openForm(SparePart selectedPart) {

        Dialog dialog = new Dialog();
        dialog.setWidth("650px");
        dialog.setMaxWidth("95vw");

        boolean editing = selectedPart != null;

        H2 title = new H2(
                editing ? "Edit Spare Part" : "Add Spare Part"
        );

        TextField codeField = new TextField("Part Code");
        codeField.setRequiredIndicatorVisible(true);

        TextField nameField = new TextField("Part Name");
        nameField.setRequiredIndicatorVisible(true);

        TextField descriptionField = new TextField("Description");
        descriptionField.setRequiredIndicatorVisible(true);

        NumberField priceField = new NumberField("Unit Price");
        priceField.setMin(0);
        priceField.setRequiredIndicatorVisible(true);

        IntegerField stockField = new IntegerField("Stock");
        stockField.setMin(0);
        stockField.setStepButtonsVisible(true);
        stockField.setRequiredIndicatorVisible(true);

        // Fill the form when editing
        if (editing) {

            codeField.setValue(selectedPart.getCode());
            codeField.setReadOnly(true);

            nameField.setValue(selectedPart.getName());
            descriptionField.setValue(
                    selectedPart.getDescription()
            );

            priceField.setValue(selectedPart.getUnitPrice());
            stockField.setValue(selectedPart.getStock());

        } else {
            priceField.setValue(0.0);
            stockField.setValue(0);
        }

        FormLayout form = new FormLayout();

        form.add(
                codeField,
                nameField,
                descriptionField,
                priceField,
                stockField
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

            String code = codeField.getValue().trim();
            String name = nameField.getValue().trim();
            String description = descriptionField.getValue().trim();

            Double price = priceField.getValue();
            Integer stock = stockField.getValue();

            // Validate required fields
            if (code.isEmpty()
                    || name.isEmpty()
                    || description.isEmpty()
                    || price == null
                    || stock == null) {

                Notification.show(
                        "Please complete all required fields."
                );
                return;
            }

            // Validate the unique part code format
            if (!code.matches("[A-Za-z0-9-]+")) {

                Notification.show(
                        "Part code can only contain letters, numbers, and hyphens."
                );
                return;
            }

            // Validate numeric values
            if (!Double.isFinite(price)
                    || price < 0
                    || stock < 0) {

                Notification.show(
                        "Price and stock values cannot be negative or invalid."
                );
                return;
            }

            // Protect the text file format
            String combinedText = code + name + description;

            if (combinedText.contains("|")
                    || combinedText.contains("\n")
                    || combinedText.contains("\r")) {

                Notification.show(
                        "Fields cannot contain | or line breaks."
                );
                return;
            }

            List<SparePart> parts = fileManager.loadSpareParts();

            if (editing) {

                boolean found = false;

                for (int i = 0; i < parts.size(); i++) {

                    SparePart current = parts.get(i);

                    if (current.getCode().equals(
                            selectedPart.getCode())) {

                        SparePart updated = new SparePart(
                                code,
                                name,
                                description,
                                price,
                                stock
                        );

                        parts.set(i, updated);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Notification.show(
                            "Spare part no longer exists."
                    );
                    return;
                }

            } else {

                // Prevent duplicate spare part codes
                for (SparePart current : parts) {

                    if (current.getCode().equalsIgnoreCase(code)) {

                        Notification.show(
                                "This spare part code already exists."
                        );
                        return;
                    }
                }

                SparePart newPart = new SparePart(
                        code,
                        name,
                        description,
                        price,
                        stock
                );

                parts.add(newPart);
            }

            // Save the updated inventory
            try {

                fileManager.saveSpareParts(parts);

                dialog.close();
                refreshGrid();

                Notification.show(
                        editing
                                ? "Spare part updated successfully."
                                : "Spare part added successfully."
                );

            } catch (Exception error) {

                Notification.show(
                        "Error saving spare part: "
                        + error.getMessage()
                );
            }
        });

        HorizontalLayout buttons = new HorizontalLayout(
                saveButton,
                cancelButton
        );

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

    // Confirm before deleting a spare part
    private void confirmDelete(SparePart part) {

        Dialog dialog = new Dialog();

        H2 title = new H2("Delete Spare Part");

        Paragraph message = new Paragraph(
                "Are you sure you want to delete "
                + part.getName() + "?"
        );

        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(event -> dialog.close());

        Button deleteButton = new Button("Delete");
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        deleteButton.addClickListener(event -> {

            List<SparePart> parts = fileManager.loadSpareParts();

            boolean removed = false;

            // Find and remove the selected spare part
            for (int i = 0; i < parts.size(); i++) {

                SparePart current = parts.get(i);

                if (current.getCode().equals(part.getCode())) {
                    parts.remove(i);
                    removed = true;
                    break;
                }
            }

            if (!removed) {
                Notification.show("Spare part no longer exists.");
                dialog.close();
                refreshGrid();
                return;
            }

            try {

                fileManager.saveSpareParts(parts);

                dialog.close();
                refreshGrid();

                Notification.show(
                        "Spare part deleted successfully."
                );

            } catch (Exception error) {

                Notification.show(
                        "Error deleting spare part: "
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
