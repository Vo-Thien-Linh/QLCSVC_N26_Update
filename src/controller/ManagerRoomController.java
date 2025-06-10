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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
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
import model.Room;
import model.RoomStatus;
import model.UserSession;
import repository.ManagerRoomRepository;
import repository.PermissionRepository;
import utils.ScannerUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class ManagerRoomController implements Initializable {

    @FXML private TableView<Room> tblRooms;
    @FXML private TableColumn<Room, Boolean> colCheck;
    @FXML private TableColumn<Room, String> colId, colRoomNumber, colRoomType, colStatus, colLocation;
    @FXML private TableColumn<Room, Integer> colSeatingCapacity;
    @FXML private TableColumn<Room, Void> colActions;
    @FXML private VBox rootPane;
    @FXML private ComboBox<String> cboSearchType, cboChangeStatus;
    @FXML private TextField txtSearch;
    @FXML private StackPane btnSearch;
    @FXML private Pagination pagination;
    @FXML private StackPane btnApply;
    @FXML private StackPane btnAddNew;
    @FXML private SVGPath iconAdd;
    @FXML private Text textAdd;

    private PermissionRepository permissionRepository = new PermissionRepository();
    private ManagerRoomRepository managerRoomRepository = new ManagerRoomRepository();
    private String status = null;
    private String keyword = null;
    private int limitItem = 15;

    private int roleId = UserSession.getRoleId();
    private boolean canCreate = permissionRepository.isAllowed(roleId, "Quản lý phòng", "Thêm mới");
    private boolean canEdit = permissionRepository.isAllowed(roleId, "Quản lý phòng", "Chỉnh sửa");
    private boolean canDelete = permissionRepository.isAllowed(roleId, "Quản lý phòng", "Xóa");

    @FXML
    private void handleAddNewAction(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/room/CreateRoom.fxml"));
            Parent root = loader.load();
            CreateRoomController controller = loader.getController();
            controller.setOnRoomAdded(() -> loadPage(0));
            Stage addNewStage = new Stage();
            addNewStage.setTitle("Thêm phòng học mới");
            addNewStage.setScene(new Scene(root));
            addNewStage.show();
        } catch (IOException e) {
            ScannerUtils.showError("Lỗi", "Không thể mở cửa sổ thêm mới!");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Role ID: " + roleId);
        System.out.println("Can Create: " + canCreate + ", Can Edit: " + canEdit + ", Can Delete: " + canDelete);

        setupButton();
        dataDumpCbo();
        cboSearchType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateStatus(newVal);
            loadPage(0);
        });

        btnSearch.setOnMouseClicked(event -> {
            keyword = txtSearch.getText().trim();
            loadPage(0);
        });

        pagination.setPageFactory(pageIndex -> {
            loadPage(pageIndex);
            return new VBox();
        });

        btnApply.setOnMouseClicked(event -> {
            ObservableList<Room> rooms = tblRooms.getItems();
            List<String> ids = rooms.stream().filter(Room::isSelected).map(Room::getId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                ScannerUtils.showError("Lỗi", "Vui lòng chọn ít nhất một phòng!");
                return;
            }
            String selectedStatus = cboChangeStatus.getSelectionModel().getSelectedItem();
            String status = selectedStatus;
            for (RoomStatus roomStatus : RoomStatus.values()) {
                if (roomStatus.toString().equals(selectedStatus)) {
                    status = roomStatus.name();
                    break;
                }
            }
            boolean success = managerRoomRepository.changeStatus(ids, status);
            if (success) {
                ScannerUtils.showInfo("Thông báo", "Đổi trạng thái thành công!");
                loadPage(pagination.getCurrentPageIndex());
            } else {
                ScannerUtils.showError("Thông báo", "Đổi trạng thái không thành công!");
            }
        });

        loadPage(0);
        permission();
    }

    private void permission() {
        btnAddNew.setManaged(canCreate);
        btnAddNew.setVisible(canCreate);
        btnApply.setManaged(canEdit);
        btnApply.setVisible(canEdit);
        cboChangeStatus.setManaged(canEdit);
        cboChangeStatus.setVisible(canEdit);
        colActions.setVisible(canEdit || canDelete);

        System.out.println("Permission check - Can Edit: " + canEdit + ", Can Delete: " + canDelete + ", colActions visible: " + colActions.isVisible());
        if (!canCreate) {
            System.out.println("btnAddNew is hidden due to lack of create permission.");
        }
        if (!canEdit) {
            System.out.println("btnApply and cboChangeStatus are hidden due to lack of edit permission.");
        }
        if (!canEdit && !canDelete) {
            System.out.println("colActions is hidden due to lack of edit/delete permissions.");
        }
    }

    private void updateStatus(String newValue) {
        status = newValue.equals("Tất cả") ? null : newValue;
        for (RoomStatus roomStatus : RoomStatus.values()) {
            if (roomStatus.toString().equals(newValue)) {
                status = roomStatus.name();
                break;
            }
        }
    }

    private void loadPage(int pageIndex) {
        int skip = pageIndex * limitItem;
        List<Room> rooms = managerRoomRepository.filterAndSearch(status, keyword, limitItem, skip);
        if (rooms == null) {
            System.out.println("No rooms found or error occurred while fetching rooms.");
            return;
        }
        int totalPages = (int) Math.ceil((double) managerRoomRepository.countRooms(status, keyword) / limitItem);
        pagination.setPageCount(totalPages);
        loadRoomData(rooms);
    }

    private void setupButton() {
        if (rootPane != null) {
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

            btnAddNew.setOnMouseEntered(e -> {
                iconAdd.setFill(Color.WHITE);
                textAdd.setFill(Color.WHITE);
            });
            btnAddNew.setOnMouseExited(e -> {
                iconAdd.setFill(Color.web("#4099ff"));
                textAdd.setFill(Color.web("#4099ff"));
            });
        }
    }

    private void dataDumpCbo() {
        if (cboChangeStatus != null && cboSearchType != null) {
            List<String> searchTypes = new ArrayList<>();
            List<String> changeStatus = new ArrayList<>();
            searchTypes.add("Tất cả");
            for (RoomStatus status : RoomStatus.values()) {
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

    public void loadRoomData(List<Room> rooms) {
        if (colId != null && colRoomNumber != null && colRoomType != null && colSeatingCapacity != null &&
                colStatus != null && colLocation != null && colActions != null && tblRooms != null) {
            colCheck.setCellFactory(tc -> new TableCell<Room, Boolean>() {
                private final CheckBox checkBox = new CheckBox();
                {
                    checkBox.setOnAction(e -> {
                        Room room = getTableView().getItems().get(getIndex());
                        room.setSelected(checkBox.isSelected());
                    });
                }
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Room room = getTableView().getItems().get(getIndex());
                        checkBox.setSelected(room.isSelected());
                        setGraphic(checkBox);
                    }
                }
            });
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            colRoomType.setCellValueFactory(new PropertyValueFactory<>("room_type"));
            colSeatingCapacity.setCellValueFactory(new PropertyValueFactory<>("seatingCapacity"));
            colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));
            colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

            colActions.setCellFactory(new Callback<TableColumn<Room, Void>, TableCell<Room, Void>>() {
                @Override
                public TableCell<Room, Void> call(final TableColumn<Room, Void> param) {
                    return new TableCell<Room, Void>() {
                        private final SVGPath editIcon = new SVGPath();
                        private final SVGPath deleteIcon = new SVGPath();
                        private final HBox actionBox = new HBox(10);
                        private final StackPane editWrapper;

                        {
                            // Icon chỉnh sửa
                            editIcon.setContent("M2 22q-0.825 0-1.412-0.588Q0 20.825 0 20V4q0-0.825 0.588-1.412Q1.175 2 2 2h10.8L11.4 3.4H2v16h16v-7.95L19.4 10V20q0 0.825-0.588 1.412Q18.225 22 17.4 22H2Zm9-9Zm-5 5v-4.25L17.15 2.6q0.3-0.3 0.675-0.45T18.55 2q0.4 0 0.775 0.15t0.675 0.45l1.4 1.4q0.275 0.3 0.425 0.675T22 5.95q0 0.4-0.15 0.775t-0.45 0.675L11 18H6Zm13.4-13.4-1.4-1.4 1.4 1.4ZM10 14h1.4l5.8-5.8-0.7-0.7-0.725-0.7L10 12.6V14Zm6.5-6.5-0.725-0.7 0.725 0.7 0.7 0.7-0.7-0.7Z");
                            editIcon.setFill(Color.web("#FFC107"));
                            editWrapper = new StackPane(editIcon);
                            editWrapper.setPrefSize(24, 24);
                            editWrapper.setStyle("-fx-cursor: hand;");

                            // Icon xóa
                            deleteIcon.setContent("M6 19a2 2 0 002 2h8a2 2 0 002-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                            deleteIcon.setFill(Color.web("#F44336"));
                            deleteIcon.setCursor(Cursor.HAND);

                            editWrapper.setOnMouseClicked(event -> {
                                Room room = getTableView().getItems().get(getIndex());
                                System.out.println("Edit icon clicked for room: " + room.getRoomNumber());
                                try {
                                    EditRoomController.showEditRoomWindow(room, () -> loadPage(pagination.getCurrentPageIndex()));
                                } catch (IOException e) {
                                    ScannerUtils.showError("Lỗi", "Không thể mở cửa sổ chỉnh sửa!");
                                    e.printStackTrace();
                                }
                            });

                            deleteIcon.setOnMouseClicked(event -> {
                                Room room = getTableView().getItems().get(getIndex());
                                System.out.println("Delete icon clicked for room: " + room.getRoomNumber());
                                if (ScannerUtils.showConfirm("Xác nhận xóa", "Phòng học: " + room.getRoomNumber())) {
                                    boolean success = managerRoomRepository.delete(room.getId());
                                    if (success) {
                                        ScannerUtils.showInfo("Thông báo", "Xóa phòng thành công!");
                                        loadPage(pagination.getCurrentPageIndex());
                                    } else {
                                        ScannerUtils.showError("Thông báo", "Xóa phòng không thành công!");
                                    }
                                }
                            });

                            actionBox.setAlignment(Pos.CENTER);
                            if (canEdit) {
                                actionBox.getChildren().add(editWrapper);
                            }
                            if (canDelete) {
                                actionBox.getChildren().add(deleteIcon);
                            }
                        }

                        @Override
                        protected void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                                setGraphic(null);
                                System.out.println("Table cell is empty, no icons displayed.");
                            } else {
                                Room room = getTableView().getItems().get(getIndex());
                                setGraphic(actionBox);
                                if (canEdit) {
                                    System.out.println("Adding edit icon for room: " + room.getRoomNumber());
                                } else {
                                    System.out.println("Edit icon not added due to lack of edit permission.");
                                }
                                if (canDelete) {
                                    System.out.println("Adding delete icon for room: " + room.getRoomNumber());
                                } else {
                                    System.out.println("Delete icon not added due to lack of delete permission.");
                                }
                                System.out.println("Displaying action icons for room: " + room.getRoomNumber());
                            }
                        }
                    };
                }
            });
            tblRooms.setItems(FXCollections.observableArrayList(rooms));
            tblRooms.setSelectionModel(null);
            System.out.println("Loaded " + rooms.size() + " rooms into TableView.");
        } else {
            System.out.println("One or more TableView components are null. Check FXML file.");
        }
    }
}