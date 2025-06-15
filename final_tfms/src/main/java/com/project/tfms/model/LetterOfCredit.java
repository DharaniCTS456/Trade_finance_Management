package com.project.tfms.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.time.LocalDate;

@Entity
public class LetterOfCredit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lcId;
    private String referenceNumber;

    private String applicantName;
    private String beneficiaryName;

    private Double amount;
    private String currency;
    // private String country; // This field might be redundant if applicant/beneficiary countries are sufficient
    private Double riskScore; // T
    // NEW FIELDS FOR COUNTRIES
    private String applicantCountry;
    private String beneficiaryCountry;
    private String riskAssessment; 

    public String getRiskAssessment() {
		return riskAssessment;
	}
	public void setRiskAssessment(String riskAssessment) {
		this.riskAssessment = riskAssessment;
	}
	private LocalDate expiryDate;
    public Double getRiskScore() {
		return riskScore;
	}
	public void setRiskScore(Double riskScore) {
		this.riskScore = riskScore;
	}
	private LocalDate issueDate;

    @Enumerated(EnumType.STRING)
    private LCStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;
    private String applicantBank;
    private String beneficiaryBank;

    private String desiredValidityPeriod;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public enum LCStatus {
        PENDING("Pending Approval"),
        ISSUED("Issued"),
        REJECTED("Rejected"),
        AMENDED("Amended"),
        CLOSED("Closed"),
        PENDING_AMENDMENT("Pending Amendment");

        private final String displayName;

        LCStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // --- Getters and Setters ---
    public Long getLcId() { return lcId; }
    public void setLcId(Long lcId) { this.lcId = lcId; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getBeneficiaryName() { return beneficiaryName; }
    public void setBeneficiaryName(String beneficiaryName) { this.beneficiaryName = beneficiaryName; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    // Removed the generic 'country' field if applicant/beneficiary countries fulfill the need
    // public String getCountry() { return country; }
    // public void setCountry(String country) { this.country = country; }

    // NEW GETTERS AND SETTERS FOR APPLICANT AND BENEFICIARY COUNTRIES
    public String getApplicantCountry() { return applicantCountry; }
    public void setApplicantCountry(String applicantCountry) { this.applicantCountry = applicantCountry; }

    public String getBeneficiaryCountry() { return beneficiaryCountry; }
    public void setBeneficiaryCountry(String beneficiaryCountry) { this.beneficiaryCountry = beneficiaryCountry; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public LCStatus getStatus() { return status; }
    public void setStatus(LCStatus status) { this.status = status; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getApplicantBank() { return applicantBank; }
    public void setApplicantBank(String applicantBank) { this.applicantBank = applicantBank; }

    public String getBeneficiaryBank() { return beneficiaryBank; }
    public void setBeneficiaryBank(String beneficiaryBank) { this.beneficiaryBank = beneficiaryBank; }

    public String getDesiredValidityPeriod() {
        return desiredValidityPeriod;
    }

    public void setDesiredValidityPeriod(String desiredValidityPeriod) {
        this.desiredValidityPeriod = desiredValidityPeriod;
    }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}