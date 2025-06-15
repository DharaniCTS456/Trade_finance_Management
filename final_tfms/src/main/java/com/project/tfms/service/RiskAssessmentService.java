package com.project.tfms.service;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.tfms.model.RiskAssessment;
import com.project.tfms.model.LetterOfCredit; // Import LetterOfCredit
import com.project.tfms.model.LetterOfCredit.LCStatus;
import com.project.tfms.repository.RiskAssessmentRepository;
import com.project.tfms.repository.LetterOfCreditRepository; // Autowire LC repository
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
 
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
 
@Service
public class RiskAssessmentService {
 
    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;
 
    @Autowired
    private LetterOfCreditRepository lcRepository; // Autowire LC repository to update LC details
 
    private final ObjectMapper objectMapper = new ObjectMapper();
 
    /**
     * Analyzes risk for a given Letter of Credit (LC) and updates its risk fields.
     * This method fetches the LC, calculates its risk, stores the detailed assessment
     * in the RiskAssessment entity, AND updates the LC entity with summary risk data.
     * @param lcReferenceNumber The reference number of the LC.
     * @return The calculated and saved RiskAssessment object.
     */
    public RiskAssessment analyzeRisk(String lcReferenceNumber) {
        LetterOfCredit lc = lcRepository.findByReferenceNumber(lcReferenceNumber);
        if (lc == null) {
            throw new RuntimeException("Letter of Credit not found for reference number: " + lcReferenceNumber);
        }
 
        // --- Risk Calculation Logic based on LC details ---
        double totalRiskScore = 0.0;
        Map<String, String> factors = new HashMap<>();
 
        // 1. Country Risk Assessment (Example: Higher score = Higher risk)
        Map<String, Integer> COUNTRY_RISK_SCORES = new HashMap<>();
        COUNTRY_RISK_SCORES.put("North Korea", 100);
        COUNTRY_RISK_SCORES.put("Syria", 10);
        COUNTRY_RISK_SCORES.put("Iran", 10);
        COUNTRY_RISK_SCORES.put("Afghanistan", 85);
        COUNTRY_RISK_SCORES.put("Somalia", 80);
        COUNTRY_RISK_SCORES.put("Venezuela", 70);
        COUNTRY_RISK_SCORES.put("Russia", 50);
        COUNTRY_RISK_SCORES.put("Ukraine", 25);
        COUNTRY_RISK_SCORES.put("Pakistan", 20);
        COUNTRY_RISK_SCORES.put("India", 10); // Example
        COUNTRY_RISK_SCORES.put("USA", 5);
        COUNTRY_RISK_SCORES.put("Germany", 3);
        COUNTRY_RISK_SCORES.put("Japan", 2);
        // Add more countries as needed with their respective risk scores
 
        int applicantCountryRisk = COUNTRY_RISK_SCORES.getOrDefault(lc.getApplicantCountry(), 10);
        int beneficiaryCountryRisk = COUNTRY_RISK_SCORES.getOrDefault(lc.getBeneficiaryCountry(),10);
        double countryRiskContribution = (applicantCountryRisk + beneficiaryCountryRisk) / 2.0;
        totalRiskScore += countryRiskContribution * 0.2;
        factors.put("applicantCountryRisk", lc.getApplicantCountry() + " (" + applicantCountryRisk + ")");
        factors.put("beneficiaryCountryRisk", lc.getBeneficiaryCountry() + " (" + beneficiaryCountryRisk + ")");
 
        // 2. Amount Risk Assessment (higher amount = higher risk contribution)
        double maxExpectedAmount = 10000000000000.0; // Adjust max expected amount
        double amountRiskContribution = 0.0;
        if (lc.getAmount() != null && lc.getAmount() > 0) {
            amountRiskContribution = (lc.getAmount() / maxExpectedAmount) * 50;
        }
        totalRiskScore += amountRiskContribution * 0.3;
        factors.put("amount", String.valueOf(lc.getAmount()));
        factors.put("amountRiskContribution", String.format("%.2f", amountRiskContribution));
 
        // 3. LC Status Risk Multiplier
        Map<LCStatus, Double> STATUS_RISK_MULTIPLIERS = new HashMap<>();
        STATUS_RISK_MULTIPLIERS.put(LCStatus.PENDING, 1.2);
        STATUS_RISK_MULTIPLIERS.put(LCStatus.PENDING_AMENDMENT, 1.5);
        STATUS_RISK_MULTIPLIERS.put(LCStatus.ISSUED, 1.0);
        STATUS_RISK_MULTIPLIERS.put(LCStatus.AMENDED, 1.1);
        STATUS_RISK_MULTIPLIERS.put(LCStatus.REJECTED, 2.0);
        STATUS_RISK_MULTIPLIERS.put(LCStatus.CLOSED, 0.5);
 
        double statusMultiplier = STATUS_RISK_MULTIPLIERS.getOrDefault(lc.getStatus(), 1.0);
        totalRiskScore *= statusMultiplier;
        factors.put("lcStatus", lc.getStatus().getDisplayName());
        factors.put("statusMultiplier", String.valueOf(statusMultiplier));
 
        // Ensure score is within a reasonable range, e.g., 0-100
        double finalRiskScore = Math.min(100.0, Math.max(0.0, totalRiskScore));
 
        String riskLevel;
        if (finalRiskScore >= 75) {
            riskLevel = "Very High";
        } else if (finalRiskScore >= 50) {
            riskLevel = "High";
        } else if (finalRiskScore >= 25) {
            riskLevel = "Moderate";
        } else {
            riskLevel = "Low";
        }
        factors.put("finalRiskScore", String.format("%.2f", finalRiskScore));
        factors.put("riskLevel", riskLevel);
        // --- End Risk Calculation Logic ---
 
     // Assuming 'factors' is a Map<String, String> or Map<String, Object>
     // Example: Map<String, String> factors = new HashMap<>();
     // factors.put("factor1", "value1");
     // factors.put("factor2", "value2");
 
     // Convert risk factors map to a normal string paragraph
     String riskFactorsString;
     if (factors != null && !factors.isEmpty()) {
         riskFactorsString = factors.entrySet().stream()
                                    .map(entry -> "- " + entry.getKey() + ": " + entry.getValue())
                                    .collect(Collectors.joining("\n"));
     } else {
         riskFactorsString = "No specific risk factors identified."; // Or an empty string, depending on your preference
     }
 
     // Check if an assessment already exists for this LC's reference number
     Optional<RiskAssessment> existingAssessment = riskAssessmentRepository.findByTransactionReference(lcReferenceNumber);
     RiskAssessment assessment;
 
     if (existingAssessment.isPresent()) {
         assessment = existingAssessment.get();
         assessment.setRiskFactors(riskFactorsString); // Now storing the formatted string
         assessment.setRiskScore(finalRiskScore);
         assessment.setRiskLevel(riskLevel);
         assessment.setAssessmentDate(LocalDateTime.now());
     } else {
         assessment = new RiskAssessment();
         assessment.setTransactionReference(lcReferenceNumber);
         assessment.setRiskFactors(riskFactorsString); // Now storing the formatted string
         assessment.setRiskScore(finalRiskScore);
         assessment.setRiskLevel(riskLevel);
         assessment.setAssessmentDate(LocalDateTime.now());
     }
 
     // IMPORTANT: Update the LetterOfCredit object's risk fields and save it
     lc.setRiskScore(finalRiskScore);
     lc.setRiskAssessment(riskLevel); // Store the risk level summary
     lcRepository.save(lc); // Save the updated LC
 
     return riskAssessmentRepository.save(assessment);}// Save/update the detailed RiskAssessment entity
    /**
     * Retrieves the latest risk assessment for a given transaction reference number.
     * @param transactionReference The common reference number for the LC.
     * @return The RiskAssessment object.
     * @throws RuntimeException if no assessment is found.
     */
    public RiskAssessment getRiskAssessmentByReferenceNumber(String transactionReference) {
        return riskAssessmentRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new RuntimeException("Risk assessment not found for transaction: " + transactionReference));
    }
 
    // You might also want a method to get a specific risk code or level if needed elsewhere
    public String getRiskCode(double riskScore) {
        if (riskScore >= 75) {
            return "R-VH"; // Very High Risk
        } else if (riskScore >= 50) {
            return "R-H";  // High Risk
        } else if (riskScore >= 25) {
            return "R-M";  // Moderate Risk
        } else {
            return "R-L";  // Low Risk
        }
    }
}