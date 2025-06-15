package com.project.tfms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.time.LocalDateTime;

@Entity
@Table(name = "trade_documents")
public class TradeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @Column(nullable = false)
    private String documentType; // "Shipping Bill", "Invoice", "Bill of Lading", "Warehouse Receipt"

    @Column(nullable = false, length = 500) // To store the Google Drive/SharePoint link
    private String documentLink;

    @ManyToOne // Many documents can be uploaded by one customer
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer uploadedBy; // Link to the Customer who uploaded the document

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime uploadDate;

    // Constructors
    public TradeDocument() {
        this.uploadDate = LocalDateTime.now(); // Set current timestamp on creation
    }

    public TradeDocument(String documentType, String documentLink, Customer uploadedBy) {
        this.documentType = documentType;
        this.documentLink = documentLink;
        this.uploadedBy = uploadedBy;
        this.uploadDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public void setDocumentLink(String documentLink) {
        this.documentLink = documentLink;
    }

    public Customer getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(Customer uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public String toString() {
        return "TradeDocument{" +
               "documentId=" + documentId +
               ", documentType='" + documentType + '\'' +
               ", documentLink='" + documentLink + '\'' +
               ", uploadedBy=" + (uploadedBy != null ? uploadedBy.getName() : "N/A") +
               ", uploadDate=" + uploadDate +
               '}';
    }
}