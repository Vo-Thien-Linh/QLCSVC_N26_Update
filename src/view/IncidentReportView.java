package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class IncidentReportView {
    public static Parent getView() {
        try {
            return FXMLLoader.load(IncidentReportView.class.getResource("/fxml/Maintenance/IncidentReport.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}