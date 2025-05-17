package com.ecommerce.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.ecommerce.transaction.exception.ResourceNotFoundException;
import com.ecommerce.transaction.mapper.TransactionMapper;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.model.TransactionStatus;
import com.ecommerce.transaction.model.TransactionType;
import com.ecommerce.transaction.payload.dto.TransactionDTO;
import com.ecommerce.transaction.payload.request.TransactionRequest;
import com.ecommerce.transaction.payload.response.PagedResponse;
import com.ecommerce.transaction.repository.TransactionRepository;
import com.ecommerce.transaction.service.impl.TransactionServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private TransactionRequest transactionRequest;
    private Transaction transaction;
    private TransactionDTO transactionDTO;

    @BeforeEach
    void setUp() {
        transactionRequest = TransactionRequest.builder()
                .propertyId(1L)
                .buyerId(2L)
                .sellerId(3L)
                .amount(new BigDecimal("1000000000"))
                .transactionType(TransactionType.SALE)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .propertyId(1L)
                .buyerId(2L)
                .sellerId(3L)
                .amount(new BigDecimal("1000000000"))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        transactionDTO = TransactionDTO.builder()
                .id(1L)
                .propertyId(1L)
                .buyerId(2L)
                .sellerId(3L)
                .amount(new BigDecimal("1000000000"))
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.PENDING)
                .build();
    }

    @Test
    void createTransaction_Success() {
        // Arrange
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);

        // Act
        TransactionDTO result = transactionService.createTransaction(transactionRequest);

        // Assert
        assertNotNull(result);
        assertEquals(transactionRequest.getPropertyId(), result.getPropertyId());
        assertEquals(transactionRequest.getAmount(), result.getAmount());
        verify(notificationService).sendTransactionCreatedNotification(any(Transaction.class));
    }

    @Test
    void getTransactionById_Success() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));
        when(transactionMapper.mapToDTO(transaction)).thenReturn(transactionDTO);

        // Act
        TransactionDTO result = transactionService.getTransactionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(transaction.getId(), result.getId());
        assertEquals(transaction.getAmount(), result.getAmount());
    }

    @Test
    void getAllTransactions_Success() {
        // Arrange
        Page<Transaction> page = new PageImpl<>(Arrays.asList(transaction));
        when(transactionRepository.searchTransactions(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);

        // Act
        PagedResponse<TransactionDTO> result = transactionService.getAllTransactions(
                0, 10, "createdAt", "desc", null, null, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        assertEquals(transaction.getId(), result.getContent().get(0).getId());
    }
} 