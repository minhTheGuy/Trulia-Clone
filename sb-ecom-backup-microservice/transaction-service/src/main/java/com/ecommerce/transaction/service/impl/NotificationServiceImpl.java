package com.ecommerce.transaction.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ecommerce.transaction.model.Transaction;
import com.ecommerce.transaction.service.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Override
    public void sendTransactionCreatedNotification(Transaction transaction) {
        log.info("Sending transaction created notification for transaction ID: {}", transaction.getId());
        
        // In a real implementation, this would send an email or push notification
        // to both the buyer and seller
        
        log.info("Transaction created notification sent to buyer ID: {} and seller ID: {}", 
                transaction.getBuyerId(), transaction.getSellerId());
    }

    @Override
    public void sendTransactionStatusChangedNotification(Transaction transaction, String previousStatus) {
        log.info("Sending transaction status changed notification for transaction ID: {}", transaction.getId());
        log.info("Status changed from {} to {}", previousStatus, transaction.getStatus());
        
        // In a real implementation, this would send notifications to relevant parties
        
        log.info("Transaction status change notification sent to buyer ID: {} and seller ID: {}", 
                transaction.getBuyerId(), transaction.getSellerId());
    }

    @Override
    public void sendPaymentReceivedNotification(Long transactionId, String amount) {
        log.info("Sending payment received notification for transaction ID: {}", transactionId);
        log.info("Payment amount: {}", amount);
        
        // In a real implementation, this would send a payment confirmation
    }

    @Override
    public void sendDepositPaidNotification(Transaction transaction) {
        log.info("Sending deposit paid notification for transaction ID: {}", transaction.getId());
        
        // Notify both parties that deposit has been paid
        
        log.info("Deposit paid notification sent to buyer ID: {} and seller ID: {}", 
                transaction.getBuyerId(), transaction.getSellerId());
    }

    @Override
    public void sendContractSignedNotification(Transaction transaction) {
        log.info("Sending contract signed notification for transaction ID: {}", transaction.getId());
        
        // Notify relevant parties that the contract has been signed
        
        log.info("Contract signed notification sent to buyer ID: {} and seller ID: {}", 
                transaction.getBuyerId(), transaction.getSellerId());
    }

    @Override
    public void sendTransactionCompletedNotification(Transaction transaction) {
        log.info("Sending transaction completed notification for transaction ID: {}", transaction.getId());
        
        // Notify both parties that the transaction has been completed
        
        log.info("Transaction completed notification sent to buyer ID: {} and seller ID: {}", 
                transaction.getBuyerId(), transaction.getSellerId());
    }

    @Override
    public void sendTransactionCancelledNotification(Transaction transaction) {
        log.info("Sending transaction cancelled notification for transaction ID: {}", transaction.getId());
        
        // Notify both parties that the transaction has been cancelled
        
        log.info("Transaction cancelled notification sent to buyer ID: {} and seller ID: {}", 
                transaction.getBuyerId(), transaction.getSellerId());
    }

    @Override
    public void sendRentalPaymentReminderNotification(Transaction transaction) {
        log.info("Sending rental payment reminder for transaction ID: {}", transaction.getId());
        
        // Notify tenant about upcoming payment
        
        log.info("Rental payment reminder sent to tenant ID: {}", transaction.getBuyerId());
    }
} 