package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class PermissionView {
    public static Parent getView() {
        try {
            return FXMLLoader.load(DeviceView.class.getResource("/fxml/role/permission.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
