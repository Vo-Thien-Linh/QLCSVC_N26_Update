package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class UserView{
    public static Parent getView() {
        try {
            return FXMLLoader.load(DeviceView.class.getResource("/fxml/user/index.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
