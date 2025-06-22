package controller;

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
        notificationArea.setText(""); // Xóa nội dung mặc định khi khởi động
        notificationArea.setStyle("-fx-background-color: transparent;"); // Đảm bảo nền trong suốt
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
                String fxmlPath = "/fxml/layout/PageManagerView.fxml"; // Mặc định cho quản trị viên
                if ("teacher".equals(roleName)) fxmlPath = "/fxml/layout/TeacherView.fxml";
                if ("maintenance".equals(roleName)) fxmlPath = "/fxml/layout/MaintenanceView.fxml";

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    Stage stage = (Stage) notificationArea.getScene().getWindow();
                    stage.setScene(new Scene(root, 1350, 721));
                    stage.setTitle("Trang chủ");
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    notificationArea.setText("Lỗi khi chuyển trang: " + e.getMessage());
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
        String username = userField.getText();
        if (username == null || username.trim().isEmpty()) {
            notificationArea.setText("Vui lòng nhập tên đăng nhập!");
            return;
        }
        User user = loginService.getUserByUsername(username);
        if (user != null) {
            notificationArea.setText("Hướng dẫn reset mật khẩu đã được gửi!");
        } else {
            notificationArea.setText("Tên đăng nhập không tồn tại!");
        }
    }
}