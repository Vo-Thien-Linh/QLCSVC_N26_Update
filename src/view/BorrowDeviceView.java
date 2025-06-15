package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class BorrowDeviceView {
    public static Parent getView() {
        try {
            return FXMLLoader.load(DeviceView.class.getResource("/fxml/borrow-device/index.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
