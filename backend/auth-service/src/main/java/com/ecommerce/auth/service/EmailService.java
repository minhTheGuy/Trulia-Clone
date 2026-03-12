package com.ecommerce.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.verification.url}")
    private String verificationBaseUrl;
    
    @Value("${app.email.reset-password.url}")
    private String passwordResetBaseUrl;

    @Async
    public void sendVerificationEmail(String to, String username, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            String verificationUrl = verificationBaseUrl + token;
            String emailContent = buildVerificationEmail(username, verificationUrl);
            
            helper.setSubject("Xác nhận tài khoản của bạn");
            helper.setText(emailContent, true);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            
            mailSender.send(mimeMessage);
            logger.info("Verification email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send verification email to {}: {}", to, e.getMessage());
        }
    }
    
    @Async
    public void sendPasswordResetEmail(String to, String username, String token) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            String resetUrl = passwordResetBaseUrl + token;
            String emailContent = buildPasswordResetEmail(username, resetUrl);
            
            helper.setSubject("Khôi phục mật khẩu");
            helper.setText(emailContent, true);
            helper.setTo(to);
            helper.setFrom(fromEmail);
            
            mailSender.send(mimeMessage);
            logger.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }
    
    private String buildVerificationEmail(String username, String verificationUrl) {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;\">"
                + "<h2 style=\"color: #333;\">Xác nhận tài khoản</h2>"
                + "<p>Xin chào " + username + ",</p>"
                + "<p>Cảm ơn bạn đã đăng ký tài khoản tại trang web của chúng tôi. Để hoàn tất quá trình đăng ký, vui lòng xác nhận email của bạn bằng cách nhấp vào nút bên dưới:</p>"
                + "<p style=\"text-align: center;\">"
                + "<a href=\"" + verificationUrl + "\" style=\"display: inline-block; padding: 10px 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 4px; font-weight: bold;\">Xác nhận tài khoản</a>"
                + "</p>"
                + "<p>Nếu bạn không thể nhấp vào nút, vui lòng sao chép và dán đường link sau vào trình duyệt của bạn:</p>"
                + "<p style=\"word-break: break-all;\">" + verificationUrl + "</p>"
                + "<p>Lưu ý: Liên kết này sẽ hết hạn sau 24 giờ.</p>"
                + "<p>Nếu bạn không đăng ký tài khoản này, vui lòng bỏ qua email này.</p>"
                + "<p>Trân trọng,<br>Đội ngũ hỗ trợ</p>"
                + "</div>";
    }
    
    private String buildPasswordResetEmail(String username, String resetUrl) {
        return "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px;\">"
                + "<h2 style=\"color: #333;\">Khôi phục mật khẩu</h2>"
                + "<p>Xin chào " + username + ",</p>"
                + "<p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn. Để đặt lại mật khẩu, vui lòng nhấp vào nút bên dưới:</p>"
                + "<p style=\"text-align: center;\">"
                + "<a href=\"" + resetUrl + "\" style=\"display: inline-block; padding: 10px 20px; background-color: #E53E3E; color: white; text-decoration: none; border-radius: 4px; font-weight: bold;\">Đặt lại mật khẩu</a>"
                + "</p>"
                + "<p>Nếu bạn không thể nhấp vào nút, vui lòng sao chép và dán đường link sau vào trình duyệt của bạn:</p>"
                + "<p style=\"word-break: break-all;\">" + resetUrl + "</p>"
                + "<p>Lưu ý: Liên kết này sẽ hết hạn sau 1 giờ.</p>"
                + "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                + "<p>Trân trọng,<br>Đội ngũ hỗ trợ</p>"
                + "</div>";
    }
} 