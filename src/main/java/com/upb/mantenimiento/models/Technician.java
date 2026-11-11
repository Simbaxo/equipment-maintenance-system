
package com.upb.mantenimiento.models;

public class Technician {

    // Technician information
    private String id;
    private String name;
    private String specialty;
    private String phone;
    private String status;

    // Empty constructor
    public Technician() {
    }

    // Constructor with technician information
    public Technician(String id, String name,
                      String specialty, String phone,
                      String status) {

        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
        this.status = status;
    }

    // Get technician ID
    public String getId() {
        return id;
    }

    // Update technician ID
    public void setId(String id) {
        this.id = id;
    }

    // Get technician name
    public String getName() {
        return name;
    }

    // Update technician name
    public void setName(String name) {
        this.name = name;
    }

    // Get technician specialty
    public String getSpecialty() {
        return specialty;
    }

    // Update technician specialty
    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    // Get technician phone number
    public String getPhone() {
        return phone;
    }

    // Update technician phone number
    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Get technician status
    public String getStatus() {
        return status;
    }

    // Update technician status
    public void setStatus(String status) {
        this.status = status;
    }
}
