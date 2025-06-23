package controller;

import javafx.animation.ScaleTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import model.Device;
import model.Room;
import repository.BorrowRoomRepository;
import utils.ScannerUtils;
import view.BorrowRoomView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class BorrowRoomController implements Initializable {
    @FXML private VBox rootPane;
    @FXML private ComboBox<String> cboSearchType;
    @FXML private TableView<Room> tblBorrowRoom;
    @FXML private TableColumn<Room, String> colId, colName, colType, colCapacity, colLocation, colStatus;
    @FXML private TableColumn<Room, Void> colActions;
    @FXML private StackPane btnSearch;
    @FXML private Pagination pagination;
    @FXML private TextField txtSearch;
    @FXML private StackPane btnListConfirm;

    private BorrowRoomRepository borrowRoomRepository = new BorrowRoomRepository();
    private String typeName = null;
    private String keyword = null;
    private int limitItem = 7;

    public void initialize(URL location, ResourceBundle resources) {
        setupButton();
        loadRoomType();
        cboSearchType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            typeName = newVal;
            loadPage(0);
        });

        btnSearch.setOnMouseClicked(event -> {
            keyword = txtSearch.getText();
            loadPage(0);
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            keyword = newValue.trim();
            loadPage(0);
        });

        pagination.setPageFactory(pageIndex -> {
            loadPage(pageIndex);
            return new VBox();
        });

        loadPage(0);
        handleBtnListConfirm();
    }

    private void handleBtnListConfirm(){
        btnListConfirm.setOnMouseClicked(event -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/borrow-room/request.fxml"));
            Parent root = null;

            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Yêu cầu mượn phòng");
            stage.show();
        });
    }

    private void setupButton() {
        if(rootPane != null){
            Set<Node> buttons = rootPane.lookupAll(".custom-button");
            for (Node node : buttons) {
                if (node instanceof StackPane btn) {
                    Rectangle layer = null;
                    for (Node child : btn.getChildren()) {
                        if (child instanceof Rectangle r && r.getStyleClass().contains("before-layer")) {
                            layer = r;
                            break;
                        }
                    }

                    if (layer != null) {
                        final Rectangle finalLayer = layer;

                        btn.setOnMouseEntered(e -> {
                            ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.3), finalLayer);
                            scaleIn.setFromX(0);
                            scaleIn.setToX(1);
                            scaleIn.playFromStart();
                        });

                        btn.setOnMouseExited(e -> {
                            ScaleTransition scaleOut = new ScaleTransition(Duration.seconds(0.3), finalLayer);
                            scaleOut.setFromX(1);
                            scaleOut.setToX(0);
                            scaleOut.playFromStart();
                        });

                    }
                }
            }
        }
    }

    private void loadPage(int pageIndex) {
        int skip = pageIndex * limitItem;
        List<Room> rooms = borrowRoomRepository.filterAndSearch(typeName, keyword, limitItem, skip);
        int totalPages = (int) Math.ceil((double) borrowRoomRepository.countRoom(typeName, keyword) / limitItem);
        pagination.setPageCount(totalPages);
        loadRoom(rooms);
    }

    private void loadRoomType(){
        List<String> datas = borrowRoomRepository.roomTypeData();
        List<String> searchType = new ArrayList<>();
        searchType.add("Tất cả");
        for(String data : datas){
            searchType.add(data);
        }

        cboSearchType.setItems(FXCollections.observableArrayList(searchType));

        if(!cboSearchType.getItems().isEmpty()){
            cboSearchType.getSelectionModel().selectFirst();
        }
    }

    private void loadRoom(List<Room> rooms){
        if(colName != null && colType != null && colCapacity != null && colLocation != null && colStatus != null && tblBorrowRoom != null){
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            colType.setCellValueFactory(new PropertyValueFactory<>("room_type"));
            colCapacity.setCellValueFactory(new PropertyValueFactory<>("seatingCapacity"));
            colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
            colStatus.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));
            colActions.setCellFactory(new Callback<TableColumn<Room, Void>, TableCell<Room, Void>>() {
                @Override
                public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                    return new TableCell<Room, Void>() {
                        private final Button requestButton = new Button("Yêu cầu mượn");
                        private final HBox actionBox = new HBox(10, requestButton);


                        {
                            requestButton.getStyleClass().add("requestButton");
                            actionBox.setAlignment(Pos.CENTER);
                            actionBox.setPadding(new Insets(5, 0, 5, 0));

                            requestButton.setOnAction(event -> {
                                String roomName = getTableView().getItems().get(getIndex()).getRoomNumber();
                                String roomId = getTableView().getItems().get(getIndex()).getId();

                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/borrow-room/confirm.fxml"));
                                Parent requestRoot = null;
                                try {
                                    requestRoot = loader.load();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                BorrowRoomConfirmController controller = loader.getController();
                                controller.setRoom(roomId, roomName);
                                controller.setReloadRoomListCallback(() -> loadPage(pagination.getCurrentPageIndex()));


                                Stage editStage = new Stage();
                                editStage.setTitle("Mượn Phòng");
                                editStage.setScene(new Scene(requestRoot));
                                editStage.show();
                            });
                        }


                        @Override
                        protected void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(actionBox);
                            }
                        }
                    };
                }
            });

            tblBorrowRoom.setItems(FXCollections.observableArrayList(rooms));
            tblBorrowRoom.setSelectionModel(null);
        }
    }

}
