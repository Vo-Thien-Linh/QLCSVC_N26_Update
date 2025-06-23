package controller;

import javafx.animation.ScaleTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import model.Device;
import model.Room;
import repository.BorrowDeviceRepository;
import utils.ScannerUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class BorrowDeviceController implements Initializable {
    @FXML private VBox rootPane;
    @FXML private StackPane btnSearch;
    @FXML private Pagination pagination;
    @FXML private TextField txtSearch;
    @FXML private TableView<Device> tblBorrowDevice;
    @FXML private TableColumn<Device, String>  colId, colName, colType,
            colSupplier, colStatus, colLocation;
    @FXML private TableColumn<Device, Void> colActions;
    @FXML private TableColumn<Device, Integer>  colAvailableQuantity;
    @FXML private StackPane btnListConfirm;

    private BorrowDeviceRepository borrowDeviceRepository = new BorrowDeviceRepository();
    private String keyword = null;
    private int limitItem = 7;

    public void initialize(URL location, ResourceBundle resources){
        setupButton();
        handleBtnListConfirm();
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
    }

    private void handleBtnListConfirm(){
        btnListConfirm.setOnMouseClicked(event -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/borrow-device/request.fxml"));
            Parent root = null;

            try {
                root = loader.load();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Yêu cầu mượn thiết bị");
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

    public void loadPage(int pageIndex) {
        int skip = pageIndex * limitItem;
        List<Device> devices = borrowDeviceRepository.search(keyword, limitItem, skip);
        int totalPages = (int) Math.ceil((double) borrowDeviceRepository.countDevices(keyword) / limitItem);
        pagination.setPageCount(totalPages);
        loadDevice(devices);
    }

    private void loadDevice(List<Device> devices) {
        if(colId != null && colName != null && colType != null && colSupplier != null && colAvailableQuantity != null && colStatus != null && colLocation != null && tblBorrowDevice != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
            colType.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
            colType.setCellFactory(column -> {
                return new TableCell<Device, String>() {
                    private final Text text = new Text();

                    {
                        text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
                        text.setLineSpacing(2);
                        setGraphic(text);
                        setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            text.setText("");
                        } else {
                            text.setText(item);
                        }
                    }
                };
            });
            colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier"));
            colAvailableQuantity.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));

            colStatus.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));

            colLocation.setCellValueFactory(cellData -> {
                Room room = cellData.getValue().getRoom();
                String location = room.getLocation();
                String roomNumber = " - Phòng " + room.getRoomNumber();
                return new ReadOnlyStringWrapper(location + roomNumber);
            });
            colActions.setCellFactory(new Callback<TableColumn<Device, Void>, TableCell<Device, Void>>() {
                @Override
                public TableCell<Device, Void> call(final TableColumn<Device, Void> param) {
                    return new TableCell<Device, Void>() {
                        private final Button requestButton = new Button("Yêu cầu mượn");
                        private final HBox actionBox = new HBox(10, requestButton);


                        {
                            requestButton.getStyleClass().add("requestButton");
                            actionBox.setAlignment(Pos.CENTER);
                            actionBox.setPadding(new Insets(5, 0, 5, 0));

                            requestButton.setOnAction(event -> {
                                Device device = getTableView().getItems().get(getIndex());

                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/borrow-device/confirm.fxml"));
                                Parent requestRoot = null;
                                try {
                                    requestRoot = loader.load();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                BorrowDeviceConfirmController controller = loader.getController();
                                controller.setDevice(device);

                                Stage editStage = new Stage();
                                editStage.setTitle("Mượn thiết bị");
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

            tblBorrowDevice.setItems(FXCollections.observableArrayList(devices));
            tblBorrowDevice.setSelectionModel(null);
        }
    }
}
