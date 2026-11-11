
package com.upb.mantenimiento.storage;

import com.upb.mantenimiento.models.SparePart;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class SparePartFileManager {

    // Location of the spare parts text file
    private final Path filePath =
            Paths.get("data", "spare-parts.txt");

    // Create sample data only if the file does not exist
    public SparePartFileManager() {
        if (!Files.exists(filePath)) {
            createSampleData();
        }
    }

    // Read spare parts from the text file
    public List<SparePart> loadSpareParts() {

        List<SparePart> parts = new ArrayList<>();

        if (!Files.exists(filePath)) {
            return parts;
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

                    // Each record must contain five fields
                    if (fields.length != 5) {
                        System.out.println(
                                "Invalid spare part record: " + line
                        );
                        continue;
                    }

                    String code = fields[0].trim();
                    String name = fields[1].trim();
                    String description = fields[2].trim();

                    double unitPrice =
                            Double.parseDouble(fields[3]);

                    int stock =
                            Integer.parseInt(fields[4]);

                    // Validate the values in the file
                    if (code.isEmpty()
                            || name.isEmpty()
                            || description.isEmpty()
                            || !Double.isFinite(unitPrice)
                            || unitPrice < 0
                            || stock < 0) {

                        System.out.println(
                                "Invalid spare part values: " + line
                        );
                        continue;
                    }

                    SparePart part = new SparePart(
                            code,
                            name,
                            description,
                            unitPrice,
                            stock
                    );

                    parts.add(part);

                } catch (Exception error) {

                    // Skip damaged records without stopping the app
                    System.out.println(
                            "Could not read spare part record: "
                            + error.getMessage()
                    );
                }
            }

        } catch (IOException error) {

            System.out.println(
                    "Error reading spare parts file: "
                    + error.getMessage()
            );
        }

        return parts;
    }

    // Save all spare parts to the text file
    public void saveSpareParts(List<SparePart> parts) {

        List<String> lines = new ArrayList<>();

        for (SparePart part : parts) {

            // Validate values before writing
            if (part.getCode() == null
                    || part.getCode().isBlank()
                    || part.getName() == null
                    || part.getName().isBlank()
                    || part.getDescription() == null
                    || part.getDescription().isBlank()
                    || !Double.isFinite(part.getUnitPrice())
                    || part.getUnitPrice() < 0
                    || part.getStock() < 0) {

                throw new IllegalArgumentException(
                        "Invalid spare part information."
                );
            }

            String line =
                    cleanText(part.getCode()) + "|"
                    + cleanText(part.getName()) + "|"
                    + cleanText(part.getDescription()) + "|"
                    + part.getUnitPrice() + "|"
                    + part.getStock();

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

            // Notify the interface that saving failed
            throw new IllegalStateException(
                    "Could not save spare parts data.",
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

    // Create sample inventory on first startup
    private void createSampleData() {

        List<SparePart> samples = new ArrayList<>();

        samples.add(new SparePart(
                "PART-001",
                "Oil Filter",
                "Industrial compressor oil filter",
                45000,
                12
        ));

        samples.add(new SparePart(
                "PART-002",
                "Drive Belt",
                "Replacement belt for industrial machines",
                85000,
                8
        ));

        samples.add(new SparePart(
                "PART-003",
                "Hydraulic Seal",
                "Seal for hydraulic pump systems",
                25000,
                15
        ));

        samples.add(new SparePart(
                "PART-004",
                "Motor Bearing",
                "Industrial motor replacement bearing",
                120000,
                4
        ));

        saveSpareParts(samples);

        System.out.println(
                "Sample spare parts data created successfully."
        );
    }
}
