package com.project.tfms.service;
 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.project.tfms.model.BankGuarantee;
import com.project.tfms.model.Customer;
import com.project.tfms.repository.BankGuaranteeRepository;
 
import java.util.List;
 
@Service
public class BankGuaranteeService {
 
    @Autowired
    private BankGuaranteeRepository repository;
 
    public BankGuarantee requestGuarantee(BankGuarantee guarantee) {
        guarantee.setStatus("Pending");
        return repository.save(guarantee);
    }
 
    // This method can be repurposed or removed if 'approve' and 'reject' are sufficient
    public BankGuarantee issueGuarantee(Long guaranteeId) {
        BankGuarantee guarantee = repository.findById(guaranteeId).orElseThrow(() -> new RuntimeException("Guarantee not found"));
        guarantee.setStatus("Issued"); // Changed from "Issued" to a more generic "Approved" or "Rejected" flow
        return repository.save(guarantee);
    }
 
    // New method to approve a bank guarantee
    public BankGuarantee approveGuarantee(Long guaranteeId) {
        BankGuarantee guarantee = repository.findById(guaranteeId)
                .orElseThrow(() -> new RuntimeException("Guarantee not found with ID: " + guaranteeId));
        guarantee.setStatus("Approved");
        return repository.save(guarantee);
    }
 
    // New method to reject a bank guarantee
    public BankGuarantee rejectGuarantee(Long guaranteeId) {
        BankGuarantee guarantee = repository.findById(guaranteeId)
                .orElseThrow(() -> new RuntimeException("Guarantee not found with ID: " + guaranteeId));
        guarantee.setStatus("Rejected");
        return repository.save(guarantee);
    }
 
    public BankGuarantee trackGuaranteeStatus(Long guaranteeId) {
        return repository.findById(guaranteeId).orElseThrow(() -> new RuntimeException("Guarantee not found"));
    }
 
    public List<BankGuarantee> getAllGuarantees() {
        return repository.findAll();
    }
 
    // New method to get all pending guarantees for admin review
    public List<BankGuarantee> getPendingGuarantees() {
        return repository.findByStatus("Pending");
    }
 
    public List<BankGuarantee> getGuaranteesByCustomer(Customer customer) {
        return repository.findByCustomer(customer);
    }
}