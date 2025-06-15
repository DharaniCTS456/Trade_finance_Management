package com.project.tfms.repository;

import com.project.tfms.model.Customer;
import com.project.tfms.model.TradeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeDocumentRepository extends JpaRepository<TradeDocument, Long> {
    // This method is used to fetch documents uploaded by a specific customer
    List<TradeDocument> findByUploadedBy(Customer customer);

    // JpaRepository already provides findAll() for fetching all documents
}
