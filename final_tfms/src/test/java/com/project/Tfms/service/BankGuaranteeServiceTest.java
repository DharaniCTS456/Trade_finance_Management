package com.project.Tfms.service;

import com.project.tfms.model.BankGuarantee;
import com.project.tfms.model.Customer;
import com.project.tfms.repository.BankGuaranteeRepository;
import com.project.tfms.service.BankGuaranteeService;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
 
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
/**
* JUnit test class for BankGuaranteeService.
* Uses Mockito to mock the BankGuaranteeRepository and test the service logic in isolation.
*/
@ExtendWith(MockitoExtension.class)
public class BankGuaranteeServiceTest {
 
    // Mock the repository interface
    @Mock
    private BankGuaranteeRepository repository;
 
    // Inject the mocked repository into the service
    @InjectMocks
    private BankGuaranteeService bankGuaranteeService;
 
    // Sample data for testing
    private BankGuarantee pendingGuarantee;
    private BankGuarantee approvedGuarantee;
    private Customer customer1;
    private Customer customer2;
 
    /**
     * Set up common test data before each test method runs.
     */
    @BeforeEach
    void setUp() {
        customer1 = new Customer();
        // Assuming Customer model has a setCustomerId method for its ID field
        customer1.setCustomerId(1L); // Updated from setId to setCustomerId
        customer1.setName("Customer A");
 
        customer2 = new Customer();
        // Assuming Customer model has a setCustomerId method for its ID field
        customer2.setCustomerId(2L); // Updated from setId to setCustomerId
        customer2.setName("Customer B");
 
        pendingGuarantee = new BankGuarantee();
        pendingGuarantee.setGuaranteeId(1L);
        pendingGuarantee.setApplicantName("Applicant One");
        pendingGuarantee.setBeneficiaryName("Beneficiary Alpha");
        pendingGuarantee.setGuaranteeAmount(1000.0);
        pendingGuarantee.setCurrency("USD");
        pendingGuarantee.setValidityPeriod("12 months");
        pendingGuarantee.setStatus("Pending");
        pendingGuarantee.setWealthDocument("wealth_doc_1.pdf");
        pendingGuarantee.setDate(new Date());
        pendingGuarantee.setCustomer(customer1);
 
        approvedGuarantee = new BankGuarantee();
        approvedGuarantee.setGuaranteeId(2L);
        approvedGuarantee.setApplicantName("Applicant Two");
        approvedGuarantee.setBeneficiaryName("Beneficiary Beta");
        approvedGuarantee.setGuaranteeAmount(2000.0);
        approvedGuarantee.setCurrency("EUR");
        approvedGuarantee.setValidityPeriod("24 months");
        approvedGuarantee.setStatus("Approved");
        approvedGuarantee.setWealthDocument("wealth_doc_2.pdf");
        approvedGuarantee.setDate(new Date());
        approvedGuarantee.setCustomer(customer1);
    }
 
    /**
     * Test case for the requestGuarantee method.
     * Verifies that the guarantee status is set to "Pending" and saved.
     */
    @Test
    void testRequestGuarantee() {
        // Given: a new guarantee object (status will be set by the service)
        BankGuarantee newGuarantee = new BankGuarantee();
        newGuarantee.setApplicantName("New Applicant");
        newGuarantee.setBeneficiaryName("New Beneficiary");
        newGuarantee.setGuaranteeAmount(5000.0);
        newGuarantee.setCurrency("GBP");
        newGuarantee.setValidityPeriod("6 months");
        newGuarantee.setWealthDocument("new_wealth_doc.pdf");
        newGuarantee.setCustomer(customer2);
// When: repository.save is called, return the pendingGuarantee
        when(repository.save(any(BankGuarantee.class))).thenReturn(pendingGuarantee);
 
        // Perform the service call
        BankGuarantee result = bankGuaranteeService.requestGuarantee(newGuarantee);
 
        // Then: verify the status is "Pending" and the repository's save method was called once
        assertNotNull(result);
        assertEquals("Pending", result.getStatus());
        verify(repository, times(1)).save(newGuarantee);
    }
 
    /**
     * Test case for the approveGuarantee method.
     * Verifies that the guarantee status is updated to "Approved" and saved.
     */
    @Test
    void testApproveGuarantee() {
        // Given: a pending guarantee exists in the repository
        when(repository.findById(1L)).thenReturn(Optional.of(pendingGuarantee));
        // When: repository.save is called, return the updated guarantee
        when(repository.save(any(BankGuarantee.class))).thenReturn(approvedGuarantee);
 
        // Perform the service call
        BankGuarantee result = bankGuaranteeService.approveGuarantee(1L);
 
        // Then: verify the status is "Approved" and the repository's save method was called
        assertNotNull(result);
        assertEquals("Approved", result.getStatus());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(pendingGuarantee);
    }
 
    /**
     * Test case for the approveGuarantee method when the guarantee is not found.
     * Verifies that a RuntimeException is thrown.
     */
    @Test
    void testApproveGuarantee_NotFound() {
        // Given: no guarantee found for the given ID
        when(repository.findById(99L)).thenReturn(Optional.empty());
 
        // Then: assert that a RuntimeException is thrown
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            bankGuaranteeService.approveGuarantee(99L);
        });
        assertEquals("Guarantee not found with ID: 99", thrown.getMessage());
        verify(repository, times(1)).findById(99L);
        verify(repository, never()).save(any(BankGuarantee.class));
    }
 
    /**
     * Test case for the rejectGuarantee method.
     * Verifies that the guarantee status is updated to "Rejected" and saved.
     */
    @Test
    void testRejectGuarantee() {
        // Given: a pending guarantee exists in the repository
        when(repository.findById(1L)).thenReturn(Optional.of(pendingGuarantee));
        // When: repository.save is called, return the updated guarantee
        BankGuarantee rejectedGuarantee = new BankGuarantee();
        rejectedGuarantee.setGuaranteeId(1L);
        rejectedGuarantee.setApplicantName("Applicant One");
        rejectedGuarantee.setBeneficiaryName("Beneficiary Alpha");
        rejectedGuarantee.setGuaranteeAmount(1000.0);
        rejectedGuarantee.setCurrency("USD");
        rejectedGuarantee.setValidityPeriod("12 months");
        rejectedGuarantee.setStatus("Rejected");
        rejectedGuarantee.setWealthDocument("wealth_doc_1.pdf");
        rejectedGuarantee.setDate(new Date());
        rejectedGuarantee.setCustomer(customer1);
        when(repository.save(any(BankGuarantee.class))).thenReturn(rejectedGuarantee);
 
        // Perform the service call
        BankGuarantee result = bankGuaranteeService.rejectGuarantee(1L);
 
        // Then: verify the status is "Rejected" and the repository's save method was called
        assertNotNull(result);
        assertEquals("Rejected", result.getStatus());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(pendingGuarantee);
    }
 
    /**
     * Test case for the rejectGuarantee method when the guarantee is not found.
     * Verifies that a RuntimeException is thrown.
     */
    @Test
    void testRejectGuarantee_NotFound() {
        // Given: no guarantee found for the given ID
        when(repository.findById(99L)).thenReturn(Optional.empty());
 
        // Then: assert that a RuntimeException is thrown
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            bankGuaranteeService.rejectGuarantee(99L);
        });
        assertEquals("Guarantee not found with ID: 99", thrown.getMessage());
        verify(repository, times(1)).findById(99L);
        verify(repository, never()).save(any(BankGuarantee.class));
    }
 
    /**
     * Test case for the getAllGuarantees method.
     * Verifies that all guarantees are returned.
     */
    @Test
    void testGetAllGuarantees() {
        // Given: a list of guarantees
        List<BankGuarantee> allGuarantees = Arrays.asList(pendingGuarantee, approvedGuarantee);
        when(repository.findAll()).thenReturn(allGuarantees);
 
        // Perform the service call
        List<BankGuarantee> result = bankGuaranteeService.getAllGuarantees();
 
        // Then: verify the returned list matches the expected list
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(pendingGuarantee));
        assertTrue(result.contains(approvedGuarantee));
        verify(repository, times(1)).findAll();
    }
 
    /**
     * Test case for the getPendingGuarantees method.
     * Verifies that only guarantees with "Pending" status are returned.
     */
    @Test
    void testGetPendingGuarantees() {
        // Given: a list containing only pending guarantees
        List<BankGuarantee> pendingList = Arrays.asList(pendingGuarantee);
        when(repository.findByStatus("Pending")).thenReturn(pendingList);
 
        // Perform the service call
        List<BankGuarantee> result = bankGuaranteeService.getPendingGuarantees();
 
        // Then: verify the returned list contains only pending guarantees
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Pending", result.get(0).getStatus());
        verify(repository, times(1)).findByStatus("Pending");
    }
 
    /**
     * Test case for the getGuaranteesByCustomer method.
     * Verifies that guarantees associated with a specific customer are returned.
     */
    @Test
    void testGetGuaranteesByCustomer() {
        // Given: a list of guarantees for customer1
        List<BankGuarantee> customer1Guarantees = Arrays.asList(pendingGuarantee, approvedGuarantee);
        when(repository.findByCustomer(customer1)).thenReturn(customer1Guarantees);
 
        // Perform the service call
        List<BankGuarantee> result = bankGuaranteeService.getGuaranteesByCustomer(customer1);
 
        // Then: verify the returned list contains guarantees for customer1
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(bg -> bg.getCustomer().equals(customer1)));
        verify(repository, times(1)).findByCustomer(customer1);
    }
}