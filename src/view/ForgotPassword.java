package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ForgotPassword {
    public static Parent getView() {
        try {
            return FXMLLoader.load(DeviceView.class.getResource("/fxml/login/forgot.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
