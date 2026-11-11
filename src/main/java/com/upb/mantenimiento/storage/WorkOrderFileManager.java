
package com.upb.mantenimiento.storage;

import com.upb.mantenimiento.models.WorkOrder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkOrderFileManager {

    // Location of the work orders text file
    private final Path filePath =
            Paths.get("data", "work-orders.txt");

    // Create sample orders only if the file does not exist
    public WorkOrderFileManager() {
        if (!Files.exists(filePath)) {
            createSampleData();
        }
    }

    // Load all work orders from the text file
    public List<WorkOrder> loadWorkOrders() {

        List<WorkOrder> orders = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return orders;
        }

        try {

            List<String> lines = Files.readAllLines(
                    filePath,
                    StandardCharsets.UTF_8
            );

            for (String line : lines) {

                if (line.isBlank()) {
                    continue;
                }

                try {

                    String[] fields = line.split("\\|", -1);

                    // Every work order must contain 10 fields
                    if (fields.length != 10) {
                        System.out.println(
                                "Invalid work order record: " + line
                        );
                        continue;
                    }

                    String id = fields[0].trim();
                    String equipmentCode = fields[1].trim();
                    String technicianId = fields[2].trim();
                    String maintenanceType = fields[3].trim();
                    String failureDescription = fields[4].trim();

                    LocalDate scheduledDate =
                            LocalDate.parse(fields[5]);

                    String status = fields[6].trim();

                    double partsCost =
                            Double.parseDouble(fields[7]);

                    double laborCost =
                            Double.parseDouble(fields[8]);

                    // Completion date may be empty
                    LocalDate completionDate = null;

                    if (!fields[9].isBlank()) {
                        completionDate =
                                LocalDate.parse(fields[9]);
                    }

                    // Validate required information
                    if (id.isEmpty()
                            || equipmentCode.isEmpty()
                            || technicianId.isEmpty()
                            || failureDescription.isEmpty()) {

                        System.out.println(
                                "Missing work order information: " + line
                        );
                        continue;
                    }

                    // Validate maintenance type
                    if (!maintenanceType.equals("Preventive")
                            && !maintenanceType.equals("Corrective")) {

                        System.out.println(
                                "Invalid maintenance type: " + line
                        );
                        continue;
                    }

                    // Validate order status
                    if (!status.equals("Scheduled")
                            && !status.equals("In Progress")
                            && !status.equals("Completed")) {

                        System.out.println(
                                "Invalid work order status: " + line
                        );
                        continue;
                    }

                    // Validate maintenance costs
                    if (!Double.isFinite(partsCost)
                            || !Double.isFinite(laborCost)
                            || partsCost < 0
                            || laborCost < 0) {

                        System.out.println(
                                "Invalid work order costs: " + line
                        );
                        continue;
                    }

                    // Validate completion information
                    if (status.equals("Completed")
                            && completionDate == null) {

                        System.out.println(
                                "Completed order missing completion date: "
                                + line
                        );
                        continue;
                    }

                    if (!status.equals("Completed")
                            && completionDate != null) {

                        System.out.println(
                                "Unfinished order has completion date: "
                                + line
                        );
                        continue;
                    }

                    WorkOrder order = new WorkOrder(
                            id,
                            equipmentCode,
                            technicianId,
                            maintenanceType,
                            failureDescription,
                            scheduledDate,
                            status,
                            partsCost,
                            laborCost,
                            completionDate
                    );

                    orders.add(order);

                } catch (Exception error) {

                    // Skip damaged records without crashing the app
                    System.out.println(
                            "Could not read work order record: "
                            + error.getMessage()
                    );
                }
            }

        } catch (IOException error) {

            System.out.println(
                    "Error reading work orders file: "
                    + error.getMessage()
            );
        }

        return orders;
    }

    // Save all work orders to the text file
    public void saveWorkOrders(List<WorkOrder> orders) {

        List<String> lines = new ArrayList<>();

        for (WorkOrder order : orders) {

            // Validate important information before saving
            if (order.getId() == null
                    || order.getId().isBlank()
                    || order.getEquipmentCode() == null
                    || order.getEquipmentCode().isBlank()
                    || order.getTechnicianId() == null
                    || order.getTechnicianId().isBlank()
                    || order.getMaintenanceType() == null
                    || order.getFailureDescription() == null
                    || order.getFailureDescription().isBlank()
                    || order.getScheduledDate() == null
                    || order.getStatus() == null
                    || !Double.isFinite(order.getPartsCost())
                    || !Double.isFinite(order.getLaborCost())
                    || order.getPartsCost() < 0
                    || order.getLaborCost() < 0) {

                throw new IllegalArgumentException(
                        "Invalid work order information."
                );
            }

            if (!order.getMaintenanceType().equals("Preventive")
                    && !order.getMaintenanceType().equals("Corrective")) {

                throw new IllegalArgumentException(
                        "Invalid maintenance type."
                );
            }

            if (!order.getStatus().equals("Scheduled")
                    && !order.getStatus().equals("In Progress")
                    && !order.getStatus().equals("Completed")) {

                throw new IllegalArgumentException(
                        "Invalid work order status."
                );
            }

            if (order.isCompleted()
                    && order.getCompletionDate() == null) {

                throw new IllegalArgumentException(
                        "Completed orders require a completion date."
                );
            }

            if (!order.isCompleted()
                    && order.getCompletionDate() != null) {

                throw new IllegalArgumentException(
                        "Unfinished orders cannot have a completion date."
                );
            }

            // Convert optional dates into text
            String completionDate = "";

            if (order.getCompletionDate() != null) {
                completionDate =
                        order.getCompletionDate().toString();
            }

            // Create the text file record
            String line =
                    cleanText(order.getId()) + "|"
                    + cleanText(order.getEquipmentCode()) + "|"
                    + cleanText(order.getTechnicianId()) + "|"
                    + cleanText(order.getMaintenanceType()) + "|"
                    + cleanText(order.getFailureDescription()) + "|"
                    + order.getScheduledDate() + "|"
                    + cleanText(order.getStatus()) + "|"
                    + order.getPartsCost() + "|"
                    + order.getLaborCost() + "|"
                    + completionDate;

            lines.add(line);
        }

        try {

            Files.createDirectories(filePath.getParent());

            Files.write(
                    filePath,
                    lines,
                    StandardCharsets.UTF_8
            );

        } catch (IOException error) {

            throw new IllegalStateException(
                    "Could not save work order data.",
                    error
            );
        }
    }

    // Remove characters that could damage the text file format
    private String cleanText(String text) {

        if (text == null) {
            return "";
        }

        return text.replace("|", " ")
                   .replace("\n", " ")
                   .replace("\r", " ");
    }

    // Create sample orders on first startup
    private void createSampleData() {

        List<WorkOrder> samples = new ArrayList<>();

        // Example preventive maintenance order
        samples.add(new WorkOrder(
                "WO-001",
                "EQ-001",
                "TECH-001",
                "Preventive",
                "Routine inspection and oil filter check",
                LocalDate.now().plusDays(2),
                "Scheduled",
                0,
                0,
                null
        ));

        // Example corrective maintenance order
        samples.add(new WorkOrder(
                "WO-002",
                "EQ-002",
                "TECH-002",
                "Corrective",
                "Hydraulic system pressure failure",
                LocalDate.now().plusDays(4),
                "Scheduled",
                0,
                0,
                null
        ));

        saveWorkOrders(samples);

        System.out.println(
                "Sample work order data created successfully."
        );
    }
}
