package com.project.Tfms.service;
 
import com.project.tfms.model.Customer;
import com.project.tfms.model.TradeDocument;
import com.project.tfms.repository.TradeDocumentRepository;
import com.project.tfms.service.TradeDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
public class TradeDocumentServiceTest {
 
    @Mock
    private TradeDocumentRepository tradeDocumentRepository;
 
    @InjectMocks
    private TradeDocumentService tradeDocumentService;
 
    private Customer customer;
    private TradeDocument tradeDocument;
 
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        customer = new Customer();
        customer.setCustomerId(1L);
        customer.setName("Test Customer");
 
        tradeDocument = new TradeDocument();
        tradeDocument.setDocumentId(1L);
        tradeDocument.setDocumentType("Invoice");
        tradeDocument.setDocumentLink("http://example.com/doc1");
        tradeDocument.setUploadedBy(customer);
    }
 
    @Test
    public void testSaveDocument() {
        when(tradeDocumentRepository.save(any(TradeDocument.class))).thenReturn(tradeDocument);
        TradeDocument saved = tradeDocumentService.saveDocument(tradeDocument);
        assertNotNull(saved);
        assertEquals("Invoice", saved.getDocumentType());
        verify(tradeDocumentRepository, times(1)).save(tradeDocument);
    }
 
    @Test
    public void testGetDocumentsByCustomer() {
        List<TradeDocument> docs = Arrays.asList(tradeDocument);
        when(tradeDocumentRepository.findByUploadedBy(customer)).thenReturn(docs);
        List<TradeDocument> result = tradeDocumentService.getDocumentsByCustomer(customer);
        assertEquals(1, result.size());
        assertEquals("Invoice", result.get(0).getDocumentType());
        verify(tradeDocumentRepository, times(1)).findByUploadedBy(customer);
    }
 
    @Test
    public void testGetDocumentById() {
        when(tradeDocumentRepository.findById(1L)).thenReturn(Optional.of(tradeDocument));
        Optional<TradeDocument> result = tradeDocumentService.getDocumentById(1L);
        assertTrue(result.isPresent());
        assertEquals("Invoice", result.get().getDocumentType());
        verify(tradeDocumentRepository, times(1)).findById(1L);
    }
 
    @Test
    public void testDeleteDocument() {
        doNothing().when(tradeDocumentRepository).deleteById(1L);
        tradeDocumentService.deleteDocument(1L);
        verify(tradeDocumentRepository, times(1)).deleteById(1L);
    }
 
    @Test
    public void testGetAllDocuments() {
        List<TradeDocument> docs = Arrays.asList(tradeDocument);
        when(tradeDocumentRepository.findAll()).thenReturn(docs);
        List<TradeDocument> result = tradeDocumentService.getAllDocuments();
        assertEquals(1, result.size());
        verify(tradeDocumentRepository, times(1)).findAll();
    }
}