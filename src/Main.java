import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import view.PageManagerView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        PageManagerView pageManagerView = new PageManagerView();
        Parent root = pageManagerView.getView();
        Scene scene = new Scene(root);
        primaryStage.setTitle("Quản lý thiết bị - Admin");
        primaryStage.setScene(scene);

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/logo-icon.png")));
        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
