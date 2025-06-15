package com.project.tfms.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Import RedirectAttributes

import com.project.tfms.model.Customer; // Assuming Admin is not directly used in CustomerController for now
import com.project.tfms.service.CustomerService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customer/register";
    }

    @PostMapping("/register")
    public String registerCustomer(@ModelAttribute("customer") Customer customer, RedirectAttributes redirectAttributes) {
        try {
            customerService.saveCustomer(customer);
            // Use RedirectAttributes to pass a message after redirection
            redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please log in.");
            System.out.println("Customer registered successfully."); // For server-side logging
            return "redirect:/login"; // Redirect to login page after successful registration
        } catch (Exception e) {
            // Handle potential errors (e.g., duplicate email if not caught by unique constraint)
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            System.err.println("Registration failed: " + e.getMessage()); // For server-side logging
            return "redirect:/register"; // Redirect back to registration with an error
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "customer/login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        Optional<Customer> customer = customerService.authenticate(email, password);
        if (customer.isPresent()) {
            Customer cust = customer.get();
            session.setAttribute("loggedInCustomer", cust);
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Invalid Email or Password");
            return "customer/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        model.addAttribute("customer", customer);
        return "customer/dashboard";
    }
}