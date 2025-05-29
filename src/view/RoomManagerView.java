package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class RoomManagerView{
    public static Parent getView() {
        try {
            return FXMLLoader.load(DeviceView.class.getResource("/fxml/room/inex.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
