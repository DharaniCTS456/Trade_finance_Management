package com.project.tfms.repository;
 
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.tfms.model.Compliance;
 
import java.util.Optional;
 
public interface ComplianceRepository extends JpaRepository<Compliance, Long> {
    Optional<Compliance> findByTransactionReference(String transactionReference);
}  