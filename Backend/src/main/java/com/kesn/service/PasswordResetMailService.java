package com.kesn.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Gửi mã OTP quên mật khẩu qua SMTP.
 * Với Gmail: dùng App Password (không dùng mật khẩu đăng nhập thường).
 * Chỉ được tạo bean khi có {@code spring.mail.host}.
 */
@Service
@ConditionalOnProperty(name = "spring.mail.host")
public class PasswordResetMailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public PasswordResetMailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:${spring.mail.username:}}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendResetCode(String toEmail, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isBlank()) {
                message.setFrom(fromEmail);
            }
            message.setTo(toEmail);
            message.setSubject("Kesn Store - Ma xac nhan dat lai mat khau");
            message.setText(buildBody(code));
            mailSender.send(message);
        } catch (MailException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            String hint = cause.getMessage() != null ? cause.getMessage() : ex.getMessage();
            throw new RuntimeException(
                    "Khong gui duoc email SMTP. Gmail: dung App Password (16 ky tu, khong khoang trang) trong "
                            + "spring.mail.password, bat 2FA, spring.mail.username = app.mail.from. Chi tiet: "
                            + (hint != null && hint.length() > 200 ? hint.substring(0, 200) + "…" : hint),
                    ex);
        }
    }

    private String buildBody(String code) {
        return "Xin chao,\n\n"
                + "Ban vua yeu cau dat lai mat khau cho tai khoan Kesn Store.\n"
                + "Ma xac nhan cua ban la: " + code + "\n\n"
                + "Ma co hieu luc trong 15 phut.\n"
                + "Neu ban khong thuc hien yeu cau nay, vui long bo qua email.\n\n"
                + "Tran trong,\nKesn Store";
    }
}
