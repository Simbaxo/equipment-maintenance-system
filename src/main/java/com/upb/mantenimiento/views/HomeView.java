
package com.upb.mantenimiento.views;

import com.upb.mantenimiento.MainLayout;
import com.upb.mantenimiento.models.Equipment;
import com.upb.mantenimiento.models.WorkOrder;
import com.upb.mantenimiento.storage.EquipmentFileManager;
import com.upb.mantenimiento.storage.WorkOrderFileManager;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home | Equipment Maintenance System")
public class HomeView extends VerticalLayout {

    private final EquipmentFileManager equipmentManager;
    private final WorkOrderFileManager orderManager;

    private final VerticalLayout dashboardContent =
            new VerticalLayout();

    public HomeView(
            EquipmentFileManager equipmentManager,
            WorkOrderFileManager orderManager) {

        this.equipmentManager = equipmentManager;
        this.orderManager = orderManager;

        // Configure the page
        setPadding(true);
        setSpacing(true);
        setWidthFull();

        H2 title = new H2("Maintenance Dashboard");

        Paragraph description = new Paragraph(
                "Equipment overview and upcoming maintenance."
        );

        Button refreshButton = new Button("Refresh");
        refreshButton.addClickListener(event -> {
            updateDashboard();
            Notification.show("Dashboard updated.");
        });

        dashboardContent.setPadding(false);
        dashboardContent.setWidthFull();

        add(title, description, refreshButton, dashboardContent);

        // Display current information
        updateDashboard();
    }

    // Find the equipment name using its code
    private String getEquipmentName(String code) {

        for (Equipment equipment : equipmentManager.loadEquipment()) {

            if (equipment.getCode().equals(code)) {
                return equipment.getName();
            }
        }

        return code;
    }

    // Display equipment information and maintenance alerts
    private void updateDashboard() {

        dashboardContent.removeAll();

        List<Equipment> equipmentList =
                equipmentManager.loadEquipment();

        List<WorkOrder> orders =
                orderManager.loadWorkOrders();

        List<WorkOrder> upcomingOrders = new ArrayList<>();

        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);

        int openOrders = 0;

        // Check all work orders
        for (WorkOrder order : orders) {

            if (!order.isCompleted()) {

                openOrders++;

                LocalDate date = order.getScheduledDate();

                // Find maintenance scheduled within seven days
                if (date != null
                        && !date.isBefore(today)
                        && !date.isAfter(nextWeek)) {

                    upcomingOrders.add(order);
                }
            }
        }

        // Display summary information
        dashboardContent.add(
                new H3("Overview"),
                new Paragraph(
                        "Total Equipment: " + equipmentList.size()
                ),
                new Paragraph(
                        "Open Work Orders: " + openOrders
                ),
                new Paragraph(
                        "Maintenance in Next 7 Days: "
                        + upcomingOrders.size()
                )
        );

        dashboardContent.add(
                new H3("Upcoming Maintenance")
        );

        // Display maintenance alerts
        if (upcomingOrders.isEmpty()) {

            dashboardContent.add(
                    new Paragraph(
                            "No maintenance scheduled in the next 7 days."
                    )
            );

        } else {

            Grid<WorkOrder> grid =
                    new Grid<>(WorkOrder.class, false);

            grid.addColumn(WorkOrder::getId)
                    .setHeader("Order ID");

            grid.addColumn(order ->
                    getEquipmentName(order.getEquipmentCode()))
                    .setHeader("Equipment");

            grid.addColumn(WorkOrder::getMaintenanceType)
                    .setHeader("Type");

            grid.addColumn(WorkOrder::getScheduledDate)
                    .setHeader("Scheduled Date");

            grid.addColumn(WorkOrder::getStatus)
                    .setHeader("Status");

            grid.setItems(upcomingOrders);
            grid.setWidthFull();
            grid.setAllRowsVisible(true);

            dashboardContent.add(grid);
        }
    }
}
