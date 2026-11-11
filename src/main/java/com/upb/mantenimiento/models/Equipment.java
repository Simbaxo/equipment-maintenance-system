
package com.upb.mantenimiento.models;

import java.time.LocalDate;

public class Equipment {

    // Equipment information
    private String code;
    private String name;
    private String location;
    private String model;
    private LocalDate acquisitionDate;
    private String status;

    // Empty constructor
    public Equipment() {
    }

    // Constructor with all equipment information
    public Equipment(String code, String name,
                     String location, String model,
                     LocalDate acquisitionDate,
                     String status) {

        this.code = code;
        this.name = name;
        this.location = location;
        this.model = model;
        this.acquisitionDate = acquisitionDate;
        this.status = status;
    }

    // Get the equipment code
    public String getCode() {
        return code;
    }

    // Update the equipment code
    public void setCode(String code) {
        this.code = code;
    }

    // Get the equipment name
    public String getName() {
        return name;
    }

    // Update the equipment name
    public void setName(String name) {
        this.name = name;
    }

    // Get the equipment location
    public String getLocation() {
        return location;
    }

    // Update the equipment location
    public void setLocation(String location) {
        this.location = location;
    }

    // Get the equipment model
    public String getModel() {
        return model;
    }

    // Update the equipment model
    public void setModel(String model) {
        this.model = model;
    }

    // Get the acquisition date
    public LocalDate getAcquisitionDate() {
        return acquisitionDate;
    }

    // Update the acquisition date
    public void setAcquisitionDate(LocalDate acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    // Get the equipment status
    public String getStatus() {
        return status;
    }

    // Update the equipment status
    public void setStatus(String status) {
        this.status = status;
    }
}
