package com.project.tfms.repository;
 
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.tfms.model.BankGuarantee;
import com.project.tfms.model.Customer;
import java.util.List;
 
public interface BankGuaranteeRepository extends JpaRepository<BankGuarantee, Long> {
    List<BankGuarantee> findByCustomer(Customer customer);
 
    // New method to find all BankGuarantees by their status
    List<BankGuarantee> findByStatus(String status);
}