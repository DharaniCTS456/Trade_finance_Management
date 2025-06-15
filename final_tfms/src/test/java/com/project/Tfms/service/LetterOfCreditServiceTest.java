package com.project.Tfms.service;

import com.project.tfms.model.Customer;
import com.project.tfms.model.LetterOfCredit;
import com.project.tfms.service.*;
import com.project.tfms.model.LetterOfCredit.LCStatus;
import com.project.tfms.repository.LetterOfCreditRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LetterOfCreditService Tests")
class LetterOfCreditServiceTest {

    @Mock
    private LetterOfCreditRepository repository;

    @InjectMocks
    private LetterOfCreditService letterOfCreditService;

    private LetterOfCredit sampleLc;
    private Customer sampleCustomer;

    @BeforeEach
    void setUp() {
        sampleCustomer = new Customer();
        sampleCustomer.setCustomerId(1L);
        sampleCustomer.setName("Test Customer");

        sampleLc = new LetterOfCredit();
        sampleLc.setLcId(100L);
        sampleLc.setApplicantName(sampleCustomer.getName());
        sampleLc.setCustomer(sampleCustomer); // Link LC to customer
        sampleLc.setAmount(10000.00);
        sampleLc.setCurrency("USD");
        sampleLc.setDesiredValidityPeriod("12 months");
        sampleLc.setApplicantBank("Bank A");
        sampleLc.setBeneficiaryBank("Bank B");
        sampleLc.setDescription("Test LC Description");
        sampleLc.setStatus(LCStatus.PENDING); // Default status for new LC
        sampleLc.setReferenceNumber(UUID.randomUUID().toString()); // Set a random UUID for consistency
    }

    @Test
    @DisplayName("Should create a new Letter of Credit successfully")
    void createLetterOfCredit_success() {
        // Given
        LetterOfCredit newLc = new LetterOfCredit();
        newLc.setApplicantName("New Applicant");
        newLc.setAmount(50000.00);
        newLc.setCurrency("EUR");
        newLc.setDesiredValidityPeriod("6 months");
        newLc.setApplicantBank("New Bank A");
        newLc.setBeneficiaryBank("New Bank B");
        newLc.setDescription("New LC Description");

        // When
        when(repository.save(any(LetterOfCredit.class))).thenAnswer(invocation -> {
            LetterOfCredit savedLc = invocation.getArgument(0);
            assertNotNull(savedLc.getReferenceNumber()); // Reference number should be set
            assertEquals(LCStatus.PENDING, savedLc.getStatus()); // Status should be PENDING
            assertNull(savedLc.getIssueDate()); // Issue date should be null
            assertNull(savedLc.getExpiryDate()); // Expiry date should be null
            savedLc.setLcId(200L); // Simulate ID generation by repository
            return savedLc;
        });

        LetterOfCredit createdLc = letterOfCreditService.createLetterOfCredit(newLc);

        // Then
        assertNotNull(createdLc);
        assertNotNull(createdLc.getLcId());
        assertEquals(LCStatus.PENDING, createdLc.getStatus());
        assertNotNull(createdLc.getReferenceNumber());
        assertEquals("New Applicant", createdLc.getApplicantName());
        assertNull(createdLc.getIssueDate());
        assertNull(createdLc.getExpiryDate());
        verify(repository, times(1)).save(any(LetterOfCredit.class));
    }

    @Test
    @DisplayName("Should amend an existing Letter of Credit successfully (Customer perspective - limited fields)")
    void amendLetterOfCredit_customer_success() {
        // Given
        // Simulate a customer amending their LC - they typically can't change issueDate, expiryDate, status
        LetterOfCredit customerUpdatedLcDetails = new LetterOfCredit();
        customerUpdatedLcDetails.setAmount(12000.00);
        customerUpdatedLcDetails.setDescription("Customer requested new terms.");
        customerUpdatedLcDetails.setApplicantBank("Customer Bank A");
        customerUpdatedLcDetails.setBeneficiaryBank("Customer Bank B");
        customerUpdatedLcDetails.setDesiredValidityPeriod("18 months");
        // IssueDate, ExpiryDate, Status are null, simulating a customer's request form
        customerUpdatedLcDetails.setIssueDate(null);
        customerUpdatedLcDetails.setExpiryDate(null);
        customerUpdatedLcDetails.setStatus(null); // Status usually changes to PENDING_AMENDMENT in controller

        // Set initial state before customer amendment
        sampleLc.setStatus(LCStatus.PENDING);
        sampleLc.setIssueDate(LocalDate.of(2024, 1, 1));
        sampleLc.setExpiryDate(LocalDate.of(2025, 1, 1));


        when(repository.findById(sampleLc.getLcId())).thenReturn(Optional.of(sampleLc));
        when(repository.save(any(LetterOfCredit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        LetterOfCredit amendedLc = letterOfCreditService.amendLetterOfCredit(sampleLc.getLcId(), customerUpdatedLcDetails);

        // Then
        assertNotNull(amendedLc);
        assertEquals(sampleLc.getLcId(), amendedLc.getLcId());
        assertEquals(12000.00, amendedLc.getAmount());
        assertEquals("Customer requested new terms.", amendedLc.getDescription());
        // These should remain unchanged because customerUpdatedLcDetails passed null for them
        assertEquals(LocalDate.of(2024, 1, 1), amendedLc.getIssueDate());
        assertEquals(LocalDate.of(2025, 1, 1), amendedLc.getExpiryDate());
        assertEquals(LCStatus.PENDING, amendedLc.getStatus()); // Status remains unchanged by service for null input
        assertEquals("Customer Bank A", amendedLc.getApplicantBank());
        assertEquals("Customer Bank B", amendedLc.getBeneficiaryBank());
        assertEquals("18 months", amendedLc.getDesiredValidityPeriod());
        verify(repository, times(1)).findById(sampleLc.getLcId());
        verify(repository, times(1)).save(sampleLc);
    }

    @Test
    @DisplayName("Should close a Letter of Credit successfully")
    void closeLetterOfCredit_success() {
        // Given
        when(repository.findById(sampleLc.getLcId())).thenReturn(Optional.of(sampleLc));
        when(repository.save(any(LetterOfCredit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        LetterOfCredit closedLc = letterOfCreditService.closeLetterOfCredit(sampleLc.getLcId());

        // Then
        assertNotNull(closedLc);
        assertEquals(LCStatus.CLOSED, closedLc.getStatus());
        verify(repository, times(1)).findById(sampleLc.getLcId());
        verify(repository, times(1)).save(sampleLc);
    }

    @Test
    @DisplayName("Should retrieve a Letter of Credit by ID successfully")
    void getLetterOfCreditById_success() {
        // Given
        when(repository.findById(sampleLc.getLcId())).thenReturn(Optional.of(sampleLc));

        // When
        LetterOfCredit foundLc = letterOfCreditService.getLetterOfCreditById(sampleLc.getLcId());

        // Then
        assertNotNull(foundLc);
        assertEquals(sampleLc.getLcId(), foundLc.getLcId());
        assertEquals(sampleLc.getAmount(), foundLc.getAmount());
        verify(repository, times(1)).findById(sampleLc.getLcId());
    }

    @Test
    @DisplayName("Should retrieve all Letters of Credit")
    void getAllLettersOfCredit_success() {
        // Given
        LetterOfCredit lc2 = new LetterOfCredit();
        lc2.setLcId(101L);
        lc2.setStatus(LCStatus.ISSUED);
        List<LetterOfCredit> allLcs = Arrays.asList(sampleLc, lc2);
        when(repository.findAll()).thenReturn(allLcs);

        // When
        List<LetterOfCredit> result = letterOfCreditService.getAllLettersOfCredit();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(sampleLc));
        assertTrue(result.contains(lc2));
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve Letters of Credit by customer ID")
    void getLettersOfCreditByCustomerId_success() {
        // Given
        LetterOfCredit lcForCustomer = new LetterOfCredit();
        lcForCustomer.setLcId(102L);
        lcForCustomer.setCustomer(sampleCustomer);
        lcForCustomer.setStatus(LCStatus.ISSUED);
        List<LetterOfCredit> customerLcs = List.of(lcForCustomer, sampleLc); // sampleLc is also for sampleCustomer

        when(repository.findByCustomer_CustomerId(sampleCustomer.getCustomerId())).thenReturn(customerLcs);

        // When
        List<LetterOfCredit> result = letterOfCreditService.getLettersOfCreditByCustomerId(sampleCustomer.getCustomerId());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(lc -> lc.getCustomer().getCustomerId().equals(sampleCustomer.getCustomerId())));
        verify(repository, times(1)).findByCustomer_CustomerId(sampleCustomer.getCustomerId());
    }

    @Test
    @DisplayName("Should retrieve Letters of Credit by multiple statuses (PENDING, PENDING_AMENDMENT)")
    void getLettersOfCreditByStatus_multipleStatuses() {
        // Given
        LetterOfCredit pendingLc = new LetterOfCredit();
        pendingLc.setLcId(201L);
        pendingLc.setStatus(LCStatus.PENDING);
        LetterOfCredit pendingAmendmentLc = new LetterOfCredit();
        pendingAmendmentLc.setLcId(203L);
        pendingAmendmentLc.setStatus(LCStatus.PENDING_AMENDMENT);

        List<LetterOfCredit> combinedLcs = Arrays.asList(pendingLc, pendingAmendmentLc);
        List<LCStatus> queryStatuses = Arrays.asList(LCStatus.PENDING, LCStatus.PENDING_AMENDMENT);

        when(repository.findByStatusIn(queryStatuses)).thenReturn(combinedLcs);

        // When
        List<LetterOfCredit> result = letterOfCreditService.getLettersOfCreditByStatus(LCStatus.PENDING, LCStatus.PENDING_AMENDMENT);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(pendingLc));
        assertTrue(result.contains(pendingAmendmentLc));
        verify(repository, times(1)).findByStatusIn(queryStatuses);
    }

    @Test
    @DisplayName("Should return empty list when getLettersOfCreditByStatus receives no statuses")
    void getLettersOfCreditByStatus_noStatusesProvided() {
        // When
        List<LetterOfCredit> result = letterOfCreditService.getLettersOfCreditByStatus();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository, never()).findByStatusIn(anyList());
    }

    @Test
    @DisplayName("Should update LC status successfully")
    void updateLcStatus_success() {
        // Given
        sampleLc.setStatus(LCStatus.PENDING);
        when(repository.findById(sampleLc.getLcId())).thenReturn(Optional.of(sampleLc));
        when(repository.save(any(LetterOfCredit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        LetterOfCredit updatedLc = letterOfCreditService.updateLcStatus(sampleLc.getLcId(), LCStatus.ISSUED);

        // Then
        assertNotNull(updatedLc);
        assertEquals(LCStatus.ISSUED, updatedLc.getStatus());
        verify(repository, times(1)).findById(sampleLc.getLcId());
        verify(repository, times(1)).save(sampleLc);
    }

    @Test
    @DisplayName("Should throw RuntimeException when updating status of a non-existent LC")
    void updateLcStatus_notFound() {
        // Given
        Long nonExistentId = 999L;
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When / Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                letterOfCreditService.updateLcStatus(nonExistentId, LCStatus.REJECTED));
        assertEquals("Letter of Credit not found with ID: " + nonExistentId, exception.getMessage());
        verify(repository, times(1)).findById(nonExistentId);
        verify(repository, never()).save(any(LetterOfCredit.class));
    }
}