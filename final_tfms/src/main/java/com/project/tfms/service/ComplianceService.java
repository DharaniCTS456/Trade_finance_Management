package com.project.tfms.service;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.tfms.model.Compliance;
import com.project.tfms.model.RiskAssessment;
import com.project.tfms.repository.ComplianceRepository;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
 
@Service
public class ComplianceService {
 
    @Autowired
    private ComplianceRepository complianceRepository;
 
    @Autowired
    private RiskAssessmentService riskAssessmentService; // To get risk score for compliance validation
 
    /**
     * Generates a compliance report for a given transaction based on its risk assessment.
     * This is a simplified logic. In a real system, compliance would involve many rules.
     * For demonstration, if risk score is "Low", it's compliant. Otherwise, non-compliant.
     * @param transactionReference The common reference number for the transaction.
     * @return The generated Compliance object.
     */
    public Compliance generateComplianceReport(String transactionReference) {
        RiskAssessment riskAssessment = riskAssessmentService.analyzeRisk(transactionReference);
 
        Compliance.ComplianceStatus complianceStatus;
        String remarks;
 
        if ("Low".equalsIgnoreCase(riskAssessment.getRiskLevel())) {
            complianceStatus = Compliance.ComplianceStatus.Compliant;
            remarks = "Transaction deemed compliant based on low risk assessment.";
        } else {
            complianceStatus = Compliance.ComplianceStatus.Non_Compliant;
            remarks = "Transaction is non-compliant due to " + riskAssessment.getRiskLevel().toLowerCase() + " risk level. Further review required.";
        }
 
        // Check if a compliance report already exists for this transaction
        Optional<Compliance> existingCompliance = complianceRepository.findByTransactionReference(transactionReference);
        Compliance compliance;
 
        if (existingCompliance.isPresent()) {
            compliance = existingCompliance.get();
            compliance.setComplianceStatus(complianceStatus);
            compliance.setRemarks(remarks);
            compliance.setReportDate(LocalDate.now());
        } else {
            compliance = new Compliance();
            compliance.setTransactionReference(transactionReference);
            compliance.setComplianceStatus(complianceStatus);
            compliance.setRemarks(remarks);
            compliance.setReportDate(LocalDate.now());
        }
 
        return complianceRepository.save(compliance);
    }
 
    /**
     * Submits a regulatory report. This method would typically involve
     * integrating with external reporting systems or generating specific file formats.
     * For this example, it just updates the status of the compliance record.
     * @param complianceId The ID of the compliance record to submit.
     * @return The updated Compliance object.
     * @throws RuntimeException if the compliance record is not found.
     */
    public Compliance submitRegulatoryReport(Long complianceId) {
        Compliance compliance = complianceRepository.findById(complianceId)
                .orElseThrow(() -> new RuntimeException("Compliance report not found with ID: " + complianceId));
        // In a real scenario, this would trigger an actual submission process.
        // For now, we can update a status or log it.
        compliance.setRemarks(compliance.getRemarks() + " (Report submitted)");
        return complianceRepository.save(compliance);
    }
 
    /**
     * Generates a PDF report for the compliance record with the given ID.
     * @param complianceId The ID of the compliance record.
     * @return A byte array representing the PDF content.
     * @throws RuntimeException if the compliance record is not found or PDF generation fails.
     */
    public byte[] generateComplianceReportPdf(Long complianceId) {
        Compliance compliance = complianceRepository.findById(complianceId)
                .orElseThrow(() -> new RuntimeException("Compliance report not found with ID: " + complianceId));
 
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
 
            document.add(new Paragraph("Compliance Report"));
            document.add(new Paragraph("Compliance ID: " + compliance.getComplianceId()));
            document.add(new Paragraph("Transaction Reference: " + compliance.getTransactionReference()));
            document.add(new Paragraph("Status: " + compliance.getComplianceStatus()));
            document.add(new Paragraph("Report Date: " + compliance.getReportDate()));
            document.add(new Paragraph("Remarks: " + compliance.getRemarks()));
 
            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generating PDF report: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error generating PDF report: " + e.getMessage(), e);
        }
    }
 
    /**
     * Retrieves a compliance report by its transaction reference.
     * @param transactionReference The common reference number.
     * @return The Compliance object.
     * @throws RuntimeException if the report is not found.
     */
    public Compliance getComplianceReport(String transactionReference) {
        return complianceRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new RuntimeException("Compliance report not found for transaction: " + transactionReference));
    }
 
    /**
     * Retrieves all compliance reports.
     * @return A list of all Compliance objects.
     */
    public List<Compliance> getAllComplianceReports() {
        return complianceRepository.findAll();
    }
}
 