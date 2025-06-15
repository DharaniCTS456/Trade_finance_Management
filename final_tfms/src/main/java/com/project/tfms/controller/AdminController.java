package com.project.tfms.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.tfms.model.Admin;
import com.project.tfms.model.BankGuarantee; // Assuming BankGuarantee is used here
import com.project.tfms.model.Customer; // Assuming Customer is still used if needed for other admin functions
import com.project.tfms.service.AdminService;
import com.project.tfms.service.BankGuaranteeService;
import com.project.tfms.service.CustomerService; // Assuming CustomerService is still used

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private AdminService admservice;

    @Autowired
    private BankGuaranteeService service;

    @GetMapping("/alogin")
    public String loginForm() {
        return "customer/alogin";
    }

    @PostMapping("/alogin")
    public String processLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
    	Optional<Admin> details=admservice.getDetails(email);
        boolean admin = admservice.authenticate(email, password);
        if (admin) {
            Admin loggedInAdmin =details.get(); // Renamed 'crust' to 'loggedInAdmin' for clarity
            session.setAttribute("loggedInAdmin", loggedInAdmin);
            return "redirect:/adashboard";
        } else {
            model.addAttribute("error", "Invalid Email or Password");
            return "customer/alogin";
        }
    }

    @GetMapping("/alogout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/alogin?alogout=true";
    }

    @GetMapping("/adashboard")
    public String dashboard(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) return "redirect:/alogin";

        model.addAttribute("admin", admin);
        return "customer/adashboard"; 
    }

    // Updated to fetch only pending guarantees for review
    @GetMapping("/issueguarentee")
    public String issueguarentee(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) return "redirect:/alogin";

        List<BankGuarantee> pendingGuarantees = service.getPendingGuarantees();
        model.addAttribute("guarantees", pendingGuarantees);
        model.addAttribute("admin", admin);
        return "bankguarantee/issueguarentee";
    }

    // New POST mapping to approve a guarantee
    @PostMapping("/approveGuarantee/{id}")
    public String approveGuarantee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.approveGuarantee(id);
            redirectAttributes.addFlashAttribute("successMessage", "Guarantee ID " + id + " approved successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving guarantee: " + e.getMessage());
        }
        return "redirect:/issueguarentee"; 
    }

    // New POST mapping to reject a guarantee
    @PostMapping("/rejectGuarantee/{id}")
    public String rejectGuarantee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            service.rejectGuarantee(id);
            redirectAttributes.addFlashAttribute("successMessage", "Guarantee ID " + id + " rejected successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error rejecting guarantee: " + e.getMessage());
        }
        return "redirect:/issueguarentee"; // Redirect back to the pending list
    }

    @GetMapping("/guarentees")
    public String guarentees(HttpSession session, Model model) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) return "redirect:/alogin";
        List<BankGuarantee> guarantees = service.getAllGuarantees();
        model.addAttribute("guarantees", guarantees);
        model.addAttribute("admin", admin);
        return "bankguarantee/guarentees";
    }

   
}