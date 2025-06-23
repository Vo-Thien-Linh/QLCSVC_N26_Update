package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginView extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginView.class.getResource("/fxml/login/index.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
//
//        Scene scene = new Scene(fxmlLoader.load(), 1350, 721);
        stage.setTitle("Đăng nhập tài khoản");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}