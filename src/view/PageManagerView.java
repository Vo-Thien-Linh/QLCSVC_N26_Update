package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PageManagerView {
    public static Parent getView() {
        try {
            return FXMLLoader.load(DeviceView.class.getResource("/fxml/layout/PageManagerView.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
