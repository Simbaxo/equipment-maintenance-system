
package com.upb.mantenimiento.storage;

import com.upb.mantenimiento.models.WorkOrderPart;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkOrderPartFileManager {

    // Location of the text file
    private final Path filePath =
            Paths.get("data", "work-order-parts.txt");

    // Create the file if it does not exist
    public WorkOrderPartFileManager() {

        try {
            Files.createDirectories(filePath.getParent());

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

        } catch (IOException error) {
            throw new IllegalStateException(
                    "Could not create work order parts file.",
                    error
            );
        }
    }

    // Read parts used in work orders
    public List<WorkOrderPart> loadWorkOrderParts() {

        List<WorkOrderPart> records = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return records;
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

                    // Each record contains four fields
                    if (fields.length != 4) {
                        System.out.println(
                                "Invalid parts usage record: " + line
                        );
                        continue;
                    }

                    String orderId = fields[0];
                    String partCode = fields[1];
                    int quantity = Integer.parseInt(fields[2]);
                    double unitPrice = Double.parseDouble(fields[3]);

                    // Validate the information
                    if (orderId.isBlank()
                            || partCode.isBlank()
                            || quantity <= 0
                            || !Double.isFinite(unitPrice)
                            || unitPrice < 0) {

                        System.out.println(
                                "Invalid parts usage values: " + line
                        );
                        continue;
                    }

                    WorkOrderPart record = new WorkOrderPart(
                            orderId,
                            partCode,
                            quantity,
                            unitPrice
                    );

                    records.add(record);

                } catch (Exception error) {

                    // Ignore damaged records without crashing
                    System.out.println(
                            "Error reading parts usage: "
                            + error.getMessage()
                    );
                }
            }

        } catch (IOException error) {
            throw new IllegalStateException(
                    "Could not read work order parts file.",
                    error
            );
        }

        return records;
    }

    // Save parts usage records
    public void saveWorkOrderParts(List<WorkOrderPart> records) {

        List<String> lines = new ArrayList<>();

        for (WorkOrderPart record : records) {

            // Validate values before saving
            if (record.getWorkOrderId() == null
                    || record.getWorkOrderId().isBlank()
                    || record.getPartCode() == null
                    || record.getPartCode().isBlank()
                    || record.getQuantity() <= 0
                    || !Double.isFinite(record.getUnitPrice())
                    || record.getUnitPrice() < 0) {

                throw new IllegalArgumentException(
                        "Invalid work order part information."
                );
            }

            // Create one text line for each record
            String line =
                    cleanText(record.getWorkOrderId()) + "|"
                    + cleanText(record.getPartCode()) + "|"
                    + record.getQuantity() + "|"
                    + record.getUnitPrice();

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
                    "Could not save work order parts file.",
                    error
            );
        }
    }

    // Remove characters that could affect the file format
    private String cleanText(String text) {

        return text.replace("|", " ")
                   .replace("\n", " ")
                   .replace("\r", " ");
    }

    // Find parts used in one work order
    public List<WorkOrderPart> getPartsForOrder(String orderId) {

        List<WorkOrderPart> result = new ArrayList<>();

        for (WorkOrderPart record : loadWorkOrderParts()) {

            if (record.getWorkOrderId().equals(orderId)) {
                result.add(record);
            }
        }

        return result;
    }

    // Calculate the parts cost of a work order
    public double calculatePartsCost(String orderId) {

        double total = 0;

        for (WorkOrderPart record : getPartsForOrder(orderId)) {
            total = total + record.getTotalCost();
        }

        return total;
    }
}
