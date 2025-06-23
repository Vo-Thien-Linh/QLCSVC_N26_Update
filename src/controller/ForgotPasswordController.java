package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.User;
import repository.ManagerUserRepository;
import utils.EmailUtils;
import utils.PasswordEncryptionUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ForgotPasswordController {
    @FXML
    private TextField emailField;
    @FXML
    private Button sendCodeButton;
    @FXML
    private TextField codeField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button submitButton;
    @FXML
    private Button backButton;
    @FXML
    private TextArea notificationArea;

    private ManagerUserRepository userRepository = new ManagerUserRepository();
    private Map<String, String> verificationCodes = new HashMap<>(); // Lưu mã xác thực theo email
    private static final Random random = new Random();

    @FXML
    public void initialize() {
        if (sendCodeButton == null || codeField == null || newPasswordField == null || confirmPasswordField == null ||
                submitButton == null || backButton == null || notificationArea == null) {
            System.err.println("Một hoặc nhiều thành phần FXML không được ánh xạ: " +
                    "sendCodeButton=" + (sendCodeButton == null) +
                    ", codeField=" + (codeField == null) +
                    ", newPasswordField=" + (newPasswordField == null) +
                    ", confirmPasswordField=" + (confirmPasswordField == null) +
                    ", submitButton=" + (submitButton == null) +
                    ", backButton=" + (backButton == null) +
                    ", notificationArea=" + (notificationArea == null));
        } else {
            sendCodeButton.setDisable(false);
            codeField.setDisable(true);
            newPasswordField.setDisable(true);
            confirmPasswordField.setDisable(true);
            submitButton.setDisable(true);
            notificationArea.setText("");
            notificationArea.setStyle("-fx-background-color: transparent;");
        }
        // Trì hoãn đặt vị trí ở giữa và khóa kích thước
        Platform.runLater(() -> {
            Stage stage = (Stage) notificationArea.getScene().getWindow();
            if (stage != null) {
                stage.centerOnScreen();
                stage.setResizable(false); // Không cho phép thu phóng
            } else {
                System.err.println("Stage không được khởi tạo trong Platform.runLater!");
            }
        });
    }

    @FXML
    private void sendVerificationCode() {
        if (sendCodeButton == null) {
            notificationArea.setText("Nút Gửi mã xác thực không được khởi tạo!");
            return;
        }
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            notificationArea.setText("Vui lòng nhập email!");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            notificationArea.setText("Email không hợp lệ!");
            return;
        }

        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            notificationArea.setText("Email không tồn tại!");
            return;
        }

        String verificationCode = String.format("%06d", random.nextInt(1000000));
        verificationCodes.put(email, verificationCode);
        try {
            System.out.println("Đang gửi mã xác thực đến: " + email);
            EmailUtils.sendVerificationCode(email, verificationCode);
            System.out.println("Mã xác thực " + verificationCode + " đã được gửi.");
            notificationArea.setText("Mã xác thực đã được gửi đến email của bạn!");
            sendCodeButton.setDisable(true);
            codeField.setDisable(false);
            newPasswordField.setDisable(false);
            confirmPasswordField.setDisable(false);
            submitButton.setDisable(false);
        } catch (MessagingException e) {
            System.err.println("Lỗi gửi email: " + e.getMessage());
            notificationArea.setText("Lỗi khi gửi mã xác thực: " + e.getMessage());
        }
    }

    @FXML
    private void submitNewPassword() {
        String email = emailField.getText().trim();
        String code = codeField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        String storedCode = verificationCodes.get(email);
        if (storedCode == null || !storedCode.equals(code)) {
            notificationArea.setText("Mã xác thực không đúng hoặc đã hết hạn!");
            return;
        }

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            notificationArea.setText("Vui lòng nhập đầy đủ mật khẩu!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            notificationArea.setText("Mật khẩu nhập lại không khớp!");
            return;
        }

        User user = userRepository.getUserByEmail(email);
        if (user != null) {
            String oldPassword = user.getPassword();
            String hashedPassword = PasswordEncryptionUtils.hashPassword(newPassword);
            System.out.println("Mật khẩu cũ: " + oldPassword + ", Mật khẩu mới (trước khi set): " + hashedPassword);

            // Cập nhật trực tiếp đối tượng user
            user.setPassword(hashedPassword);
            System.out.println("Mật khẩu sau khi set: " + user.getPassword());

            // Tạo bản sao với giá trị mới
            User updatedUser = new User(
                    user.getUserId(),
                    user.getFullname(),
                    user.getUsername(),
                    user.getThumbnail(),
                    user.getYearold(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    hashedPassword, // Sử dụng trực tiếp hashedPassword
                    user.getStatus(),
                    user.getRole()
            );
            System.out.println("Mật khẩu trong bản sao: " + updatedUser.getPassword());

            if (userRepository.edit(updatedUser)) {
                notificationArea.setText("Đổi mật khẩu thành công!");
                verificationCodes.remove(email);
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            Stage stage = (Stage) notificationArea.getScene().getWindow();
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/index.fxml"));
                                Parent root = loader.load();
                                stage.setScene(new Scene(root));
                                stage.centerOnScreen(); // Đặt giữa màn hình
                                stage.setResizable(false); // Không cho phép thu phóng
                                stage.setTitle("Đăng nhập");
                                stage.show();
                                System.out.println("Chuyển về trang đăng nhập thành công.");
                            } catch (IOException e) {
                                e.printStackTrace();
                                notificationArea.setText("Lỗi khi chuyển về trang đăng nhập: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                notificationArea.setText("Lỗi khi cập nhật mật khẩu!");
            }
        }
    }

    @FXML
    private void backToLogin() {
        try {
            System.out.println("Đang quay lại trang đăng nhập...");
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/index.fxml"));
            if (getClass().getResource("/fxml/login/index.fxml") == null) {
                throw new IOException("File index.fxml không được tìm thấy tại /fxml/login/index.fxml");
            }
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.centerOnScreen(); // Đặt giữa màn hình
            stage.setResizable(false); // Không cho phép thu phóng
            stage.setTitle("Đăng nhập");
            stage.show();
            System.out.println("Quay lại trang đăng nhập thành công.");
        } catch (IOException e) {
            e.printStackTrace();
            notificationArea.setText("Lỗi khi quay lại: " + e.getMessage());
            System.err.println("Lỗi chi tiết khi quay lại: " + e.getMessage());
        }
    }
}