package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class IncidentHandlingView {
    public static Parent getView() {
        try {
            return FXMLLoader.load(IncidentHandlingView.class.getResource("/fxml/Maintenance/ReportHandling.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}