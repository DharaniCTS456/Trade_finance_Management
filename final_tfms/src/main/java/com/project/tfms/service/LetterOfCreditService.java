package com.project.tfms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.tfms.model.LetterOfCredit;
import com.project.tfms.model.LetterOfCredit.LCStatus; 
import com.project.tfms.repository.LetterOfCreditRepository;
import java.util.Arrays; 
import java.util.List;
import java.util.UUID;


@Service
public class LetterOfCreditService {

    @Autowired
    private LetterOfCreditRepository repository;

    /**
     * Creates a new Letter of Credit.
     * Sets initial status to "Pending", generates a unique reference number,
     * and initializes bank-controlled dates to null.
     * @param lc The LetterOfCredit object to be created.
     * @return The saved LetterOfCredit object.
     */
    public LetterOfCredit createLetterOfCredit(LetterOfCredit lc) {
        // Initial status when customer submits a request
        lc.setStatus(LCStatus.PENDING);
        // Generate unique reference number
        lc.setReferenceNumber(UUID.randomUUID().toString());

        // These dates are determined by the bank upon issuance, not by the customer request.
        // They should be null at this stage.
        lc.setIssueDate(null);
        lc.setExpiryDate(null);

        // ApplicantName is already set in controller from logged-in customer.
        // Other fields like BeneficiaryName, Currency, etc., would be null
        // until a bank officer fills them or if your initial form collects them.

        return repository.save(lc);
    }

    /**
     * Amends an existing Letter of Credit. This method is called with the specific
     * fields that are allowed to be updated based on the calling context (customer or admin).
     *
     * @param lcId The ID of the LC to amend.
     * @param updatedLc A LetterOfCredit object containing the fields to be updated.
     * The controller should ensure only relevant fields are passed here
     * based on the user's role.
     * @return The amended LetterOfCredit object.
     * @throws RuntimeException if the LC is not found.
     */
    public LetterOfCredit amendLetterOfCredit(Long lcId, LetterOfCredit updatedLc) {
        LetterOfCredit existingLc = repository.findById(lcId)
                .orElseThrow(() -> new RuntimeException("Letter of Credit not found with ID: " + lcId));

        // Update fields that are passed from the form.
        // The controller should already ensure that only permissible fields for the
        // current user role (customer/admin) are present in the 'updatedLc' object.

        // Common fields amendable by both customer (request) and admin:
        existingLc.setAmount(updatedLc.getAmount());
        existingLc.setDescription(updatedLc.getDescription());
        existingLc.setApplicantBank(updatedLc.getApplicantBank());
        existingLc.setBeneficiaryBank(updatedLc.getBeneficiaryBank());
        existingLc.setDesiredValidityPeriod(updatedLc.getDesiredValidityPeriod());

        // Bank-controlled fields (issueDate, expiryDate, status) should ONLY be updated
        // if they are explicitly provided (i.e., from an admin form).
        // For customer amendments, these fields from 'updatedLc' would typically be null,
        // and thus existingLc's values for these won't be overwritten.
        if (updatedLc.getIssueDate() != null) {
            existingLc.setIssueDate(updatedLc.getIssueDate());
        }
        if (updatedLc.getExpiryDate() != null) {
            existingLc.setExpiryDate(updatedLc.getExpiryDate());
        }
        // Only update status if it's explicitly provided in updatedLc (e.g., by admin)
        // or if the logic in the controller mandates a specific status change.
        if (updatedLc.getStatus() != null) {
            existingLc.setStatus(updatedLc.getStatus());
        }

        return repository.save(existingLc);
    }


    /**
     * Closes a Letter of Credit.
     * @param lcId The ID of the LC to close.
     * @return The closed LetterOfCredit object.
     * @throws RuntimeException if the LC is not found.
     */
    public LetterOfCredit closeLetterOfCredit(Long lcId) {
        LetterOfCredit lc = repository.findById(lcId)
                .orElseThrow(() -> new RuntimeException("Letter of Credit not found with ID: " + lcId));
        lc.setStatus(LCStatus.CLOSED);
        return repository.save(lc);
    }

    /**
     * Retrieves a Letter of Credit by its ID.
     * @param lcId The ID of the LC.
     * @return The LetterOfCredit object.
     * @throws RuntimeException if the LC is not found.
     */
    public LetterOfCredit getLetterOfCreditById(Long lcId) {
        return repository.findById(lcId)
                .orElseThrow(() -> new RuntimeException("Letter of Credit not found with ID: " + lcId));
    }

    /**
     * Retrieves all Letters of Credit.
     * @return A list of all LetterOfCredit objects.
     */
    public List<LetterOfCredit> getAllLettersOfCredit() {
        return repository.findAll();
    }

    /**
     * Retrieves all Letters of Credit for a specific customer.
     * @param customerId The ID of the customer.
     * @return A list of LetterOfCredit objects belonging to the customer.
     */
    public List<LetterOfCredit> getLettersOfCreditByCustomerId(Long customerId) {
        // This method assumes you have `findByCustomer_CustomerId` in your repository.
        // Make sure your LetterOfCredit entity has a 'customer' field and Customer has 'customerId'.
        return repository.findByCustomer_CustomerId(customerId);
    }

    /**
     * Retrieves Letters of Credit by one or more specified statuses.
     * This is the new method needed by the controller.
     * @param statuses Variable arguments list of LCStatus enums.
     * @return A list of LetterOfCredit objects matching any of the given statuses.
     */
    public List<LetterOfCredit> getLettersOfCreditByStatus(LCStatus... statuses) {
        if (statuses == null || statuses.length == 0) {
            return List.of(); // Return empty list if no statuses are provided
        }
        // This assumes your LetterOfCreditRepository has a method like:
        // List<LetterOfCredit> findByStatusIn(Collection<LCStatus> statuses);
        return repository.findByStatusIn(Arrays.asList(statuses));
    }

    /**
     * Updates the status of a specific Letter of Credit.
     * This replaces the old approve/reject methods for more generic control.
     * @param id The ID of the Letter of Credit.
     * @param newStatus The new status to set.
     * @return The updated LetterOfCredit object.
     * @throws RuntimeException if the LC is not found.
     */
    public LetterOfCredit updateLcStatus(Long id, LCStatus newStatus) {
        LetterOfCredit lc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Letter of Credit not found with ID: " + id));
        lc.setStatus(newStatus);
        return repository.save(lc);
    }

}