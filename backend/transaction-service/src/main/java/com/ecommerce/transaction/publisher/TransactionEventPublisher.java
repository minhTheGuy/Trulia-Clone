package com.ecommerce.transaction.publisher;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import com.ecommerce.transaction.event.TransactionEvent;
import com.ecommerce.transaction.model.Payment;
import com.ecommerce.transaction.model.Transaction;

@Component
public class TransactionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TransactionEventPublisher.class);

    private static final String BINDING = "transaction-events-out-0";

    @Autowired
    private StreamBridge streamBridge;

    public void publishPaymentCompleted(Transaction transaction, Payment payment) {
        TransactionEvent event = buildEvent("PAYMENT_COMPLETED", transaction, payment);
        send(event);
    }

    public void publishPaymentFailed(Transaction transaction, Payment payment) {
        TransactionEvent event = buildEvent("PAYMENT_FAILED", transaction, payment);
        send(event);
    }

    public void publishTransactionCreated(Transaction transaction) {
        TransactionEvent event = TransactionEvent.builder()
                .eventType("TRANSACTION_CREATED")
                .transactionId(transaction.getId())
                .propertyId(transaction.getPropertyId())
                .buyerId(transaction.getBuyerId())
                .sellerId(transaction.getSellerId())
                .amount(transaction.getAmount())
                .currency("VND")
                .transactionType(transaction.getTransactionType() != null ? transaction.getTransactionType().name() : null)
                .transactionStatus(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .timestamp(LocalDateTime.now())
                .build();
        send(event);
    }

    public void publishTransactionCancelled(Transaction transaction) {
        TransactionEvent event = TransactionEvent.builder()
                .eventType("TRANSACTION_CANCELLED")
                .transactionId(transaction.getId())
                .propertyId(transaction.getPropertyId())
                .buyerId(transaction.getBuyerId())
                .sellerId(transaction.getSellerId())
                .amount(transaction.getAmount())
                .currency("VND")
                .transactionType(transaction.getTransactionType() != null ? transaction.getTransactionType().name() : null)
                .transactionStatus(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .timestamp(LocalDateTime.now())
                .build();
        send(event);
    }

    private TransactionEvent buildEvent(String eventType, Transaction transaction, Payment payment) {
        return TransactionEvent.builder()
                .eventType(eventType)
                .transactionId(transaction.getId())
                .paymentId(payment.getId())
                .propertyId(transaction.getPropertyId())
                .buyerId(transaction.getBuyerId())
                .sellerId(transaction.getSellerId())
                .amount(payment.getAmount())
                .currency("VND")
                .transactionType(transaction.getTransactionType() != null ? transaction.getTransactionType().name() : null)
                .transactionStatus(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .paymentStatus(payment.getStatus() != null ? payment.getStatus().name() : null)
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private void send(TransactionEvent event) {
        try {
            boolean sent = streamBridge.send(BINDING, event);
            if (sent) {
                log.info("Published Kafka event: type={}, transactionId={}", event.getEventType(), event.getTransactionId());
            } else {
                log.warn("Failed to publish Kafka event: type={}, transactionId={}", event.getEventType(), event.getTransactionId());
            }
        } catch (Exception e) {
            log.error("Error publishing Kafka event: type={}", event.getEventType(), e);
        }
    }
}
