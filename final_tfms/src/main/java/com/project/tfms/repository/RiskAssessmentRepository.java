package com.project.tfms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.tfms.model.RiskAssessment;

import java.util.Optional;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {
    Optional<RiskAssessment> findByTransactionReference(String transactionReference);
}
