package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailUtils {
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "buiminhtrong54@gmail.com"; // Thay bằng email của bạn
    private static final String EMAIL_PASSWORD = "ehoq ramh mcbv casw"; // Thay bằng mật khẩu ứng dụng

    public static void sendVerificationCode(String toEmail, String code) throws MessagingException {
        System.out.println("Đang khởi tạo kết nối SMTP đến: " + SMTP_HOST + ":" + SMTP_PORT);
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", SMTP_HOST); // Tin cậy host để tránh lỗi SSL

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                System.out.println("Xác thực với email: " + EMAIL_USERNAME);
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            System.out.println("Đang tạo và gửi email đến: " + toEmail);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã xác thực khôi phục mật khẩu");
            message.setText("Mã xác thực của bạn là: " + code + "\nMã có hiệu lực trong 5 phút.");

            Transport.send(message);
            System.out.println("Email gửi thành công đến: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("Nguyên nhân chi tiết: " + e.getCause().getMessage());
            }
            throw new MessagingException("Lỗi gửi email: " + e.getMessage(), e);
        }
    }
}