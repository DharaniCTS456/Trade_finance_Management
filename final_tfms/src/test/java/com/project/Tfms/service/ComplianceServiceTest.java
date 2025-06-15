package com.project.Tfms.service;

import com.project.tfms.service.*;
import com.project.tfms.model.Compliance;
import com.project.tfms.model.RiskAssessment;
import com.project.tfms.repository.ComplianceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComplianceServiceTest {

    @Mock
    private ComplianceRepository complianceRepository;

    @Mock
    private RiskAssessmentService riskAssessmentService;

    @InjectMocks
    private ComplianceService complianceService;

    private static final String TRANSACTION_REF = "TRN12345";
    private static final Long COMPLIANCE_ID = 1L;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this); // Not strictly necessary with @ExtendWith(MockitoExtension.class)
    }

    @Test
    void generateComplianceReport_lowRisk_newReport() {
        // Arrange
        RiskAssessment lowRiskAssessment = new RiskAssessment();
        lowRiskAssessment.setTransactionReference(TRANSACTION_REF);
        lowRiskAssessment.setRiskLevel("Low");
        lowRiskAssessment.setRiskScore(10.0);

        Compliance expectedCompliance = new Compliance();
        expectedCompliance.setTransactionReference(TRANSACTION_REF);
        expectedCompliance.setComplianceStatus(Compliance.ComplianceStatus.Compliant);
        expectedCompliance.setRemarks("Transaction deemed compliant based on low risk assessment.");
        expectedCompliance.setReportDate(LocalDate.now());

        when(riskAssessmentService.analyzeRisk(TRANSACTION_REF)).thenReturn(lowRiskAssessment);
        when(complianceRepository.findByTransactionReference(TRANSACTION_REF)).thenReturn(Optional.empty());
        when(complianceRepository.save(any(Compliance.class))).thenReturn(expectedCompliance);

        // Act
        Compliance actualCompliance = complianceService.generateComplianceReport(TRANSACTION_REF);

        // Assert
        assertNotNull(actualCompliance);
        assertEquals(Compliance.ComplianceStatus.Compliant, actualCompliance.getComplianceStatus());
        assertEquals(TRANSACTION_REF, actualCompliance.getTransactionReference());
        assertTrue(actualCompliance.getRemarks().contains("low risk assessment"));
        assertEquals(LocalDate.now(), actualCompliance.getReportDate());

        verify(riskAssessmentService, times(1)).analyzeRisk(TRANSACTION_REF);
        verify(complianceRepository, times(1)).findByTransactionReference(TRANSACTION_REF);
        verify(complianceRepository, times(1)).save(any(Compliance.class));
    }

    @Test
    void generateComplianceReport_highRisk_newReport() {
        // Arrange
        RiskAssessment highRiskAssessment = new RiskAssessment();
        highRiskAssessment.setTransactionReference(TRANSACTION_REF);
        highRiskAssessment.setRiskLevel("High");
        highRiskAssessment.setRiskScore(90.0);

        Compliance expectedCompliance = new Compliance();
        expectedCompliance.setTransactionReference(TRANSACTION_REF);
        expectedCompliance.setComplianceStatus(Compliance.ComplianceStatus.Non_Compliant);
        expectedCompliance.setRemarks("Transaction is non-compliant due to high risk level. Further review required.");
        expectedCompliance.setReportDate(LocalDate.now());

        when(riskAssessmentService.analyzeRisk(TRANSACTION_REF)).thenReturn(highRiskAssessment);
        when(complianceRepository.findByTransactionReference(TRANSACTION_REF)).thenReturn(Optional.empty());
        when(complianceRepository.save(any(Compliance.class))).thenReturn(expectedCompliance);

        // Act
        Compliance actualCompliance = complianceService.generateComplianceReport(TRANSACTION_REF);

        // Assert
        assertNotNull(actualCompliance);
        assertEquals(Compliance.ComplianceStatus.Non_Compliant, actualCompliance.getComplianceStatus());
        assertEquals(TRANSACTION_REF, actualCompliance.getTransactionReference());
        assertTrue(actualCompliance.getRemarks().contains("high risk level"));
        assertEquals(LocalDate.now(), actualCompliance.getReportDate());

        verify(riskAssessmentService, times(1)).analyzeRisk(TRANSACTION_REF);
        verify(complianceRepository, times(1)).findByTransactionReference(TRANSACTION_REF);
        verify(complianceRepository, times(1)).save(any(Compliance.class));
    }

    @Test
    void generateComplianceReport_existingReportUpdated() {
        // Arrange
        RiskAssessment mediumRiskAssessment = new RiskAssessment();
        mediumRiskAssessment.setTransactionReference(TRANSACTION_REF);
        mediumRiskAssessment.setRiskLevel("Medium");
        mediumRiskAssessment.setRiskScore(50.0);

        Compliance existingCompliance = new Compliance();
        existingCompliance.setComplianceId(COMPLIANCE_ID);
        existingCompliance.setTransactionReference(TRANSACTION_REF);
        existingCompliance.setComplianceStatus(Compliance.ComplianceStatus.Compliant); // Old status
        existingCompliance.setRemarks("Initial compliant remark.");
        existingCompliance.setReportDate(LocalDate.of(2023, 1, 1)); // Old date

        Compliance updatedCompliance = new Compliance();
        updatedCompliance.setComplianceId(COMPLIANCE_ID);
        updatedCompliance.setTransactionReference(TRANSACTION_REF);
        updatedCompliance.setComplianceStatus(Compliance.ComplianceStatus.Non_Compliant); // New status
        updatedCompliance.setRemarks("Transaction is non-compliant due to medium risk level. Further review required.");
        updatedCompliance.setReportDate(LocalDate.now()); // New date

        when(riskAssessmentService.analyzeRisk(TRANSACTION_REF)).thenReturn(mediumRiskAssessment);
        when(complianceRepository.findByTransactionReference(TRANSACTION_REF)).thenReturn(Optional.of(existingCompliance));
        when(complianceRepository.save(any(Compliance.class))).thenReturn(updatedCompliance);

        // Act
        Compliance actualCompliance = complianceService.generateComplianceReport(TRANSACTION_REF);

        // Assert
        assertNotNull(actualCompliance);
        assertEquals(COMPLIANCE_ID, actualCompliance.getComplianceId());
        assertEquals(Compliance.ComplianceStatus.Non_Compliant, actualCompliance.getComplianceStatus());
        assertEquals(TRANSACTION_REF, actualCompliance.getTransactionReference());
        assertTrue(actualCompliance.getRemarks().contains("medium risk level"));
        assertEquals(LocalDate.now(), actualCompliance.getReportDate());

        verify(riskAssessmentService, times(1)).analyzeRisk(TRANSACTION_REF);
        verify(complianceRepository, times(1)).findByTransactionReference(TRANSACTION_REF);
        verify(complianceRepository, times(1)).save(existingCompliance); // Verify the existing object was saved
    }

    @Test
    void submitRegulatoryReport_success() {
        // Arrange
        Compliance compliance = new Compliance();
        compliance.setComplianceId(COMPLIANCE_ID);
        compliance.setTransactionReference(TRANSACTION_REF);
        compliance.setRemarks("Original remarks.");

        when(complianceRepository.findById(COMPLIANCE_ID)).thenReturn(Optional.of(compliance));
        when(complianceRepository.save(any(Compliance.class))).thenReturn(compliance);

        // Act
        Compliance actualCompliance = complianceService.submitRegulatoryReport(COMPLIANCE_ID);

        // Assert
        assertNotNull(actualCompliance);
        assertTrue(actualCompliance.getRemarks().contains("(Report submitted)"));
        verify(complianceRepository, times(1)).findById(COMPLIANCE_ID);
        verify(complianceRepository, times(1)).save(compliance);
    }

    @Test
    void submitRegulatoryReport_notFound() {
        // Arrange
        when(complianceRepository.findById(COMPLIANCE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> complianceService.submitRegulatoryReport(COMPLIANCE_ID));

        assertEquals("Compliance report not found with ID: " + COMPLIANCE_ID, exception.getMessage());
        verify(complianceRepository, times(1)).findById(COMPLIANCE_ID);
        verify(complianceRepository, never()).save(any(Compliance.class));
    }

    @Test
    void generateComplianceReportPdf_success() {
        // Arrange
        Compliance compliance = new Compliance();
        compliance.setComplianceId(COMPLIANCE_ID);
        compliance.setTransactionReference(TRANSACTION_REF);
        compliance.setComplianceStatus(Compliance.ComplianceStatus.Compliant);
        compliance.setReportDate(LocalDate.now());
        compliance.setRemarks("Test remarks for PDF.");

        when(complianceRepository.findById(COMPLIANCE_ID)).thenReturn(Optional.of(compliance));

        // Act
        byte[] pdfBytes = complianceService.generateComplianceReportPdf(COMPLIANCE_ID);

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // You could add more sophisticated PDF content validation here,
        // but for a unit test, checking for non-empty bytes is often sufficient.
        // For example, you might look for specific byte patterns if you know them.
        verify(complianceRepository, times(1)).findById(COMPLIANCE_ID);
    }

    @Test
    void generateComplianceReportPdf_notFound() {
        // Arrange
        when(complianceRepository.findById(COMPLIANCE_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> complianceService.generateComplianceReportPdf(COMPLIANCE_ID));

        assertEquals("Compliance report not found with ID: " + COMPLIANCE_ID, exception.getMessage());
        verify(complianceRepository, times(1)).findById(COMPLIANCE_ID);
    }

    @Test
    void getComplianceReport_success() {
        // Arrange
        Compliance compliance = new Compliance();
        compliance.setTransactionReference(TRANSACTION_REF);
        compliance.setComplianceId(COMPLIANCE_ID);

        when(complianceRepository.findByTransactionReference(TRANSACTION_REF)).thenReturn(Optional.of(compliance));

        // Act
        Compliance actualCompliance = complianceService.getComplianceReport(TRANSACTION_REF);

        // Assert
        assertNotNull(actualCompliance);
        assertEquals(TRANSACTION_REF, actualCompliance.getTransactionReference());
        assertEquals(COMPLIANCE_ID, actualCompliance.getComplianceId());
        verify(complianceRepository, times(1)).findByTransactionReference(TRANSACTION_REF);
    }

    @Test
    void getComplianceReport_notFound() {
        // Arrange
        when(complianceRepository.findByTransactionReference(TRANSACTION_REF)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> complianceService.getComplianceReport(TRANSACTION_REF));

        assertEquals("Compliance report not found for transaction: " + TRANSACTION_REF, exception.getMessage());
        verify(complianceRepository, times(1)).findByTransactionReference(TRANSACTION_REF);
    }

    @Test
    void getAllComplianceReports_success() {
        // Arrange
        Compliance c1 = new Compliance();
        c1.setComplianceId(1L);
        c1.setTransactionReference("TRN001");
        Compliance c2 = new Compliance();
        c2.setComplianceId(2L);
        c2.setTransactionReference("TRN002");

        List<Compliance> expectedReports = Arrays.asList(c1, c2);

        when(complianceRepository.findAll()).thenReturn(expectedReports);

        // Act
        List<Compliance> actualReports = complianceService.getAllComplianceReports();

        // Assert
        assertNotNull(actualReports);
        assertEquals(2, actualReports.size());
        assertEquals("TRN001", actualReports.get(0).getTransactionReference());
        assertEquals("TRN002", actualReports.get(1).getTransactionReference());
        verify(complianceRepository, times(1)).findAll();
    }

    @Test
    void getAllComplianceReports_emptyList() {
        // Arrange
        when(complianceRepository.findAll()).thenReturn(List.of());

        // Act
        List<Compliance> actualReports = complianceService.getAllComplianceReports();

        // Assert
        assertNotNull(actualReports);
        assertTrue(actualReports.isEmpty());
        verify(complianceRepository, times(1)).findAll();
    }
}
