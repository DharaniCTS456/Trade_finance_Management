package com.project.Tfms.Controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.project.tfms.controller.RiskAssessmentController;
import com.project.tfms.model.LetterOfCredit;
import com.project.tfms.model.RiskAssessment;
import com.project.tfms.service.LetterOfCreditService;
import com.project.tfms.service.RiskAssessmentService;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentControllerTest {

    @Mock
    private RiskAssessmentService riskAssessmentService;

    @Mock
    private LetterOfCreditService lcService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private RiskAssessmentController riskAssessmentController;

    private LetterOfCredit mockLc;
    private RiskAssessment mockRiskAssessment;

    @BeforeEach
    void setUp() {
        // Mock LetterOfCredit object
        mockLc = new LetterOfCredit();
        mockLc.setLcId(1L);
        mockLc.setReferenceNumber("LC123");

        // Mock RiskAssessment object
        mockRiskAssessment = new RiskAssessment();
        mockRiskAssessment.setRiskLevel("High");
    }

    @Test
    void testAnalyzeLcRisk_AdminNotLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("loggedInAdmin")).thenReturn(null); // No admin logged in

        String result = riskAssessmentController.analyzeLcRisk(1L, redirectAttributes, session);
        assertEquals("redirect:/alogin", result);
    }

    @Test
    void testAnalyzeLcRisk_LcReferenceMissing_ShouldRedirectWithError() {
        when(session.getAttribute("loggedInAdmin")).thenReturn("admin");
        mockLc.setReferenceNumber(null);
        when(lcService.getLetterOfCreditById(1L)).thenReturn(mockLc);

        String result = riskAssessmentController.analyzeLcRisk(1L, redirectAttributes, session);
        assertEquals("redirect:/lc/admin/all", result);
    }

    @Test
    void testAnalyzeLcRisk_SuccessfulAnalysis_ShouldRedirectToRiskScore() {
        when(session.getAttribute("loggedInAdmin")).thenReturn("admin");
        when(lcService.getLetterOfCreditById(1L)).thenReturn(mockLc);
        when(riskAssessmentService.analyzeRisk("LC123")).thenReturn(mockRiskAssessment);

        String result = riskAssessmentController.analyzeLcRisk(1L, redirectAttributes, session);
        assertEquals("redirect:/risk/lc-score/1", result);
    }

    @Test
    void testGetLcRiskScore_AdminNotLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("loggedInAdmin")).thenReturn(null);

        String result = riskAssessmentController.getLcRiskScore(1L, model, session);
        assertEquals("redirect:/alogin", result);
    }

    @Test
    void testGetLcRiskScore_LcNotFound_ShouldReturnErrorView() {
        when(session.getAttribute("loggedInAdmin")).thenReturn("admin");
        when(lcService.getLetterOfCreditById(1L)).thenThrow(new RuntimeException("LC not found"));

        String result = riskAssessmentController.getLcRiskScore(1L, model, session);
        assertEquals("error", result);
    }

    @Test
    void testGetLcRiskScore_SuccessfulRetrieval_ShouldReturnRiskDetailsView() {
        when(session.getAttribute("loggedInAdmin")).thenReturn("admin");
        when(lcService.getLetterOfCreditById(1L)).thenReturn(mockLc);
        when(riskAssessmentService.getRiskAssessmentByReferenceNumber("LC123")).thenReturn(mockRiskAssessment);

        String result = riskAssessmentController.getLcRiskScore(1L, model, session);
        assertEquals("risk/lc_risk_details", result);
    }
}
