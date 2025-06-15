package com.project.Tfms.Controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.project.tfms.controller.TradeDocumentController;
import com.project.tfms.model.Admin;
import com.project.tfms.model.Customer;
import com.project.tfms.model.TradeDocument;
import com.project.tfms.service.TradeDocumentService;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TradeDocumentControllerTest {

    @Mock
    private TradeDocumentService tradeDocumentService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private TradeDocumentController tradeDocumentController;

    private Customer mockCustomer;
    private Admin mockAdmin;
    private TradeDocument mockDocument;

    @BeforeEach
    void setUp() {
        // Mock Customer
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);
        mockCustomer.setEmail("customer@example.com");

        // Mock Admin
        mockAdmin = new Admin();
        mockAdmin.setAdminId(1L);
        mockAdmin.setEmail("admin@example.com");

        // Mock TradeDocument
        mockDocument = new TradeDocument();
        mockDocument.setDocumentId(1L);
        mockDocument.setDocumentType("Invoice");
        mockDocument.setDocumentLink("http://example.com/doc.pdf");
        mockDocument.setUploadedBy(mockCustomer);
    }

    @Test
    void testShowUploadForm_NoCustomerLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(null);

        String result = tradeDocumentController.showUploadForm(model, session);
        assertEquals("redirect:/login", result);
    }

    @Test
    void testShowUploadForm_CustomerLoggedIn_ShouldReturnUploadPage() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(mockCustomer);

        String result = tradeDocumentController.showUploadForm(model, session);
        assertEquals("documents/upload-document", result);
    }

    @Test
    void testUploadDocument_NoCustomerLoggedIn_ShouldRedirectToLoginPage() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(null);

        String result = tradeDocumentController.uploadDocument(mockDocument, model, session);
        assertEquals("customer/login", result);
    }

    @Test
    void testUploadDocument_CustomerLoggedIn_SuccessfulUpload() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(mockCustomer);

        String result = tradeDocumentController.uploadDocument(mockDocument, model, session);
        assertEquals("documents/upload-document", result);
        verify(tradeDocumentService, times(1)).saveDocument(mockDocument);
    }

    @Test
    void testViewMyDocuments_NoCustomerLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(null);

        String result = tradeDocumentController.viewMyDocuments(model, session);
        assertEquals("redirect:/login", result);
    }

    @Test
    void testViewMyDocuments_CustomerLoggedIn_ShouldReturnDocumentList() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(mockCustomer);
        when(tradeDocumentService.getDocumentsByCustomer(mockCustomer)).thenReturn(List.of(mockDocument));

        String result = tradeDocumentController.viewMyDocuments(model, session);
        assertEquals("documents/my-documents", result);
    }

    @Test
    void testViewAllDocumentsForAdmin_NoAdminLoggedIn_ShouldRedirectToAdminLogin() {
        when(session.getAttribute("loggedInAdmin")).thenReturn(null);

        String result = tradeDocumentController.viewAllDocumentsForAdmin(model, session);
        assertEquals("redirect:/alogin", result);
    }

    @Test
    void testViewAllDocumentsForAdmin_AdminLoggedIn_ShouldReturnAllDocumentsView() {
        when(session.getAttribute("loggedInAdmin")).thenReturn(mockAdmin);
        when(tradeDocumentService.getAllDocuments()).thenReturn(List.of(mockDocument));

        String result = tradeDocumentController.viewAllDocumentsForAdmin(model, session);
        assertEquals("documents/all-documents", result);
    }

    @Test
    void testDeleteDocument_NoCustomerLoggedIn_ShouldRedirectToLogin() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(null);

        String result = tradeDocumentController.deleteDocument(1L, session, model);
        assertEquals("redirect:/login", result);
    }

    @Test
    void testDeleteDocument_CustomerDeletesOwnDocument_SuccessfulDeletion() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(mockCustomer);
        when(tradeDocumentService.getDocumentById(1L)).thenReturn(Optional.of(mockDocument));

        String result = tradeDocumentController.deleteDocument(1L, session, model);
        assertEquals("redirect:/documents/my-documents", result);
        verify(tradeDocumentService, times(1)).deleteDocument(1L);
    }

    @Test
    void testDeleteDocument_CustomerTriesToDeleteOthersDocument_ShouldFail() {
        Customer anotherCustomer = new Customer();
        anotherCustomer.setCustomerId(2L);

        mockDocument.setUploadedBy(anotherCustomer); // Document belongs to another user

        when(session.getAttribute("loggedInCustomer")).thenReturn(mockCustomer);
        when(tradeDocumentService.getDocumentById(1L)).thenReturn(Optional.of(mockDocument));

        String result = tradeDocumentController.deleteDocument(1L, session, model);
        assertEquals("redirect:/documents/my-documents", result);
        verify(tradeDocumentService, never()).deleteDocument(1L);
    }

    @Test
    void testDeleteDocument_DocumentNotFound_ShouldFailGracefully() {
        when(session.getAttribute("loggedInCustomer")).thenReturn(mockCustomer);
        when(tradeDocumentService.getDocumentById(1L)).thenReturn(Optional.empty());

        String result = tradeDocumentController.deleteDocument(1L, session, model);
        assertEquals("redirect:/documents/my-documents", result);
        verify(tradeDocumentService, never()).deleteDocument(1L);
    }
}
