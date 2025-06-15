package com.project.tfms.controller;

import com.project.tfms.model.Admin; // Import Admin model for session access
import com.project.tfms.model.Customer;
import com.project.tfms.model.TradeDocument;
import com.project.tfms.service.TradeDocumentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/documents") // Base path for document related operations
public class TradeDocumentController {

    @Autowired
    private TradeDocumentService tradeDocumentService;

    // Show the document upload form for customers
    @GetMapping("/upload")
    public String showUploadForm(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login"; // Redirect to login if no customer is logged in
        }
        model.addAttribute("tradeDocument", new TradeDocument()); // Object for form binding
        model.addAttribute("customer", customer); // For sidebar display
        // List of mandatory document types
        model.addAttribute("documentTypes", List.of("Shipping Bill", "Invoice", "Bill of Lading", "Warehouse Receipt"));
        return "documents/upload-document"; // points to src/main/resources/templates/documents/upload-document.html
    }

    // Handle the document upload (saving the link) for customers
    @PostMapping("/upload")
    public String uploadDocument(@ModelAttribute("tradeDocument") TradeDocument tradeDocument, Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            model.addAttribute("errorMessage", "You must be logged in to upload documents.");
            return "customer/login"; // Redirect to login
        }

        tradeDocument.setUploadedBy(customer); // Associate the document with the logged-in customer
        tradeDocumentService.saveDocument(tradeDocument); // Save the document (and its link) to the database

        model.addAttribute("successMessage", "Document uploaded successfully!");
        model.addAttribute("customer", customer); // Keep customer in model for consistent UI
        // Re-initialize a new TradeDocument for the form to clear it after submission
        model.addAttribute("tradeDocument", new TradeDocument());
        model.addAttribute("documentTypes", List.of("Shipping Bill", "Invoice", "Bill of Lading", "Warehouse Receipt"));

        System.out.println("Document uploaded by customer: " + customer.getEmail() + " | Type: " + tradeDocument.getDocumentType() + " | Link: " + tradeDocument.getDocumentLink());
        return "documents/upload-document"; // Stay on the upload page with success message
    }

    // View all documents uploaded by the current customer
    @GetMapping("/my-documents")
    public String viewMyDocuments(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login"; // Redirect to login if no customer is logged in
        }

        List<TradeDocument> myDocuments = tradeDocumentService.getDocumentsByCustomer(customer);
        model.addAttribute("documents", myDocuments);
        model.addAttribute("customer", customer); // For sidebar display
        return "documents/my-documents"; // points to src/main/resources/templates/documents/my-documents.html
    }

    /**
     * New endpoint for admin to view all trade documents uploaded by all customers.
     * Ensures only logged-in admins can access this page.
     *
     * @param model The Spring Model to pass data to the view.
     * @param session The HttpSession to check for logged-in admin.
     * @return The Thymeleaf template name for displaying all documents.
     */
    @GetMapping("/all")
    public String viewAllDocumentsForAdmin(Model model, HttpSession session) {
        // Retrieve the logged-in admin from the session
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");

        // Basic authorization check: ensure an admin is logged in
        if (admin == null) {
            System.out.println("Unauthorized access attempt to /documents/all. Redirecting to admin login.");
            return "redirect:/alogin"; // Redirect to admin login if not authenticated
        }

        // Fetch all trade documents
        List<TradeDocument> allDocuments = tradeDocumentService.getAllDocuments();
        model.addAttribute("documents", allDocuments);
        model.addAttribute("admin", admin); // Pass the admin object to the model for sidebar display

        System.out.println("Admin " + admin.getEmail() + " viewing all documents. Total documents: " + allDocuments.size());
        return "documents/all-documents"; // points to src/main/resources/templates/documents/all-documents.html
    }


    // Handle document deletion for customers
    @PostMapping("/delete/{id}")
    public String deleteDocument(@PathVariable Long id, HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("loggedInCustomer");
        if (customer == null) {
            return "redirect:/login";
        }

        Optional<TradeDocument> documentOptional = tradeDocumentService.getDocumentById(id);
        if (documentOptional.isPresent()) {
            TradeDocument document = documentOptional.get();
            // Ensure only the owner can delete their document
            if (document.getUploadedBy().getCustomerId().equals(customer.getCustomerId())) {
                tradeDocumentService.deleteDocument(id);
                model.addAttribute("message", "Document deleted successfully!");
                System.out.println("Document ID " + id + " deleted by customer " + customer.getEmail());
            } else {
                model.addAttribute("error", "Unauthorized to delete this document.");
                System.out.println("Customer " + customer.getEmail() + " attempted to delete unauthorized document ID " + id);
            }
        } else {
            model.addAttribute("error", "Document not found.");
            System.out.println("Attempted to delete non-existent document ID " + id);
        }
        return "redirect:/documents/my-documents"; // Redirect to refresh the list
    }
}
