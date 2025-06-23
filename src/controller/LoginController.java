package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.User;
import model.UserSession;
import service.LoginService;

import java.io.IOException;

public class LoginController {
    @FXML
    private Label welcomeText;

    @FXML
    private TextField userField;

    @FXML
    private PasswordField PasswordField;

    @FXML
    private TextField tempPasswordField;

    @FXML
    private CheckBox displaypassword;

    @FXML
    private ImageView loginImage;

    @FXML
    private TextArea notificationArea;

    private final LoginService loginService = new LoginService();

    public void initialize() {
        if (loginImage != null) {
            loginImage.setImage(new Image(getClass().getResourceAsStream("/img/house-black-silhouette-without-door (1).png")));
        }

        tempPasswordField.managedProperty().bind(tempPasswordField.visibleProperty());
        tempPasswordField.visibleProperty().bind(displaypassword.selectedProperty());
        PasswordField.managedProperty().bind(PasswordField.visibleProperty());
        PasswordField.visibleProperty().bind(displaypassword.selectedProperty().not());
        tempPasswordField.textProperty().bindBidirectional(PasswordField.textProperty());
        notificationArea.setText("");
        notificationArea.setStyle("-fx-background-color: transparent;");

        // Đảm bảo stage login hiện tại luôn nằm giữa màn hình và không được resize
        Platform.runLater(() -> {
            Stage stage = (Stage) notificationArea.getScene().getWindow();
            if (stage != null) {
                stage.centerOnScreen();
                stage.setResizable(false);
            } else {
                System.err.println("Stage không được khởi tạo trong Platform.runLater!");
            }
        });
    }

    @FXML
    protected void onHelloButtonClick() {
        System.out.println("Button Đăng nhập được nhấn");
        String username = userField.getText();
        String password = PasswordField.getText();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            notificationArea.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try {
            User user = loginService.authenticateUser(username, password);
            if (user != null) {
                UserSession.startSession(user.getUserId(), user.getRole() != null ? user.getRole().getRoleId() : 0);
                notificationArea.setText("Đăng nhập thành công! Chào " + user.getFullname());

                String roleName = user.getRole() != null ? user.getRole().getRoleName() : "unknown";
                String fxmlPath = "/fxml/layout/PageManagerView.fxml";
                if ("teacher".equals(roleName)) fxmlPath = "/fxml/borrow-device/index.fxml.fxml";
                if ("maintenance".equals(roleName)) fxmlPath = "/fxml/statistical/index.fxml.fxml";

                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                // ✅ Tạo Stage mới để tránh kế thừa trạng thái giao diện cũ
                Stage newStage = new Stage();
                newStage.setTitle("Trang chủ");
                newStage.setScene(new Scene(root));
                newStage.setMaximized(true); // Toàn màn hình
                newStage.show();

                // ✅ Đóng Stage đăng nhập hiện tại
                Stage currentStage = (Stage) notificationArea.getScene().getWindow();
                currentStage.close();

            } else {
                notificationArea.setText("Đăng nhập thất bại! Tên đăng nhập hoặc mật khẩu không đúng.");
                System.out.println("Đăng nhập thất bại - Username: " + username + ", Password: " + password);
            }
        } catch (Exception e) {
            e.printStackTrace();
            notificationArea.setText("Đã xảy ra lỗi khi đăng nhập. Vui lòng thử lại!");
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        System.out.println("Checkbox Hiển thị mật khẩu được nhấn");
    }

    @FXML
    private void forgotPassword() {
        System.out.println("Nút Quên mật khẩu được nhấn. Kiểm tra tài nguyên: " + getClass().getResource("/fxml/login/forgot.fxml"));
        try {
            if (getClass().getResource("/fxml/login/forgot.fxml") == null) {
                throw new IOException("File forgot.fxml không được tìm thấy tại /fxml/login/forgot.fxml");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/forgot.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage(); // Mở stage mới
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.setTitle("Quên mật khẩu");
            stage.show();

            // Đóng login stage hiện tại
            Stage currentStage = (Stage) notificationArea.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            notificationArea.setText("Lỗi khi chuyển đến trang quên mật khẩu: " + e.getMessage());
        }
    }
}
