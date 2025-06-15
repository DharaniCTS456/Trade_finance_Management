package com.project.tfms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; // Add this import for @Table

@Entity
@Table(name = "customers") // Good practice to explicitly name the table
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 15) // Keep phone number
    private String phone;

    @Column(columnDefinition = "TEXT") // Keep address
    private String address;

    @Column(nullable = false, length = 50) // New role field
    private String role; // e.g., "ROLE_CUSTOMER"

    private String password;

    public Customer() {}

    // Updated constructor to include 'role'
    public Customer(String name, String email, String phone, String address, String role, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.role = role; // Initialize role
        this.password = password;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // New getter/setter for role
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) { 
        this.password = password;
    }

    @Override
    public String toString() {
        return "Customer{" +
               "customerId=" + customerId +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", phone='" + phone + '\'' +
               ", address='" + address + '\'' +
               ", role='" + role + '\'' +
               '}';
    }
}