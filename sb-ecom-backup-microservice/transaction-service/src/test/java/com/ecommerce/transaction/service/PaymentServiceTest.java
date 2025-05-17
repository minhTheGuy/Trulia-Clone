package com.ecommerce.transaction.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.transaction.exception.ResourceNotFoundException;
import com.ecommerce.transaction.mapper.PaymentMapper;
import com.ecommerce.transaction.model.Payment;
import com.ecommerce.transaction.model.PaymentMethod;
import com.ecommerce.transaction.model.PaymentStatus;
import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.payload.dto.PaymentDTO;
import com.ecommerce.transaction.payload.request.PaymentRequest;
import com.ecommerce.transaction.repository.PaymentRepository;
import com.ecommerce.transaction.repository.TransactionRepository;
import com.ecommerce.transaction.service.impl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StripeService stripeService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequest paymentRequest;
    private Payment payment;
    private PaymentDTO paymentDTO;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        paymentRequest = PaymentRequest.builder()
                .transactionId(1L)
                .amount(new BigDecimal("1000000"))
                .paymentMethod(PaymentMethod.STRIPE)
                .description("Test payment")
                .build();

        payment = Payment.builder()
                .id(1L)
                .transactionId(1L)
                .amount(new BigDecimal("1000000"))
                .paymentMethod(PaymentMethod.STRIPE)
                .status(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .description("Test payment")
                .build();

        paymentDTO = PaymentDTO.builder()
                .id(1L)
                .transactionId(1L)
                .amount(new BigDecimal("1000000"))
                .paymentMethod(PaymentMethod.STRIPE)
                .status(PaymentStatus.PENDING)
                .description("Test payment")
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("1000000"))
                .build();
    }

    @Test
    void createPayment_Success() {
        // Arrange
        when(transactionRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentMapper.mapToDTO(any(Payment.class))).thenReturn(paymentDTO);

        // Act
        PaymentDTO result = paymentService.createPayment(paymentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(paymentRequest.getAmount(), result.getAmount());
        assertEquals(paymentRequest.getPaymentMethod(), result.getPaymentMethod());
        verify(notificationService).sendPaymentReceivedNotification(anyLong(), anyString());
    }

    @Test
    void getPaymentById_Success() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(java.util.Optional.of(payment));
        when(paymentMapper.mapToDTO(payment)).thenReturn(paymentDTO);

        // Act
        PaymentDTO result = paymentService.getPaymentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(payment.getId(), result.getId());
        assertEquals(payment.getAmount(), result.getAmount());
    }

    @Test
    void getPaymentById_NotFound() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPaymentById(999L);
        });
    }
} 