package com.project.tfms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.tfms.model.LetterOfCredit;
import com.project.tfms.model.LetterOfCredit.LCStatus; // Import the nested LCStatus enum
import com.project.tfms.model.Customer;
import com.project.tfms.service.LetterOfCreditService;

import jakarta.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;

@Controller
@RequestMapping("/lc")
public class LetterOfCreditController {

    @Autowired
    private LetterOfCreditService lcService;

    // --- CUSTOMER-FACING ENDPOINTS ---

    @GetMapping("/create")
    public String showCreateLcForm(Model model, HttpSession session) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }
        LetterOfCredit newLc = new LetterOfCredit();
        // Automatically set applicant name from the logged-in customer for the form
        newLc.setApplicantName(loggedInCustomer.getName());
        model.addAttribute("letterOfCredit", newLc);
        return "lc/create";
    }

    @PostMapping("/create")
    public String createLetterOfCredit(@ModelAttribute("letterOfCredit") LetterOfCredit letterOfCredit, HttpSession session, Model model) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            model.addAttribute("errorMessage", "You must be logged in to create a Letter of Credit.");
            return "customer/login"; // Or wherever your login page is
        }

        // Set customer association
        letterOfCredit.setCustomer(loggedInCustomer);
        // Automatically set applicant name from the logged-in customer
        letterOfCredit.setApplicantName(loggedInCustomer.getName());

        try {
            lcService.createLetterOfCredit(letterOfCredit);
            model.addAttribute("successMessage", "Letter of Credit request submitted successfully!");
            // After successful submission, clear the form for a new entry, retaining applicant name
            LetterOfCredit newLc = new LetterOfCredit();
            newLc.setApplicantName(loggedInCustomer.getName());
            model.addAttribute("letterOfCredit", newLc);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error submitting LC request: " + e.getMessage());
            // Keep existing data to allow user to correct
            model.addAttribute("letterOfCredit", letterOfCredit);
        }
        return "lc/create";
    }

    @GetMapping("/details/{id}")
    public String getLcDetails(@PathVariable Long id, Model model, HttpSession session) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");

        if (loggedInCustomer == null) {
            return "redirect:/login"; // Redirect to login if not authenticated
        }

        LetterOfCredit lc = null;
        try {
            lc = lcService.getLetterOfCreditById(id);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Letter of Credit not found.");
            return "redirect:/lc/my-lcs";
        }

        // Authorization check: Ensure the LC belongs to the logged-in customer
        if (lc.getCustomer() == null || !(lc.getCustomer().getCustomerId().equals(loggedInCustomer.getCustomerId()))) {
            model.addAttribute("errorMessage", "You are not authorized to view this Letter of Credit.");
            return "redirect:/lc/my-lcs";
        }

        model.addAttribute("letterOfCredit", lc);
        return "lc/details";
    }

    @GetMapping("/amend/{id}")
    public String showAmendLcForm(@PathVariable Long id, Model model, HttpSession session) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            return "redirect:/login";
        }

        LetterOfCredit lc = null;
        try {
            lc = lcService.getLetterOfCreditById(id);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Letter of Credit not found for amendment.");
            return "redirect:/lc/my-lcs";
        }

        // Authorization check: Ensure the LC belongs to the logged-in customer
        if (lc.getCustomer() == null || !(lc.getCustomer().getCustomerId().equals(loggedInCustomer.getCustomerId()))) {
            model.addAttribute("errorMessage", "You are not authorized to amend this Letter of Credit.");
            return "redirect:/lc/my-lcs";
        }

        // Only allow amendment if the LC is in a specific status (e.g., PENDING or PENDING_AMENDMENT)
        if (lc.getStatus() != LCStatus.PENDING &&
            lc.getStatus() != LCStatus.PENDING_AMENDMENT) {
            model.addAttribute("errorMessage", "This Letter of Credit cannot be amended in its current status (" + lc.getStatus().getDisplayName() + ").");
            return "redirect:/lc/details/" + id; // Redirect to details page
        }

        model.addAttribute("letterOfCredit", lc);
        return "lc/amend";
    }

    @PostMapping("/amend/{id}")
    public String amendLetterOfCredit(@PathVariable Long id, @ModelAttribute("letterOfCredit") LetterOfCredit letterOfCredit, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            return "redirect:/login";
        }

        try {
            LetterOfCredit existingLc = lcService.getLetterOfCreditById(id);
            // Authorization check: Ensure the LC belongs to the logged-in customer
            if (existingLc.getCustomer() == null || !(existingLc.getCustomer().getCustomerId().equals(loggedInCustomer.getCustomerId()))) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to amend this Letter of Credit.");
                return "redirect:/lc/my-lcs"; // Redirect instead of staying on form for auth issues
            }

            // Also check status for customer amendment POST
            if (existingLc.getStatus() != LCStatus.PENDING &&
                existingLc.getStatus() != LCStatus.PENDING_AMENDMENT) {
                redirectAttributes.addFlashAttribute("errorMessage", "This Letter of Credit cannot be amended in its current status (" + existingLc.getStatus().getDisplayName() + ").");
                return "redirect:/lc/details/" + id;
            }

            // Update only fields that the customer is allowed to amend
            existingLc.setAmount(letterOfCredit.getAmount());
            existingLc.setApplicantBank(letterOfCredit.getApplicantBank());
            existingLc.setBeneficiaryBank(letterOfCredit.getBeneficiaryBank());
            existingLc.setDescription(letterOfCredit.getDescription());
            existingLc.setDesiredValidityPeriod(letterOfCredit.getDesiredValidityPeriod()); // New field

            // If a customer amends an already ISSUED or AMENDED LC, it should go back to PENDING_AMENDMENT
            if (existingLc.getStatus() == LCStatus.ISSUED || existingLc.getStatus() == LCStatus.AMENDED) {
                existingLc.setStatus(LCStatus.PENDING_AMENDMENT);
            }
            // If it's already PENDING, it remains PENDING (no status change)

            lcService.amendLetterOfCredit(id, existingLc); // Pass the updated existing LC
            redirectAttributes.addFlashAttribute("successMessage", "Letter of Credit amended successfully! Awaiting bank review.");
            return "redirect:/lc/details/" + id;
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Error amending Letter of Credit: " + e.getMessage());
            model.addAttribute("letterOfCredit", letterOfCredit); // Re-add to model to persist form data on error
            return "lc/amend";
        }
    }

    @PostMapping("/close/{id}")
    public String closeLetterOfCredit(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            return "redirect:/login";
        }

        try {
            LetterOfCredit lcToClose = lcService.getLetterOfCreditById(id);
            if (lcToClose.getCustomer() == null || !(lcToClose.getCustomer().getCustomerId().equals(loggedInCustomer.getCustomerId()))) {
                redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to close this Letter of Credit.");
                return "redirect:/lc/my-lcs";
            }

            lcService.closeLetterOfCredit(id);
            redirectAttributes.addFlashAttribute("successMessage", "Letter of Credit closed successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error closing Letter of Credit: " + e.getMessage());
        }
        return "redirect:/lc/my-lcs";
    }

    @GetMapping("/my-lcs")
    public String getMyLettersOfCredit(Model model, HttpSession session) {
        Customer loggedInCustomer = (Customer) session.getAttribute("loggedInCustomer");
        if (loggedInCustomer == null) {
            return "redirect:/login";
        }
        List<LetterOfCredit> lcs = lcService.getLettersOfCreditByCustomerId(loggedInCustomer.getCustomerId());
        model.addAttribute("lcs", lcs);
        return "lc/my-lcs";
    }

    // --- ADMIN-FACING ENDPOINTS ---

    @GetMapping("/admin/all")
    public String getAllLettersOfCreditForAdmin(Model model, HttpSession session) {
        // Add admin role check here.
        if (session.getAttribute("loggedInAdmin") == null) { // Basic admin check
            return "redirect:/alogin";
        }
        List<LetterOfCredit> lcs = lcService.getAllLettersOfCredit();
        model.addAttribute("lcs", lcs);
        return "lc/alist";
    }

    @GetMapping("/admin/details/{id}")
    public String getLcDetailsForAdmin(@PathVariable Long id, Model model, HttpSession session) {
        // Add admin role check here.
        if (session.getAttribute("loggedInAdmin") == null) { // Basic admin check
            return "redirect:/alogin";
        }
        LetterOfCredit lc = null;
        try {
            lc = lcService.getLetterOfCreditById(id);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Letter of Credit not found.");
            return "redirect:/lc/admin/all";
        }
        model.addAttribute("letterOfCredit", lc);
        model.addAttribute("dateTimeFormatter", DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
        return "lc/admin_details";
    }

    @GetMapping("/admin/amend/{id}")
    public String showAmendLcFormForAdmin(@PathVariable Long id, Model model, HttpSession session) {
        // Add admin role check here.
        if (session.getAttribute("loggedInAdmin") == null) { // Basic admin check
            return "redirect:/alogin";
        }
        LetterOfCredit lc = null;
        try {
            lc = lcService.getLetterOfCreditById(id);
            model.addAttribute("letterOfCredit", lc);
            return "lc/admin_amend";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Letter of Credit not found for amendment.");
            return "redirect:/lc/admin/all";
        }
    }

    @PostMapping("/admin/amend/{id}")
    public String amendLetterOfCreditByAdmin(@PathVariable Long id, @ModelAttribute("letterOfCredit") LetterOfCredit letterOfCredit, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        // Add admin role check here.
        if (session.getAttribute("loggedInAdmin") == null) { // Basic admin check
            return "redirect:/alogin";
        }
        try {
            LetterOfCredit existingLc = lcService.getLetterOfCreditById(id);

            // Admin can set all fields, including issueDate, expiryDate, and status
            existingLc.setAmount(letterOfCredit.getAmount());
            existingLc.setIssueDate(letterOfCredit.getIssueDate());
            existingLc.setExpiryDate(letterOfCredit.getExpiryDate());
            existingLc.setApplicantBank(letterOfCredit.getApplicantBank());
            existingLc.setBeneficiaryBank(letterOfCredit.getBeneficiaryBank());
            existingLc.setStatus(letterOfCredit.getStatus()); // This now receives LCStatus enum directly
            existingLc.setDescription(letterOfCredit.getDescription());
            existingLc.setDesiredValidityPeriod(letterOfCredit.getDesiredValidityPeriod());

            lcService.amendLetterOfCredit(id, existingLc);
            redirectAttributes.addFlashAttribute("successMessage", "Letter of Credit amended successfully by Admin!");
            return "redirect:/lc/admin/details/" + id;
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", "Error amending Letter of Credit: " + e.getMessage());
            model.addAttribute("letterOfCredit", letterOfCredit); // Re-add to model to persist form data on error
            return "lc/admin_amend";
        }
    }

    @PostMapping("/admin/close/{id}")
    public String closeLetterOfCreditByAdmin(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpSession session) {
        // Add admin role check here.
        if (session.getAttribute("loggedInAdmin") == null) { // Basic admin check
            return "redirect:/alogin";
        }
        try {
            lcService.closeLetterOfCredit(id);
            redirectAttributes.addFlashAttribute("successMessage", "Letter of Credit closed successfully by Admin!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error closing Letter of Credit: " + e.getMessage());
        }
        return "redirect:/lc/admin/all";
    }

    // New endpoints for Admin to review and approve/reject LCs
    @GetMapping("/admin/pending-review")
    public String showPendingLcsForAdmin(HttpSession session, Model model) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/alogin";
        }
        // Assuming lcService has a method to get LCs with PENDING or PENDING_AMENDMENT status
        List<LetterOfCredit> pendingLcs = lcService.getLettersOfCreditByStatus(LCStatus.PENDING, LCStatus.PENDING_AMENDMENT);
        model.addAttribute("lcs", pendingLcs);
        return "lc/pending_review"; // Create this new Thymeleaf template
    }

    @PostMapping("/admin/approve/{id}")
    public String approveLcByAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/alogin";
        }
        try {
            lcService.updateLcStatus(id, LCStatus.ISSUED); // Update LC status to ISSUED
            redirectAttributes.addFlashAttribute("successMessage", "Letter of Credit ID " + id + " approved successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving Letter of Credit: " + e.getMessage());
        }
        return "redirect:/lc/admin/pending-review";
    }

    @PostMapping("/admin/reject/{id}")
    public String rejectLcByAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/alogin";
        }
        try {
            lcService.updateLcStatus(id, LCStatus.REJECTED); // Update LC status to REJECTED
            redirectAttributes.addFlashAttribute("successMessage", "Letter of Credit ID " + id + " rejected successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting Letter of Credit: " + e.getMessage());
        }
        return "redirect:/lc/admin/pending-review";
    }
}