  
package com.project.tfms.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.project.tfms.model.Compliance;
import java.util.List;
import com.project.tfms.service.*;
 
@Controller
@RequestMapping("/compliance") // Base path for compliance endpoints
public class ComplianceController {
 
    @Autowired
    private ComplianceService complianceService;
 
    /**
     * Generates and displays a compliance report for a specific transaction.
     * @param referenceNumber The common reference number for the transaction.
     * @param model Model to pass data to the view.
     * @return The view name for the compliance report.
     */
    @GetMapping("/generate")
    public String showGenerateForm() {
        return "compliance/generate";
    }
 
    @PostMapping("/generate")
    public String generateComplianceReport(@RequestParam("referenceNumber") String referenceNumber, Model model) {
        try {
            Compliance report = complianceService.generateComplianceReport(referenceNumber);
            model.addAttribute("successMessage", "Compliance report generated for " + referenceNumber);
            return "redirect:/compliance/report/" + referenceNumber; // Redirect to view the report
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error generating compliance report: " + e.getMessage());
            return "compliance/generate";
        }
    }
 
    /**
     * Displays a specific compliance report.
     * @param referenceNumber The common reference number for the transaction.
     * @param model Model to pass data to the view.
     * @return The view name for the compliance report details.
     */
    @GetMapping("/report/{referenceNumber}")
    public String getComplianceReport(@PathVariable String referenceNumber, Model model) {
        try {
            Compliance report = complianceService.getComplianceReport(referenceNumber);
            model.addAttribute("complianceReport", report);
            return "compliance/report"; // Assuming src/main/resources/templates/compliance/report.html
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error"; // A generic error page
        }
    }
 
    /**
     * Submits a regulatory report for a given compliance ID.
     * This would typically be an admin action.
     * @param id The ID of the compliance record to submit.
     * @param model Model to pass data to the view.
     * @return Redirects to the list of compliance reports or a success page.
     */
    @PostMapping("/submit/{id}")
    public ResponseEntity<byte[]> submitRegulatoryReport(@PathVariable Long id) {
        // Generate the PDF report bytes
        byte[] pdfBytes = complianceService.generateComplianceReportPdf(id);

        // Set headers for PDF download
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "compliance_report_" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);

        // Update the compliance record as submitted
        complianceService.submitRegulatoryReport(id);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
 
    /**
     * Displays a list of all compliance reports.
     * @param model Model to pass data to the view.
     * @return The view name for the compliance reports list.
     */
    @GetMapping("/all")
    public String getAllComplianceReports(Model model) {
        List<Compliance> reports = complianceService.getAllComplianceReports();
        model.addAttribute("complianceReports", reports);
        return "compliance/list"; // Assuming src/main/resources/templates/compliance/list.html
    }
}