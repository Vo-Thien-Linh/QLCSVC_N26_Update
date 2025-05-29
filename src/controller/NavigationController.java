package controller;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class NavigationController {
    private StackPane contentPane;

    public NavigationController(StackPane contentPane) {
        this.contentPane = contentPane;
    }

    public Button createNavButton(String text, Node content) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: transparent");
        btn.setOnAction(e -> switchView(content));
        return btn;
    }

    private void switchView(Node view) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(view);
    }
}

