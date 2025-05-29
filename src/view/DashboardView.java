package view;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;


public class DashboardView{
    public static Parent getView() {
        try {
            return FXMLLoader.load(DashboardView.class.getResource("/fxml/dashboard/index.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}