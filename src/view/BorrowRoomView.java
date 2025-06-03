package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class BorrowRoomView {
    public static Parent getView() {
        try {
            return FXMLLoader.load(DeviceView.class.getResource("/fxml/borrow-room/index.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
