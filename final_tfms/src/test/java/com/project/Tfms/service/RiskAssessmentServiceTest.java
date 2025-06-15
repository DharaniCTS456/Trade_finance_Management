package com.project.Tfms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tfms.model.LetterOfCredit;
import com.project.tfms.model.RiskAssessment;
import com.project.tfms.repository.LetterOfCreditRepository;
import com.project.tfms.repository.RiskAssessmentRepository;
import com.project.tfms.service.RiskAssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class) // Initializes mocks
public class RiskAssessmentServiceTest {
    @Mock // Creates a mock instance of RiskAssessmentRepository
    private RiskAssessmentRepository riskAssessmentRepository;
    @Mock // Creates a mock instance of LetterOfCreditRepository
    private LetterOfCreditRepository lcRepository;
    @InjectMocks // Injects the mocks into RiskAssessmentService
    private RiskAssessmentService riskAssessmentService;
    // A simple ObjectMapper to verify JSON content (optional but good practice)
    private final ObjectMapper objectMapper = new ObjectMapper();
    private LetterOfCredit mockLc;
    private RiskAssessment mockRiskAssessment;
    @BeforeEach
    void setUp() {
        // Initialize a mock LC object for consistent testing
        mockLc = new LetterOfCredit();
        mockLc.setLcId(1L);
        mockLc.setReferenceNumber("LC-REF-001");
        mockLc.setApplicantCountry("India");
        mockLc.setBeneficiaryCountry("USA");
        mockLc.setAmount(500000.00);
        mockLc.setStatus(LetterOfCredit.LCStatus.ISSUED); // Assuming default status
        // Initialize a mock RiskAssessment object
        mockRiskAssessment = new RiskAssessment();
        mockRiskAssessment.setRiskId(101L);
        mockRiskAssessment.setTransactionReference("LC-REF-001");
        mockRiskAssessment.setRiskScore(20.0); // Example score
        mockRiskAssessment.setRiskLevel("Low"); // Example level
        mockRiskAssessment.setAssessmentDate(LocalDateTime.now());
    }
    @Test
    @DisplayName("analyzeRisk: Should create a new risk assessment for a valid LC and update LC")
    void analyzeRisk_shouldCreateNewAssessmentAndSaveLc() throws JsonProcessingException {
        // Given
        String lcRef = "LC-REF-001";
        when(lcRepository.findByReferenceNumber(lcRef)).thenReturn(mockLc);
        when(riskAssessmentRepository.findByTransactionReference(lcRef)).thenReturn(Optional.empty()); // No existing assessment
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved object
        // When
        RiskAssessment result = riskAssessmentService.analyzeRisk(lcRef);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionReference()).isEqualTo(lcRef);
        assertThat(result.getRiskScore()).isBetween(0.0, 100.0); // Check if score is within bounds
        assertThat(result.getRiskLevel()).isIn("Low", "Moderate", "High", "Very High");
        assertThat(result.getAssessmentDate()).isNotNull();
        // Verify that LC was updated and saved
        assertThat(mockLc.getRiskScore()).isEqualTo(result.getRiskScore());
        assertThat(mockLc.getRiskAssessment()).isEqualTo(result.getRiskLevel());
        // Verify repository interactions
        verify(lcRepository, times(1)).findByReferenceNumber(lcRef);
        verify(riskAssessmentRepository, times(1)).findByTransactionReference(lcRef);
        verify(riskAssessmentRepository, times(1)).save(any(RiskAssessment.class));
        verify(lcRepository, times(1)).save(mockLc); // Verify LC was saved
    }
    @Test
    @DisplayName("analyzeRisk: Should update an existing risk assessment for a valid LC and update LC")
    void analyzeRisk_shouldUpdateExistingAssessmentAndSaveLc() throws JsonProcessingException {
        // Given
        String lcRef = "LC-REF-001";
        // Simulate an existing assessment with a different score/level
        RiskAssessment existingAssessment = new RiskAssessment();
        existingAssessment.setRiskId(1L);
        existingAssessment.setTransactionReference(lcRef);
        existingAssessment.setRiskScore(10.0);
        existingAssessment.setRiskLevel("Low");
        existingAssessment.setAssessmentDate(LocalDateTime.now().minusDays(1));
        when(lcRepository.findByReferenceNumber(lcRef)).thenReturn(mockLc);
        when(riskAssessmentRepository.findByTransactionReference(lcRef)).thenReturn(Optional.of(existingAssessment)); // Existing assessment found
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved object
        // When
        RiskAssessment result = riskAssessmentService.analyzeRisk(lcRef);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRiskId()).isEqualTo(existingAssessment.getRiskId()); // Should be the same ID
        assertThat(result.getTransactionReference()).isEqualTo(lcRef);
        assertThat(result.getRiskScore()).isNotEqualTo(10.0); // Score should be re-calculated
        assertThat(result.getRiskLevel()).isNotEqualTo("High"); // Level should be re-calculated if score changed
//        assertThat(result.getAssessmentDate()).isAfter(existingAssessment.getAssessmentDate()); // Date should be updated
        // Verify that LC was updated and saved
        assertThat(mockLc.getRiskScore()).isEqualTo(result.getRiskScore());
        assertThat(mockLc.getRiskAssessment()).isEqualTo(result.getRiskLevel());
        // Verify repository interactions
        verify(lcRepository, times(1)).findByReferenceNumber(lcRef);
        verify(riskAssessmentRepository, times(1)).findByTransactionReference(lcRef);
        verify(riskAssessmentRepository, times(1)).save(existingAssessment); // Verify the existing object was saved
        verify(lcRepository, times(1)).save(mockLc); // Verify LC was saved
    }
    @Test
    @DisplayName("analyzeRisk: Should throw RuntimeException if LC is not found")
    void analyzeRisk_shouldThrowExceptionWhenLcNotFound() {
        // Given
        String lcRef = "NON-EXISTENT-LC";
        when(lcRepository.findByReferenceNumber(lcRef)).thenReturn(null); // LC not found
        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> riskAssessmentService.analyzeRisk(lcRef));
        assertThat(thrown.getMessage()).contains("Letter of Credit not found");
        // Verify no saves occurred
        verify(lcRepository, times(1)).findByReferenceNumber(lcRef);
        verify(riskAssessmentRepository, never()).findByTransactionReference(anyString());
        verify(riskAssessmentRepository, never()).save(any(RiskAssessment.class));
        verify(lcRepository, never()).save(any(LetterOfCredit.class));
    }
 

    @Test
    @DisplayName("getRiskAssessmentByReferenceNumber: Should return assessment when found")
    void getRiskAssessmentByReferenceNumber_shouldReturnAssessmentWhenFound() {
        // Given
        String lcRef = "LC-REF-001";
        when(riskAssessmentRepository.findByTransactionReference(lcRef)).thenReturn(Optional.of(mockRiskAssessment));
        // When
        RiskAssessment result = riskAssessmentService.getRiskAssessmentByReferenceNumber(lcRef);
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionReference()).isEqualTo(lcRef);
        verify(riskAssessmentRepository, times(1)).findByTransactionReference(lcRef);
    }
    @Test
    @DisplayName("getRiskAssessmentByReferenceNumber: Should throw RuntimeException when not found")
    void getRiskAssessmentByReferenceNumber_shouldThrowExceptionWhenNotFound() {
        // Given
        String lcRef = "NON-EXISTENT";
        when(riskAssessmentRepository.findByTransactionReference(lcRef)).thenReturn(Optional.empty());
        // When / Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> riskAssessmentService.getRiskAssessmentByReferenceNumber(lcRef));
        assertThat(thrown.getMessage()).contains("Risk assessment not found");
        verify(riskAssessmentRepository, times(1)).findByTransactionReference(lcRef);
    }
    @Test
    @DisplayName("getRiskCode: Should return correct risk codes for different scores")
    void getRiskCode_shouldReturnCorrectCodes() {
        assertThat(riskAssessmentService.getRiskCode(80.0)).isEqualTo("R-VH");
        assertThat(riskAssessmentService.getRiskCode(75.0)).isEqualTo("R-VH");
        assertThat(riskAssessmentService.getRiskCode(74.9)).isEqualTo("R-H");
        assertThat(riskAssessmentService.getRiskCode(50.0)).isEqualTo("R-H");
        assertThat(riskAssessmentService.getRiskCode(49.9)).isEqualTo("R-M");
        assertThat(riskAssessmentService.getRiskCode(25.0)).isEqualTo("R-M");
        assertThat(riskAssessmentService.getRiskCode(24.9)).isEqualTo("R-L");
        assertThat(riskAssessmentService.getRiskCode(0.0)).isEqualTo("R-L");
    }
}