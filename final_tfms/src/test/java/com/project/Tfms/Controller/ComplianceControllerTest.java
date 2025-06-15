package com.project.Tfms.Controller;

import com.project.tfms.controller.ComplianceController;
import com.project.tfms.model.Compliance; // Make sure this is the correct Compliance model
import com.project.tfms.service.ComplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.time.LocalDate; // Import LocalDate
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ComplianceControllerTest {

    @Mock
    private ComplianceService complianceService;

    @InjectMocks
    private ComplianceController complianceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(complianceController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void showGenerateForm_shouldReturnGenerateView() throws Exception {
        mockMvc.perform(get("/compliance/generate"))
                .andExpect(status().isOk())
                .andExpect(view().name("compliance/generate"));
    }


    @Test
    void generateComplianceReport_error_shouldReturnGenerateViewWithError() throws Exception {
        String referenceNumber = "REF456";
        String errorMessage = "Service error during generation";

        when(complianceService.generateComplianceReport(referenceNumber)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/compliance/generate")
                        .param("referenceNumber", referenceNumber))
                .andExpect(status().isOk())
                .andExpect(view().name("compliance/generate"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Error generating compliance report: " + errorMessage));
    }

    @Test
    void getComplianceReport_success_shouldReturnReportViewWithCompliance() throws Exception {
        String referenceNumber = "REF789";
        // Ensure this Compliance object is from com.project.tfms.model.Compliance
        Compliance mockCompliance = new Compliance(2L, referenceNumber, Compliance.ComplianceStatus.Compliant.name(), "Report content for REF789");

        when(complianceService.getComplianceReport(referenceNumber)).thenReturn(mockCompliance);

        mockMvc.perform(get("/compliance/report/{referenceNumber}", referenceNumber))
                .andExpect(status().isOk())
                .andExpect(view().name("compliance/report"))
                .andExpect(model().attributeExists("complianceReport"))
                .andExpect(model().attribute("complianceReport", mockCompliance));
    }

    @Test
    void getComplianceReport_error_shouldReturnErrorView() throws Exception {
        String referenceNumber = "REF000";
        String errorMessage = "Report not found";

        when(complianceService.getComplianceReport(referenceNumber)).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/compliance/report/{referenceNumber}", referenceNumber))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", errorMessage));
    }

    @Test
    void getAllComplianceReports_shouldReturnListViewWithReports() throws Exception {
        List<Compliance> mockReports = Arrays.asList(
                new Compliance(1L, "REF001", Compliance.ComplianceStatus.Compliant.name(), "Content 1"),
                new Compliance(2L, "REF002", Compliance.ComplianceStatus.Non_Compliant.name(), "Content 2")
        );

        when(complianceService.getAllComplianceReports()).thenReturn(mockReports);

        mockMvc.perform(get("/compliance/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("compliance/list"))
                .andExpect(model().attributeExists("complianceReports"))
                .andExpect(model().attribute("complianceReports", mockReports));
    }
}