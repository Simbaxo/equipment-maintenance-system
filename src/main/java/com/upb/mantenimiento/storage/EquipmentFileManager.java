
package com.upb.mantenimiento.storage;

import com.upb.mantenimiento.models.Equipment;
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
public class EquipmentFileManager {

    // Location of the equipment text file
    private final Path filePath = Paths.get("data", "equipment.txt");

    // Create sample data if the file does not exist
    public EquipmentFileManager() {
        if (!Files.exists(filePath)) {
            createSampleData();
        }
    }

    // Read all equipment records from the text file
    public List<Equipment> loadEquipment() {

        List<Equipment> equipmentList = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return equipmentList;
        }

        try {
            List<String> lines = Files.readAllLines(
                    filePath,
                    StandardCharsets.UTF_8
            );

            for (String line : lines) {

                try {
                    if (line.isBlank()) {
                        continue;
                    }

                    String[] fields = line.split("\\|", -1);

                    // Every equipment record must have 6 fields
                    if (fields.length != 6) {
                        System.out.println("Invalid equipment record: " + line);
                        continue;
                    }

                    Equipment equipment = new Equipment(
                            fields[0],
                            fields[1],
                            fields[2],
                            fields[3],
                            LocalDate.parse(fields[4]),
                            fields[5]
                    );

                    // Ignore records with missing or invalid values
                    if (equipment.getCode().isBlank()
                            || equipment.getName().isBlank()
                            || equipment.getLocation().isBlank()
                            || equipment.getModel().isBlank()) {

                        System.out.println("Invalid equipment values: " + line);
                        continue;
                    }

                    if (!equipment.getStatus().equals("Operational")
                            && !equipment.getStatus().equals("Under Maintenance")
                            && !equipment.getStatus().equals("Out of Service")) {

                        System.out.println("Invalid equipment status: " + line);
                        continue;
                    }

                    equipmentList.add(equipment);

                } catch (Exception error) {
                    // Skip a damaged line without stopping the application
                    System.out.println(
                            "Could not read equipment record: "
                            + error.getMessage()
                    );
                }
            }

        } catch (IOException error) {
            System.out.println(
                    "Error reading equipment file: " + error.getMessage()
            );
        }

        return equipmentList;
    }

    // Save all equipment records to the text file
    public void saveEquipment(List<Equipment> equipmentList) {

        List<String> lines = new ArrayList<>();

        for (Equipment equipment : equipmentList) {

            // Prevent the separator from appearing inside text fields
            String code = cleanText(equipment.getCode());
            String name = cleanText(equipment.getName());
            String location = cleanText(equipment.getLocation());
            String model = cleanText(equipment.getModel());
            String status = cleanText(equipment.getStatus());

            String line = code + "|"
                    + name + "|"
                    + location + "|"
                    + model + "|"
                    + equipment.getAcquisitionDate() + "|"
                    + status;

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
            // Report the failure so a view can show an error notification
            throw new IllegalStateException(
                    "Could not save equipment data.", error
            );
        }
    }

    // Prevent invalid characters inside text file fields
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.replace("|", " ")
                   .replace("\n", " ")
                   .replace("\r", " ");
    }

    // Create sample records on the first run
    private void createSampleData() {

        List<Equipment> samples = new ArrayList<>();

        samples.add(new Equipment(
                "EQ-001",
                "Industrial Compressor",
                "Production Area A",
                "GA15",
                LocalDate.of(2024, 3, 15),
                "Operational"
        ));

        samples.add(new Equipment(
                "EQ-002",
                "Hydraulic Pump",
                "Production Area B",
                "HP200",
                LocalDate.of(2023, 8, 10),
                "Under Maintenance"
        ));

        samples.add(new Equipment(
                "EQ-003",
                "Electric Generator",
                "Storage Area",
                "EG500",
                LocalDate.of(2025, 1, 20),
                "Operational"
        ));

        saveEquipment(samples);

        System.out.println("Sample equipment data created successfully.");
    }
}
