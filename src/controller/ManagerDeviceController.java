package controller;

import javafx.animation.ScaleTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

import model.Device;
import model.DeviceStatus;
import model.UserSession;
import repository.ManagerDeviceRepository;
import repository.PermissionRepository;
import utils.ScannerUtils;

public class ManagerDeviceController implements Initializable {

    @FXML private TableView<Device> tblDevices;
    @FXML private TableColumn<Device, Boolean> colCheck;
    @FXML private TableColumn<Device, String>  colId, colImage, colDeviceName, colDeviceType, colPurchaseDate,
            colSupplier, colStatus, colRoom, colAllow;
    @FXML private TableColumn<Device, BigDecimal> colPrice;
    @FXML private TableColumn<Device, Void> colActions;
    @FXML private TableColumn<Device, Integer> colQuantity;
    @FXML private VBox rootPane;
    @FXML private ComboBox<String> cboSearchType, cboChangeStatus;
    @FXML private TextField txtSearch;
    @FXML private StackPane btnSearch;
    @FXML private Pagination pagination;
    @FXML private StackPane btnApply;
    @FXML private StackPane btnAddNew;

    private PermissionRepository permissionRepository = new PermissionRepository();
    private ManagerDeviceRepository managerDeviceRepository = new ManagerDeviceRepository();
    private String status = null;
    private String keyword = null;
    private int limitItem = 7;

    private int roleId = UserSession.getRoleId();
    private boolean canCreate = permissionRepository.isAllowed(roleId, "Quản lý thiết bị", "Thêm mới");
    private boolean canEdit = permissionRepository.isAllowed(roleId, "Quản lý thiết bị", "Chỉnh sửa");
    private boolean canDelete = permissionRepository.isAllowed(roleId, "Quản lý thiết bị", "Xóa");

    @FXML
    private void handleAddNewAction(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/device/create.fxml"));
            Parent root = loader.load();

            CreateDeviceController controller = loader.getController();
            controller.setOnDeviceAdded(() -> {
                loadPage(0);
            });

            Stage addNewStage = new Stage();
            addNewStage.setTitle("Thêm thiết bị mới");
            addNewStage.setScene(new Scene(root));
            addNewStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        setupButton();
        dataDumpCbo();
        cboSearchType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateStatus(newVal);
            loadPage(0);
        });

        btnSearch.setOnMouseClicked(event -> {
            keyword = txtSearch.getText();
            loadPage(0);
        });

        pagination.setPageFactory(pageIndex -> {
            loadPage(pageIndex);
            return new VBox();
        });

        btnApply.setOnMouseClicked(event -> {
            ObservableList<Device> devices = tblDevices.getItems();
            List<String> ids = devices.stream().filter(Device::isSelected).map(Device::getId).collect(Collectors.toList());
            String status = cboChangeStatus.getSelectionModel().getSelectedItem();
            for(DeviceStatus deviceStatus : DeviceStatus.values()) {
                if(deviceStatus.toString().equals(status)) {
                    status = deviceStatus.name();
                    break;
                }
            }
            Boolean success = managerDeviceRepository.changeStatus(ids, status);
            if(success){
                ScannerUtils.showInfo("Thông báo", "Đổi trạng thái thành công!");
                loadPage(pagination.getCurrentPageIndex());
            } else {
                ScannerUtils.showError("Thông báo", "Đổi trạng thái không thành công!");
            }
        });

        loadPage(0);
        permisson();
    }

    private void permisson(){
//        xét quyền cho nút thêm mới
        btnAddNew.setManaged(canCreate);
        btnAddNew.setVisible(canCreate);

//        xét quyền cho phần thay đổi nhiều trạng thái
        btnApply.setManaged(canEdit);
        btnApply.setVisible(canEdit);
        cboChangeStatus.setManaged(canEdit);
        cboChangeStatus.setVisible(canEdit);

        if(!canEdit && !canDelete){
            colActions.setVisible(false);
        }
    }

    private void updateStatus(String newValue) {
        status = newValue;
        for (DeviceStatus deviceStatus : DeviceStatus.values()) {
            if (deviceStatus.toString().equals(newValue)) {
                status = deviceStatus.name();
                break;
            }
        }
    }

    private void loadPage(int pageIndex) {
        int skip = pageIndex * limitItem;
        List<Device> devices = managerDeviceRepository.filterAndSearch(status, keyword, limitItem, skip);
        int totalPages = (int) Math.ceil((double) managerDeviceRepository.countDevices(status, keyword) / limitItem);
        pagination.setPageCount(totalPages);
        loadDeviceData(devices);
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

    private void dataDumpCbo() {
        if (cboChangeStatus != null && cboSearchType != null) {
            List<String> searchTypes = new ArrayList<>();
            List<String> changeStatus = new ArrayList<>();
            searchTypes.add("Tất cả");
            for (DeviceStatus status : DeviceStatus.values()) {
                searchTypes.add(status.toString());
                changeStatus.add(status.toString());
            }
            changeStatus.add("Xóa");
            cboChangeStatus.setItems(FXCollections.observableArrayList(changeStatus));
            cboSearchType.setItems(FXCollections.observableArrayList(searchTypes));

            if (!cboSearchType.getItems().isEmpty() && !cboChangeStatus.getItems().isEmpty()) {
                cboSearchType.getSelectionModel().selectFirst();
                cboChangeStatus.getSelectionModel().selectFirst();
            }
        }
    }

    public void loadDeviceData(List<Device> devices) {
        if(colId != null && colImage != null && colDeviceName != null && colDeviceType != null && colPurchaseDate != null && colSupplier != null && colPrice != null && colQuantity != null && colStatus != null && colRoom != null && tblDevices != null) {
            colCheck.setCellFactory(tc -> new TableCell<Device, Boolean>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOnAction(e -> {
                        Device device = getTableView().getItems().get(getIndex());
                        device.setSelected(checkBox.isSelected());
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Device device = getTableView().getItems().get(getIndex());
                        checkBox.setSelected(device.isSelected());
                        setGraphic(checkBox);
                    }
                }
            });
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colImage.setCellValueFactory(new PropertyValueFactory<>("thumbnail"));

            colImage.setCellFactory(column -> new TableCell<Device, String>() {
                private final ImageView imageView = new ImageView();

                {
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(60);
                    imageView.setPreserveRatio(true);
                }

                @Override
                protected void updateItem(String imageUrl, boolean empty) {
                    super.updateItem(imageUrl, empty);
                    if (empty || imageUrl == null || imageUrl.isEmpty()) {
                        setGraphic(null);
                    } else {
                        try {
                            Image image = new Image(imageUrl, true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            setGraphic(null);
                        }
                    }
                }
            });
            colDeviceName.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
            colDeviceType.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
            colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier"));
            colPurchaseDate.setCellValueFactory(new PropertyValueFactory<>("purchaseDate"));
            colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

            colPrice.setCellFactory(column -> new TableCell<Device, BigDecimal>() {
                @Override
                protected void updateItem(BigDecimal item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                        setText(formatter.format(item) + " VND");
                    }
                }
            });
            colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            colStatus.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));

            colRoom.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getRoom().getRoomNumber()));
            colAllow.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getAllow() ? "Mượn" : "Không mượn")
            );
            colActions.setCellFactory(new Callback<TableColumn<Device, Void>, TableCell<Device, Void>>() {
                @Override
                public TableCell<Device, Void> call(final TableColumn<Device, Void> param) {
                    return new TableCell<Device, Void>() {
                        private final SVGPath editIcon = new SVGPath();
                        private final SVGPath deleteIcon = new SVGPath();
                        private final HBox actionBox = new HBox(10);

                        {
                            // Icon sửa
                            editIcon.setContent("M2 22q-0.825 0-1.412-0.588Q0 20.825 0 20V4q0-0.825 0.588-1.412Q1.175 2 2 2h10.8L11.4 3.4H2v16h16v-7.95L19.4 10V20q0 0.825-0.588 1.412Q18.225 22 17.4 22H2Zm9-9Zm-5 5v-4.25L17.15 2.6q0.3-0.3 0.675-0.45T18.55 2q0.4 0 0.775 0.15t0.675 0.45l1.4 1.4q0.275 0.3 0.425 0.675T22 5.95q0 0.4-0.15 0.775t-0.45 0.675L11 18H6Zm13.4-13.4-1.4-1.4 1.4 1.4ZM10 14h1.4l5.8-5.8-0.7-0.7-0.725-0.7L10 12.6V14Zm6.5-6.5-0.725-0.7 0.725 0.7 0.7 0.7-0.7-0.7Z");
                            editIcon.setFill(Color.web("#FFC107"));
                            StackPane iconWrapper = new StackPane(editIcon);
                            iconWrapper.setPrefSize(24, 24);
                            iconWrapper.setStyle("-fx-cursor: hand;");

                            // Icon xóa
                            deleteIcon.setContent("M6 19a2 2 0 002 2h8a2 2 0 002-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                            deleteIcon.setFill(Color.web("#F44336"));
                            deleteIcon.setCursor(Cursor.HAND);

                            // Bắt sự kiện chỉnh sửa
                            iconWrapper.setOnMouseClicked(event -> {
                                Device device = getTableView().getItems().get(getIndex());

                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/device/EditDevice.fxml"));
                                    Parent editRoot = loader.load();

                                    EditDeviceController controller = loader.getController();

                                    controller.setDevice(device);

                                    Stage editStage = new Stage();
                                    editStage.setTitle("Chỉnh sửa thiết bị");
                                    editStage.setScene(new Scene(editRoot));
                                    editStage.setOnHidden(e -> {
                                        loadPage(pagination.getCurrentPageIndex());
                                    });
                                    editStage.show();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });


                            deleteIcon.setOnMouseClicked(event -> {
                                Device device = getTableView().getItems().get(getIndex());

                                if(ScannerUtils.showConfirm("Xác nhận xóa", "Thiết bị: " + device.getDeviceName())){
                                    boolean success = managerDeviceRepository.delete(device.getId());
                                    if(success) {
                                        ScannerUtils.showInfo("Thông báo", "Xóa thiết bị thành công!");
                                        loadPage(pagination.getCurrentPageIndex());
                                    } else {
                                        ScannerUtils.showError("Thông báo", "Xóa thiết bị không thành công!");
                                    }
                                }
                            });



                            actionBox.setAlignment(Pos.CENTER);
                            if(canEdit){
                                actionBox.getChildren().addAll(iconWrapper);
                            }

                            if(canDelete){
                                actionBox.getChildren().addAll(deleteIcon);
                            }
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
            tblDevices.setItems(FXCollections.observableArrayList(devices));
            tblDevices.setSelectionModel(null);
        }
    }
}
