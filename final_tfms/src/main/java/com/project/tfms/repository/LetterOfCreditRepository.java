package com.project.tfms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.tfms.model.LetterOfCredit;
import com.project.tfms.model.LetterOfCredit.LCStatus;

import java.util.List;

public interface LetterOfCreditRepository extends JpaRepository<LetterOfCredit, Long> {
    LetterOfCredit findByReferenceNumber(String referenceNumber);
    // Corrected method name: findByCustomer_CustomerId
    List<LetterOfCredit> findByCustomer_CustomerId(Long long1);
    List<LetterOfCredit> findByStatus(LCStatus status);
	List<LetterOfCredit> findByStatusIn(List<LCStatus> asList);
}