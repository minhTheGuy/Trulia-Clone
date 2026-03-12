package com.ecommerce.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import com.ecommerce.transaction.model.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.ecommerce.transaction.exception.ResourceNotFoundException;
import com.ecommerce.transaction.exception.TransactionApiException;
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

    private Transaction transaction;
    private TransactionDTO transactionDTO;
    private TransactionRequest transactionRequest;

    @BeforeEach
    public void setup() {
        // Setup Transaction
        transaction = Transaction.builder()
                .id(1L)
                .propertyId(100L)
                .buyerId(200L)
                .sellerId(300L)
                .amount(new BigDecimal("250000.00"))
                .commission(new BigDecimal("7500.00"))
                .transactionType(TransactionType.RENT)
                .status(TransactionStatus.PENDING)
                .paymentMethod(PaymentMethod.STRIPE)
                .contractNumber("CTR123456")
                .propertyTitle("Beautiful House")
                .propertyAddress("123 Main St, New York, NY 10001")
                .buyerName("John Doe")
                .sellerName("Jane Smith")
                .notes("First-time homebuyer")
                .depositPaid(false)
                .contractSigned(false)
                .fullPaymentCompleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Setup TransactionDTO
        transactionDTO = new TransactionDTO();
        transactionDTO.setId(1L);
        transactionDTO.setPropertyId(100L);
        transactionDTO.setBuyerId(200L);
        transactionDTO.setSellerId(300L);
        transactionDTO.setAmount(new BigDecimal("250000.00"));
        transactionDTO.setCommission(new BigDecimal("7500.00"));
        transactionDTO.setTransactionType(TransactionType.RENT);
        transactionDTO.setStatus(TransactionStatus.PENDING);
        transactionDTO.setPaymentMethod(PaymentMethod.STRIPE);
        transactionDTO.setContractNumber("CTR123456");
        transactionDTO.setPropertyTitle("Beautiful House");
        transactionDTO.setPropertyAddress("123 Main St, New York, NY 10001");
        transactionDTO.setBuyerName("John Doe");
        transactionDTO.setSellerName("Jane Smith");
        transactionDTO.setNotes("First-time homebuyer");
        transactionDTO.setDepositPaid(false);
        transactionDTO.setContractSigned(false);
        transactionDTO.setFullPaymentCompleted(false);

        // Setup TransactionRequest
        transactionRequest = new TransactionRequest();
        transactionRequest.setPropertyId(100L);
        transactionRequest.setBuyerId(200L);
        transactionRequest.setSellerId(300L);
        transactionRequest.setAmount(new BigDecimal("250000.00"));
        transactionRequest.setCommission(new BigDecimal("7500.00"));
        transactionRequest.setTransactionType(TransactionType.RENT);
        transactionRequest.setPaymentMethod(PaymentMethod.STRIPE);
        transactionRequest.setContractNumber("CTR123456");
        transactionRequest.setPropertyTitle("Beautiful House");
        transactionRequest.setPropertyAddress("123 Main St, New York, NY 10001");
        transactionRequest.setBuyerName("John Doe");
        transactionRequest.setSellerName("Jane Smith");
        transactionRequest.setNotes("First-time homebuyer");
    }

    @Test
    public void testCreateTransaction_Success() {
        // Arrange
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);
        doNothing().when(notificationService).sendTransactionCreatedNotification(any(Transaction.class));

        // Act
        TransactionDTO result = transactionService.createTransaction(transactionRequest);

        // Assert
        assertNotNull(result);
        assertEquals(transactionDTO.getId(), result.getId());
        assertEquals(transactionDTO.getPropertyId(), result.getPropertyId());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(notificationService, times(1)).sendTransactionCreatedNotification(any(Transaction.class));
    }

    @Test
    public void testGetTransactionById_Success() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);

        // Act
        TransactionDTO result = transactionService.getTransactionById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(transactionDTO.getId(), result.getId());
        assertEquals(transactionDTO.getPropertyId(), result.getPropertyId());
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetTransactionById_NotFound() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getTransactionById(1L));
        verify(transactionRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAllTransactions_Success() {
        // Arrange
        Page<Transaction> transactionPage = new PageImpl<>(Arrays.asList(transaction));
        when(transactionRepository.searchTransactions(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(transactionPage);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);

        // Act
        PagedResponse<TransactionDTO> result = transactionService.getAllTransactions(
                0, 10, "id", "asc", null, null, null, null, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(transactionDTO.getId(), result.getContent().get(0).getId());
        verify(transactionRepository, times(1)).searchTransactions(
                any(), any(), any(), any(), any(), any(), any(), any(Pageable.class));
    }

    @Test
    public void testUpdateTransaction_Success() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);

        // Act
        TransactionDTO result = transactionService.updateTransaction(1L, transactionRequest);

        // Assert
        assertNotNull(result);
        assertEquals(transactionDTO.getId(), result.getId());
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testUpdateTransaction_NotFound() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> transactionService.updateTransaction(1L, transactionRequest));
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testUpdateTransaction_CompletedStatus() {
        // Arrange
        transaction.setStatus(TransactionStatus.COMPLETED);
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));

        // Act & Assert
        assertThrows(TransactionApiException.class, () -> transactionService.updateTransaction(1L, transactionRequest));
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testCancelTransaction_Success() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);
        doNothing().when(notificationService).sendTransactionStatusChangedNotification(any(Transaction.class), anyString());
        doNothing().when(notificationService).sendTransactionCancelledNotification(any(Transaction.class));

        // Act
        TransactionDTO result = transactionService.cancelTransaction(1L);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(notificationService, times(1)).sendTransactionStatusChangedNotification(any(Transaction.class), anyString());
        verify(notificationService, times(1)).sendTransactionCancelledNotification(any(Transaction.class));
    }

    @Test
    public void testCancelTransaction_NotFound() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> transactionService.cancelTransaction(1L));
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    public void testUpdateTransactionStatus_Success() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);
        doNothing().when(notificationService).sendTransactionStatusChangedNotification(any(Transaction.class), anyString());

        // Act
        TransactionDTO result = transactionService.updateTransactionStatus(1L, TransactionStatus.IN_PROGRESS);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(notificationService, times(1)).sendTransactionStatusChangedNotification(any(Transaction.class), anyString());
    }

    @Test
    public void testUpdateTransactionStatus_Completed() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);
        doNothing().when(notificationService).sendTransactionStatusChangedNotification(any(Transaction.class), anyString());

        // Act
        TransactionDTO result = transactionService.updateTransactionStatus(1L, TransactionStatus.COMPLETED);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(notificationService, times(1)).sendTransactionStatusChangedNotification(any(Transaction.class), anyString());
    }

    @Test
    public void testMarkDepositPaid_Success() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);

        // Act
        TransactionDTO result = transactionService.markDepositPaid(1L);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testMarkContractSigned_Success() {
        // Arrange
        when(transactionRepository.findById(anyLong())).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(transactionMapper.mapToDTO(any(Transaction.class))).thenReturn(transactionDTO);

        // Act
        TransactionDTO result = transactionService.markContractSigned(1L);

        // Assert
        assertNotNull(result);
        verify(transactionRepository, times(1)).findById(1L);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
} 