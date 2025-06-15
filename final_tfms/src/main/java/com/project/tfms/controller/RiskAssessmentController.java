package com.project.tfms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.tfms.model.LetterOfCredit; // Import LetterOfCredit
import com.project.tfms.model.RiskAssessment; // Import RiskAssessment
import com.project.tfms.service.LetterOfCreditService; // Import LetterOfCreditService
import com.project.tfms.service.RiskAssessmentService;

import jakarta.servlet.http.HttpSession; // For admin session check

@Controller
@RequestMapping("/risk") // Base path for risk assessment endpoints
public class RiskAssessmentController {

    @Autowired
    private RiskAssessmentService riskAssessmentService;

    @Autowired
    private LetterOfCreditService lcService; // We need this to fetch the LC by ID

    /**
     * Triggers risk analysis for a specific Letter of Credit (LC) by its ID.
     * This endpoint should be accessible by Admins to re-evaluate or generate initial risk.
     * @param lcId The ID of the Letter of Credit.
     * @param redirectAttributes Used for flash messages during redirects.
     * @param session For admin authentication check.
     * @return Redirects to the risk score display page for the LC.
     */
    @PostMapping("/analyze-lc/{lcId}")
    public String analyzeLcRisk(@PathVariable Long lcId, RedirectAttributes redirectAttributes, HttpSession session) {
        // Admin role check
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/alogin"; // Redirect to admin login if not authenticated
        }

        try {
            // Fetch the LetterOfCredit by its ID
            LetterOfCredit lc = lcService.getLetterOfCreditById(lcId);

            // Ensure the LC's reference number is set before analysis (critical for RiskAssessment entity)
            if (lc.getReferenceNumber() == null || lc.getReferenceNumber().isEmpty()) {
                // You might generate one here, or ensure it's generated during LC creation
                // For this example, we'll assume it exists or throw an error
                throw new RuntimeException("LC reference number is missing, cannot perform risk analysis.");
            }

            // Perform or re-perform the risk analysis
            // The analyzeRisk method in RiskAssessmentService currently uses transactionReference (which maps to LC's referenceNumber)
            RiskAssessment assessment = riskAssessmentService.analyzeRisk(lc.getReferenceNumber());

            // Optional: Update LC with the latest risk details if you want to store it there as well
            // lc.setRiskScore(assessment.getRiskScore());
            // lc.setRiskAssessment(assessment.getRiskLevel()); // Or a more detailed assessment
            // lc.setRiskCode(assessment.getRiskLevel()); // You might define a specific risk code based on the level
            // lcService.saveLetterOfCredit(lc); // Need a save method in LcService if you uncomment these

            redirectAttributes.addFlashAttribute("successMessage", "Risk analysis completed for LC ID: " + lcId);
            return "redirect:/risk/lc-score/" + lcId; // Redirect to view the score for this LC ID
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error during risk analysis: " + e.getMessage());
            return "redirect:/lc/admin/all"; // Redirect back to admin LC list on error
        }
    }

    /**
     * Displays the Letter of Credit details along with its latest risk assessment.
     * This page fetches the LC by ID and then retrieves its associated risk assessment.
     * @param lcId The ID of the Letter of Credit.
     * @param model Model to pass data to the view.
     * @param session For admin authentication check.
     * @return The view name for displaying LC and risk assessment details.
     */
    @GetMapping("/lc-score/{lcId}")
    public String getLcRiskScore(@PathVariable Long lcId, Model model, HttpSession session) {
        // Admin role check
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/alogin"; // Redirect to admin login if not authenticated
        }

        try {
            // Fetch the LetterOfCredit object
            LetterOfCredit lc = lcService.getLetterOfCreditById(lcId);
            model.addAttribute("letterOfCredit", lc);

            // Fetch the associated RiskAssessment using the LC's reference number
            RiskAssessment riskAssessment = riskAssessmentService.getRiskAssessmentByReferenceNumber(lc.getReferenceNumber());
            model.addAttribute("riskAssessment", riskAssessment);

            return "risk/lc_risk_details"; // Create this new Thymeleaf template
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Error retrieving LC or risk details: " + e.getMessage());
            return "error"; // A generic error page
        }
    }
}