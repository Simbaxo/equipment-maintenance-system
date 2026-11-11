
package com.upb.mantenimiento.views;

import com.upb.mantenimiento.MainLayout;
import com.upb.mantenimiento.models.Equipment;
import com.upb.mantenimiento.models.WorkOrder;
import com.upb.mantenimiento.storage.EquipmentFileManager;
import com.upb.mantenimiento.storage.WorkOrderFileManager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route(value = "reports", layout = MainLayout.class)
@PageTitle("Reports | Equipment Maintenance System")
public class ReportsView extends VerticalLayout {

    private final WorkOrderFileManager orderManager;
    private final EquipmentFileManager equipmentManager;

    private final Select<Integer> yearFilter = new Select<>();
    private final Select<String> monthFilter = new Select<>();
    private final Select<String> equipmentFilter = new Select<>();

    private final VerticalLayout reportContent =
            new VerticalLayout();

    public ReportsView(
            WorkOrderFileManager orderManager,
            EquipmentFileManager equipmentManager) {

        this.orderManager = orderManager;
        this.equipmentManager = equipmentManager;

        // Configure the main page
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        H2 title = new H2("Maintenance Cost Reports");

        Paragraph description = new Paragraph(
                "Review completed maintenance costs by equipment and month."
        );

        // Configure year selection
        yearFilter.setLabel("Year");

        List<Integer> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();

        // Include years found in completed orders
        years.add(currentYear);

        for (WorkOrder order : orderManager.loadWorkOrders()) {
            if (order.getCompletionDate() != null) {

                int year = order.getCompletionDate().getYear();

                if (!years.contains(year)) {
                    years.add(year);
                }
            }
        }

        years.sort((first, second) -> second.compareTo(first));

        yearFilter.setItems(years);
        yearFilter.setValue(currentYear);
        yearFilter.setWidthFull();

        // Configure month selection
        monthFilter.setLabel("Month");
        monthFilter.setItems(
                "All Months",
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        );
        monthFilter.setValue("All Months");
        monthFilter.setWidthFull();

        // Configure equipment selection
        equipmentFilter.setLabel("Equipment");

        List<String> equipmentCodes = new ArrayList<>();
        equipmentCodes.add("All Equipment");

        for (Equipment equipment : equipmentManager.loadEquipment()) {
            equipmentCodes.add(equipment.getCode());
        }

        equipmentFilter.setItems(equipmentCodes);
        equipmentFilter.setValue("All Equipment");
        equipmentFilter.setWidthFull();

        equipmentFilter.setItemLabelGenerator(code -> {
            if (code.equals("All Equipment")) {
                return code;
            }
            return code + " - " + getEquipmentName(code);
        });

        // Update reports whenever a filter changes
        yearFilter.addValueChangeListener(event -> updateReports());
        monthFilter.addValueChangeListener(event -> updateReports());
        equipmentFilter.addValueChangeListener(event -> updateReports());

        // Arrange filters responsively
        FormLayout filters = new FormLayout();
        filters.setWidthFull();
        filters.add(yearFilter, monthFilter, equipmentFilter);

        Button refreshButton = new Button("Refresh Reports");
        refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        refreshButton.addClickListener(event -> {
            updateReports();
            Notification.show("Reports updated successfully.");
        });

        reportContent.setPadding(false);
        reportContent.setWidthFull();

        add(
                title,
                description,
                filters,
                refreshButton,
                reportContent
        );

        updateReports();
    }

    // Find an equipment name using its code
    private String getEquipmentName(String code) {

        for (Equipment equipment : equipmentManager.loadEquipment()) {

            if (equipment.getCode().equals(code)) {
                return equipment.getName();
            }
        }

        return code;
    }

    // Format money values
    private String formatMoney(double amount) {
        return String.format("$%,.0f COP", amount);
    }

    // Check whether an order matches the selected filters
    private boolean matchesFilters(WorkOrder order) {

        // Reports only include completed maintenance
        if (!order.isCompleted()
                || order.getCompletionDate() == null) {
            return false;
        }

        Integer selectedYear = yearFilter.getValue();
        String selectedMonth = monthFilter.getValue();
        String selectedEquipment = equipmentFilter.getValue();

        LocalDate completed = order.getCompletionDate();

        if (selectedYear != null
                && completed.getYear() != selectedYear) {
            return false;
        }

        if (selectedMonth != null
                && !selectedMonth.equals("All Months")) {

            String orderMonth = completed.getMonth()
                    .getDisplayName(
                            TextStyle.FULL,
                            Locale.ENGLISH
                    );

            if (!orderMonth.equals(selectedMonth)) {
                return false;
            }
        }

        if (selectedEquipment != null
                && !selectedEquipment.equals("All Equipment")
                && !selectedEquipment.equals(order.getEquipmentCode())) {
            return false;
        }

        return true;
    }

    // Recalculate and display maintenance cost reports
    private void updateReports() {

        reportContent.removeAll();

        List<WorkOrder> filteredOrders = new ArrayList<>();

        double totalParts = 0;
        double totalLabor = 0;

        // Load completed orders matching selected filters
        for (WorkOrder order : orderManager.loadWorkOrders()) {

            if (matchesFilters(order)) {

                filteredOrders.add(order);
                totalParts += order.getPartsCost();
                totalLabor += order.getLaborCost();
            }
        }

        double totalCost = totalParts + totalLabor;

        // Display summary totals
        H3 summaryTitle = new H3("Cost Summary");

        Paragraph orderCount = new Paragraph(
                "Completed Work Orders: " + filteredOrders.size()
        );

        Paragraph partsSummary = new Paragraph(
                "Spare Parts Cost: " + formatMoney(totalParts)
        );

        Paragraph laborSummary = new Paragraph(
                "Labor Cost: " + formatMoney(totalLabor)
        );

        Paragraph totalSummary = new Paragraph(
                "Total Maintenance Cost: " + formatMoney(totalCost)
        );

        totalSummary.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "20px");

        reportContent.add(
                summaryTitle,
                orderCount,
                partsSummary,
                laborSummary,
                totalSummary
        );

        // Display costs grouped by equipment
        reportContent.add(new H3("Costs by Equipment"));

        Grid<CostRow> equipmentGrid = new Grid<>(CostRow.class, false);

        equipmentGrid.addColumn(CostRow::getName)
                .setHeader("Equipment")
                .setAutoWidth(true);

        equipmentGrid.addColumn(CostRow::getCount)
                .setHeader("Orders")
                .setAutoWidth(true);

        equipmentGrid.addColumn(row -> formatMoney(row.getParts()))
                .setHeader("Parts")
                .setAutoWidth(true);

        equipmentGrid.addColumn(row -> formatMoney(row.getLabor()))
                .setHeader("Labor")
                .setAutoWidth(true);

        equipmentGrid.addColumn(row -> formatMoney(row.getTotal()))
                .setHeader("Total")
                .setAutoWidth(true);

        List<CostRow> equipmentRows = new ArrayList<>();

        for (WorkOrder order : filteredOrders) {

            String code = order.getEquipmentCode();
            CostRow matchingRow = null;

            // Find an existing summary for this equipment
            for (CostRow row : equipmentRows) {
                if (row.getId().equals(code)) {
                    matchingRow = row;
                    break;
                }
            }

            // Create a new summary if needed
            if (matchingRow == null) {
                matchingRow = new CostRow(
                        code,
                        code + " - " + getEquipmentName(code)
                );
                equipmentRows.add(matchingRow);
            }

            matchingRow.addOrder(order);
        }

        equipmentGrid.setItems(equipmentRows);
        equipmentGrid.setWidthFull();
        equipmentGrid.setAllRowsVisible(true);

        reportContent.add(equipmentGrid);

        // Display costs grouped by completion month
        reportContent.add(new H3("Costs by Month"));

        Grid<CostRow> monthlyGrid = new Grid<>(CostRow.class, false);

        monthlyGrid.addColumn(CostRow::getName)
                .setHeader("Month")
                .setAutoWidth(true);

        monthlyGrid.addColumn(CostRow::getCount)
                .setHeader("Orders")
                .setAutoWidth(true);

        monthlyGrid.addColumn(row -> formatMoney(row.getParts()))
                .setHeader("Parts")
                .setAutoWidth(true);

        monthlyGrid.addColumn(row -> formatMoney(row.getLabor()))
                .setHeader("Labor")
                .setAutoWidth(true);

        monthlyGrid.addColumn(row -> formatMoney(row.getTotal()))
                .setHeader("Total")
                .setAutoWidth(true);

        List<CostRow> monthlyRows = new ArrayList<>();

        for (WorkOrder order : filteredOrders) {

            YearMonth month = YearMonth.from(
                    order.getCompletionDate()
            );

            String monthId = month.toString();
            CostRow matchingRow = null;

            for (CostRow row : monthlyRows) {
                if (row.getId().equals(monthId)) {
                    matchingRow = row;
                    break;
                }
            }

            if (matchingRow == null) {

                String monthName = month.getMonth()
                        .getDisplayName(
                                TextStyle.FULL,
                                Locale.ENGLISH
                        );

                matchingRow = new CostRow(
                        monthId,
                        monthName + " " + month.getYear()
                );

                monthlyRows.add(matchingRow);
            }

            matchingRow.addOrder(order);
        }

        monthlyRows.sort(
                (first, second) -> first.getId()
                        .compareTo(second.getId())
        );

        monthlyGrid.setItems(monthlyRows);
        monthlyGrid.setWidthFull();
        monthlyGrid.setAllRowsVisible(true);

        reportContent.add(monthlyGrid);

        // Display individual completed work orders
        reportContent.add(new H3("Completed Work Order Details"));

        Grid<WorkOrder> detailsGrid =
                new Grid<>(WorkOrder.class, false);

        detailsGrid.addColumn(WorkOrder::getId)
                .setHeader("Order ID")
                .setAutoWidth(true);

        detailsGrid.addColumn(order ->
                getEquipmentName(order.getEquipmentCode()))
                .setHeader("Equipment")
                .setAutoWidth(true);

        detailsGrid.addColumn(WorkOrder::getMaintenanceType)
                .setHeader("Type")
                .setAutoWidth(true);

        detailsGrid.addColumn(WorkOrder::getCompletionDate)
                .setHeader("Completed")
                .setAutoWidth(true);

        detailsGrid.addColumn(order ->
                formatMoney(order.getTotalCost()))
                .setHeader("Total Cost")
                .setAutoWidth(true);

        detailsGrid.setItems(filteredOrders);
        detailsGrid.setWidthFull();
        detailsGrid.setAllRowsVisible(true);

        reportContent.add(detailsGrid);

        if (filteredOrders.isEmpty()) {
            reportContent.add(
                    new Paragraph(
                            "No completed maintenance orders match the selected filters."
                    )
            );
        }
    }

    // Simple class used to group maintenance costs
    private static class CostRow {

        private String id;
        private String name;
        private int count;
        private double parts;
        private double labor;

        public CostRow(String id, String name) {
            this.id = id;
            this.name = name;
            this.count = 0;
            this.parts = 0;
            this.labor = 0;
        }

        public void addOrder(WorkOrder order) {
            count++;
            parts += order.getPartsCost();
            labor += order.getLaborCost();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }

        public double getParts() {
            return parts;
        }

        public double getLabor() {
            return labor;
        }

        public double getTotal() {
            return parts + labor;
        }
    }
}
