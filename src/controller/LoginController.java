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

        // Trì hoãn đặt vị trí ở giữa và khóa kích thước
        Platform.runLater(() -> {
            Stage stage = (Stage) notificationArea.getScene().getWindow();
            if (stage != null) {
                stage.centerOnScreen();
                stage.setResizable(false); // Không cho phép thu phóng cho đăng nhập
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
                int roleId = user.getRole() != null ? user.getRole().getRoleId() : 0;
                String fxmlPath = "/fxml/layout/PageManagerView.fxml"; // Giao diện chính cho tất cả vai trò
                System.out.println("Đang chuyển đến giao diện: roleId=" + roleId + ", fxmlPath=" + fxmlPath + ", Tồn tại: " + (getClass().getResource(fxmlPath) != null));
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    Stage stage = (Stage) notificationArea.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setMaximized(true);
                    stage.setResizable(true);
                    System.out.println("Giao diện sau đăng nhập: Role ID = " + roleId + ", Path = " + fxmlPath + ", Maximized = " + stage.isMaximized() + ", Resizable = " + stage.isResizable());
                    stage.setTitle("Trang chủ");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    notificationArea.setText("Lỗi khi chuyển trang: " + e.getMessage() + ", Đường dẫn: " + fxmlPath);
                }
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
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.setTitle("Quên mật khẩu");
            stage.show();
            Stage currentStage = (Stage) notificationArea.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            notificationArea.setText("Lỗi khi chuyển đến trang quên mật khẩu: " + e.getMessage());
            System.err.println("Lỗi chi tiết: " + e.getMessage());
        }
    }
}