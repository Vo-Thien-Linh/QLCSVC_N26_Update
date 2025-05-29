package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class RoleView {
    public static Parent getView() {
        try {
            return FXMLLoader.load(DeviceView.class.getResource("/fxml/role/index.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
