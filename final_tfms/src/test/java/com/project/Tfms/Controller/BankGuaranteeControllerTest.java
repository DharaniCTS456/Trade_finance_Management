package com.project.Tfms.Controller;

import com.project.tfms.model.BankGuarantee;
import com.project.tfms.model.Customer;
import com.project.tfms.controller.*;
import com.project.tfms.service.BankGuaranteeService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the BankGuaranteeController.
 * This class uses Mockito to mock dependencies like BankGuaranteeService and HttpSession,
 * allowing for isolated testing of the controller's logic.
 */
@ExtendWith(MockitoExtension.class)
public class BankGuaranteeControllerTest {

    // InjectMocks automatically injects the mocked dependencies into the controller
    @InjectMocks
    private BankGuaranteeController bankGuaranteeController;

    // Mock the BankGuaranteeService dependency
    @Mock
    private BankGuaranteeService bankGuaranteeService;

    // Mock the HttpSession dependency
    @Mock
    private HttpSession session;

    // Common objects used across tests
    private Customer testCustomer;
    private BankGuarantee testBankGuarantee;

    /**
     * Sets up common test data before each test method runs.
     */
    @BeforeEach
    void setUp() {
        // Initialize a dummy customer for testing
        testCustomer = new Customer();
        testCustomer.setCustomerId(1L);
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john.doe@example.com");

        // Initialize a dummy bank guarantee for testing
        testBankGuarantee = new BankGuarantee();
        testBankGuarantee.setGuaranteeId(101L);
        testBankGuarantee.setApplicantName("John Doe");
        testBankGuarantee.setStatus("PENDING");
        testBankGuarantee.setCustomer(testCustomer);
    }

    /**
     * Test case for showRequestForm when a customer is logged in.
     * Verifies that the correct view is returned and the model contains a new BankGuarantee
     * with the applicant name pre-filled.
     */
    @Test
    void testShowRequestForm_loggedInCustomer() {
        // Arrange
        Model model = new ConcurrentModel();
        when(session.getAttribute("loggedInCustomer")).thenReturn(testCustomer);

        // Act
        String viewName = bankGuaranteeController.showRequestForm(model, session);

        // Assert
        assertEquals("bankguarantee/requestguarentee", viewName);
        BankGuarantee bgInModel = (BankGuarantee) model.getAttribute("bankGuarantee");
        assertNotNull(bgInModel);
        assertEquals(testCustomer.getName(), bgInModel.getApplicantName());
    }

    /**
     * Test case for showRequestForm when no customer is logged in.
     * Verifies that the user is redirected to the login page.
     */
    @Test
    void testShowRequestForm_noLoggedInCustomer() {
        // Arrange
        Model model = new ConcurrentModel();
        when(session.getAttribute("loggedInCustomer")).thenReturn(null);

        // Act
        String viewName = bankGuaranteeController.showRequestForm(model, session);

        // Assert
        assertEquals("redirect:/login", viewName);
        verify(session, times(1)).getAttribute("loggedInCustomer"); // Ensure session was checked
    }
  

    /**
     * Test case for requestGuarantee when no customer is logged in.
     * Verifies that an error message is added and the login view is returned.
     */
    @Test
    void testRequestGuarantee_noLoggedInCustomer() {
        // Arrange
        Model model = new ConcurrentModel();
        when(session.getAttribute("loggedInCustomer")).thenReturn(null);

        // Act
        String viewName = bankGuaranteeController.requestGuarantee(testBankGuarantee, model, session);

        // Assert
        assertEquals("customer/login", viewName);
        assertTrue(model.containsAttribute("errorMessage"));
        assertEquals("You must be logged in to request a guarantee.", model.getAttribute("errorMessage"));
        verify(bankGuaranteeService, never()).requestGuarantee(any(BankGuarantee.class)); // Service should not be called
    }

    /**
     * Test case for issueGuarantee.
     * Verifies that the service method is called, success message is added, and the correct view is returned.
     */
    @Test
    void testIssueGuarantee() {
        // Arrange
        Model model = new ConcurrentModel();
        Long guaranteeId = 1L;
        when(bankGuaranteeService.issueGuarantee(guaranteeId)).thenReturn(testBankGuarantee);

        // Act
        String viewName = bankGuaranteeController.issueGuarantee(guaranteeId, model);

        // Assert
        assertEquals("bankguarantee/issue", viewName);
        assertTrue(model.containsAttribute("successMessage"));
        assertEquals("Guarantee issued successfully!", model.getAttribute("successMessage"));
        assertEquals(testBankGuarantee, model.getAttribute("bankGuarantee"));
        verify(bankGuaranteeService, times(1)).issueGuarantee(guaranteeId);
    }

    /**
     * Test case for trackGuaranteeStatus.
     * Verifies that the service method is called and the correct view with the guarantee is returned.
     */
    @Test
    void testTrackGuaranteeStatus() {
        // Arrange
        Model model = new ConcurrentModel();
        Long guaranteeId = 1L;
        when(bankGuaranteeService.trackGuaranteeStatus(guaranteeId)).thenReturn(testBankGuarantee);

        // Act
        String viewName = bankGuaranteeController.trackGuaranteeStatus(guaranteeId, model);

        // Assert
        assertEquals("bankguarantee/track", viewName);
        assertEquals(testBankGuarantee, model.getAttribute("bankGuarantee"));
        verify(bankGuaranteeService, times(1)).trackGuaranteeStatus(guaranteeId);
    }

    /**
     * Test case for getMyGuarantees when a customer is logged in.
     * Verifies that the service method is called to retrieve guarantees and they are added to the model.
     */
    @Test
    void testGetMyGuarantees_loggedInCustomer() {
        // Arrange
        Model model = new ConcurrentModel();
        List<BankGuarantee> customerGuarantees = Arrays.asList(testBankGuarantee, new BankGuarantee());
        when(session.getAttribute("loggedInCustomer")).thenReturn(testCustomer);
        when(bankGuaranteeService.getGuaranteesByCustomer(testCustomer)).thenReturn(customerGuarantees);

        // Act
        String viewName = bankGuaranteeController.getMyGuarantees(model, session);

        // Assert
        assertEquals("bankguarantee/myguarantees", viewName);
        assertTrue(model.containsAttribute("guarantees"));
        assertEquals(customerGuarantees, model.getAttribute("guarantees"));
        verify(bankGuaranteeService, times(1)).getGuaranteesByCustomer(testCustomer);
    }

    /**
     * Test case for getMyGuarantees when no customer is logged in.
     * Verifies that the user is redirected to the login page.
     */
    @Test
    void testGetMyGuarantees_noLoggedInCustomer() {
        // Arrange
        Model model = new ConcurrentModel();
        when(session.getAttribute("loggedInCustomer")).thenReturn(null);

        // Act
        String viewName = bankGuaranteeController.getMyGuarantees(model, session);

        // Assert
        assertEquals("redirect:/login", viewName);
        verify(bankGuaranteeService, never()).getGuaranteesByCustomer(any(Customer.class)); // Service should not be called
    }
}