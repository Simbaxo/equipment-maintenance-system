
package com.upb.mantenimiento.storage;

import com.upb.mantenimiento.models.Technician;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class TechnicianFileManager {

    // Location of the technician text file
    private final Path filePath = Paths.get("data", "technicians.txt");

    // Create sample data if the file does not exist
    public TechnicianFileManager() {
        if (!Files.exists(filePath)) {
            createSampleData();
        }
    }

    // Read all technician records from the text file
    public List<Technician> loadTechnicians() {

        List<Technician> technicians = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return technicians;
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

                    // Every technician record must have five fields
                    if (fields.length != 5) {
                        System.out.println(
                                "Invalid technician record: " + line
                        );
                        continue;
                    }

                    String id = fields[0].trim();
                    String name = fields[1].trim();
                    String specialty = fields[2].trim();
                    String phone = fields[3].trim();
                    String status = fields[4].trim();

                    // Validate required fields
                    if (id.isEmpty()
                            || name.isEmpty()
                            || specialty.isEmpty()
                            || phone.isEmpty()) {

                        System.out.println(
                                "Missing technician information: " + line
                        );
                        continue;
                    }

                    // Validate technician status
                    if (!status.equals("Active")
                            && !status.equals("Inactive")) {

                        System.out.println(
                                "Invalid technician status: " + line
                        );
                        continue;
                    }

                    Technician technician = new Technician(
                            id,
                            name,
                            specialty,
                            phone,
                            status
                    );

                    technicians.add(technician);

                } catch (Exception error) {

                    // Skip damaged records without crashing
                    System.out.println(
                            "Error reading technician record: "
                            + error.getMessage()
                    );
                }
            }

        } catch (IOException error) {

            System.out.println(
                    "Error reading technician file: "
                    + error.getMessage()
            );
        }

        return technicians;
    }

    // Save all technician records to the text file
    public void saveTechnicians(List<Technician> technicians) {

        List<String> lines = new ArrayList<>();

        for (Technician technician : technicians) {

            String line = cleanText(technician.getId()) + "|"
                    + cleanText(technician.getName()) + "|"
                    + cleanText(technician.getSpecialty()) + "|"
                    + cleanText(technician.getPhone()) + "|"
                    + cleanText(technician.getStatus());

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

            // Let the interface report any save errors
            throw new IllegalStateException(
                    "Could not save technician data.",
                    error
            );
        }
    }

    // Remove characters that could damage the file format
    private String cleanText(String text) {

        if (text == null) {
            return "";
        }

        return text.replace("|", " ")
                   .replace("\n", " ")
                   .replace("\r", " ");
    }

    // Create sample technicians on the first run
    private void createSampleData() {

        List<Technician> samples = new ArrayList<>();

        samples.add(new Technician(
                "TECH-001",
                "Carlos Martinez",
                "Mechanical Maintenance",
                "3001234567",
                "Active"
        ));

        samples.add(new Technician(
                "TECH-002",
                "Ana Rodriguez",
                "Electrical Maintenance",
                "3009876543",
                "Active"
        ));

        samples.add(new Technician(
                "TECH-003",
                "Juan Lopez",
                "General Maintenance",
                "3015551234",
                "Active"
        ));

        saveTechnicians(samples);

        System.out.println(
                "Sample technician data created successfully."
        );
    }
}
