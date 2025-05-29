package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class StatisticalView {
    public static Parent getView() {
        try {
            return FXMLLoader.load(StatisticalView.class.getResource("/fxml/statistical/index.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
