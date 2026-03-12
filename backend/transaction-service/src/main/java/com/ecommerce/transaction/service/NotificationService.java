package com.ecommerce.transaction.service;

import com.ecommerce.transaction.model.Transaction;

public interface NotificationService {
    // Send notification when transaction is created
    void sendTransactionCreatedNotification(Transaction transaction);
    
    // Send notification when transaction status changes
    void sendTransactionStatusChangedNotification(Transaction transaction, String previousStatus);
    
    // Send notification when payment is received
    void sendPaymentReceivedNotification(Long transactionId, String amount);
    
    // Send notification when deposit is paid
    void sendDepositPaidNotification(Transaction transaction);
    
    // Send notification when contract is signed
    void sendContractSignedNotification(Transaction transaction);
    
    // Send notification when transaction is completed
    void sendTransactionCompletedNotification(Transaction transaction);
    
    // Send notification when transaction is cancelled
    void sendTransactionCancelledNotification(Transaction transaction);
    
    // Send payment reminder for rental transactions
    void sendRentalPaymentReminderNotification(Transaction transaction);
} 