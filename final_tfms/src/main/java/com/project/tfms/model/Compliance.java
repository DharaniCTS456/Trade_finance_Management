package com.project.tfms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDate;
import java.util.Objects; // Import for Objects.hash and Objects.equals

@Entity
public class Compliance {

    public enum ComplianceStatus {
        Compliant,
        Non_Compliant
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complianceId;

    private String transactionReference;

    @Enumerated(EnumType.STRING)
    private ComplianceStatus complianceStatus;

    @Column(length = 255)
    private String remarks;

    private LocalDate reportDate;

    // --- Constructors ---

    // Default constructor (required by JPA)
    public Compliance() {
    }

    // Parameterized constructor to match test usage (assuming 'string' maps to status and 'string2' to remarks)
    public Compliance(Long complianceId, String transactionReference, String complianceStatusString, String remarks) {
        this.complianceId = complianceId;
        this.transactionReference = transactionReference;
        // Convert string to enum
        try {
            this.complianceStatus = ComplianceStatus.valueOf(complianceStatusString.replace(" ", "_")); // Handle spaces if any
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid ComplianceStatus string: " + complianceStatusString);
            // Optionally set to a default or throw a more specific exception
            this.complianceStatus = null; // Or ComplianceStatus.Non_Compliant;
        }
        this.remarks = remarks;
        this.reportDate = LocalDate.now(); // Automatically set report date
    }

    // You might also want a constructor without complianceId for new entities
    public Compliance(String transactionReference, ComplianceStatus complianceStatus, String remarks, LocalDate reportDate) {
        this.transactionReference = transactionReference;
        this.complianceStatus = complianceStatus;
        this.remarks = remarks;
        this.reportDate = reportDate;
    }

    // --- Getters and Setters ---

    public Long getComplianceId() {
        return complianceId;
    }

    public void setComplianceId(Long complianceId) {
        this.complianceId = complianceId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public ComplianceStatus getComplianceStatus() {
        return complianceStatus;
    }

    public void setComplianceStatus(ComplianceStatus complianceStatus) {
        this.complianceStatus = complianceStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    // --- Utility Methods ---

    @Override
    public String toString() {
        return "Compliance{" +
               "complianceId=" + complianceId +
               ", transactionReference='" + transactionReference + '\'' +
               ", complianceStatus=" + complianceStatus +
               ", remarks='" + remarks + '\'' +
               ", reportDate=" + reportDate +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compliance that = (Compliance) o;
        return Objects.equals(complianceId, that.complianceId) &&
               Objects.equals(transactionReference, that.transactionReference) &&
               complianceStatus == that.complianceStatus &&
               Objects.equals(remarks, that.remarks) &&
               Objects.equals(reportDate, that.reportDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(complianceId, transactionReference, complianceStatus, remarks, reportDate);
    }
}