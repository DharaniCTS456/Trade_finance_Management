package com.project.tfms.service;

import com.project.tfms.model.Customer;
import com.project.tfms.model.TradeDocument;
import com.project.tfms.repository.TradeDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TradeDocumentService {

    @Autowired
    private TradeDocumentRepository tradeDocumentRepository;

    public TradeDocument saveDocument(TradeDocument tradeDocument) {
        return tradeDocumentRepository.save(tradeDocument);
    }

    public List<TradeDocument> getDocumentsByCustomer(Customer customer) {
        return tradeDocumentRepository.findByUploadedBy(customer);
    }

    public Optional<TradeDocument> getDocumentById(Long id) {
        return tradeDocumentRepository.findById(id);
    }

    public void deleteDocument(Long id) {
        tradeDocumentRepository.deleteById(id);
    }

    /**
     * Retrieves all trade documents from the database.
     * This method is intended for administrative views.
     * @return A list of all TradeDocument entities.
     */
    public List<TradeDocument> getAllDocuments() {
        return tradeDocumentRepository.findAll();
    }
}
