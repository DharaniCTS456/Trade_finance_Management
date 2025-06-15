package com.project.tfms.controller;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
 
import com.project.tfms.model.Admin;
import com.project.tfms.model.BankGuarantee;
import com.project.tfms.model.Customer;
import com.project.tfms.service.BankGuaranteeService;
import jakarta.servlet.http.HttpSession;
 
import java.util.List;
 
@Controller
public class BankGuaranteeController {
 
    @Autowired
    private BankGuaranteeService service;
 
    @GetMapping("/requestguarantee")
    public String showRequestForm(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        BankGuarantee newGuarantee = new BankGuarantee();
        newGuarantee.setApplicantName(customer.getName()); // Set applicant name from logged-in customer
        model.addAttribute("bankGuarantee", newGuarantee);
        return "bankguarantee/requestguarentee";
    }
 
    @PostMapping("/requestguarantee")
    public String requestGuarantee(@ModelAttribute("bankGuarantee") BankGuarantee guarantee, Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            model.addAttribute("errorMessage", "You must be logged in to request a guarantee.");
            return "customer/login";
        }
        guarantee.setCustomer(customer);
        service.requestGuarantee(guarantee);
        model.addAttribute("successMessage", "Guarantee request submitted successfully!");
        // After successful submission, re-prepare the form with the customer's name
        BankGuarantee newGuarantee = new BankGuarantee();
        newGuarantee.setApplicantName(customer.getName());
        model.addAttribute("bankGuarantee", newGuarantee);
        System.out.println("bank guarantee request method hit by customer: " + customer.getEmail());
        return "bankguarantee/requestguarentee";
    }
 
    @PostMapping("/issue/{id}")
    public String issueGuarantee(@PathVariable Long id, Model model) {
        BankGuarantee guarantee = service.issueGuarantee(id);
        model.addAttribute("successMessage", "Guarantee issued successfully!");
        model.addAttribute("bankGuarantee", guarantee);
        return "bankguarantee/issue";
    }
 
    @GetMapping("/track/{id}")
    public String trackGuaranteeStatus(@PathVariable Long id, Model model) {
        BankGuarantee guarantee = service.trackGuaranteeStatus(id);
        model.addAttribute("bankGuarantee", guarantee);
        return "bankguarantee/track";
    }
 
    @GetMapping("/myguarantees")
    public String getMyGuarantees(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }
        List<BankGuarantee> myGuarantees = service.getGuaranteesByCustomer(customer);
        model.addAttribute("guarantees", myGuarantees);
        return "bankguarantee/myguarantees";
    }
}