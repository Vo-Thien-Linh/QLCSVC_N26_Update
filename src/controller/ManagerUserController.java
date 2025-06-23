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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import model.*;
import repository.ManagerUserRepository;
import repository.PermissionRepository;
import utils.ScannerUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ManagerUserController implements Initializable {
    @FXML private VBox rootPane;
    @FXML private TableView<User> tblUsers;
    @FXML private ComboBox<String> cboSearchType, cboChangeStatus;
    @FXML private TextField txtSearch;
    @FXML private StackPane btnSearch;
    @FXML private Pagination pagination;
    @FXML private StackPane btnApply;
    @FXML private StackPane btnAddNew;
    @FXML private TableColumn<User, Void> colActions;
    @FXML private TableColumn<User, Boolean> colCheck;
    @FXML private TableColumn<User, String> colId, colImage, colFullname, colUsername, colYearold, colEmail, colPhoneNumber, colStatus, colRole, colUnit;

    private PermissionRepository permissionRepository = new PermissionRepository();
    private ManagerUserRepository managerUserRepository = new ManagerUserRepository();
    private String status = null;
    private String keyword = null;
    private int limitItem = 7;

    private int roleId = UserSession.getRoleId();
    private boolean canCreate = permissionRepository.isAllowed(roleId, "Quản lý người dùng", "Thêm mới");
    private boolean canEdit = permissionRepository.isAllowed(roleId, "Quản lý người dùng", "Chỉnh sửa");
    private boolean canDelete = permissionRepository.isAllowed(roleId, "Quản lý người dùng", "Xóa");

    @FXML
    private void handleAddNewAction(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/create.fxml"));
            Parent root = loader.load();

            CreateUserController controller = loader.getController();
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
        formatHeaderLabel();
        cboSearchType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateStatus(newVal);
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

        btnApply.setOnMouseClicked(event -> {
            ObservableList<User> users = tblUsers.getItems();
            List<String> ids = users.stream().filter(User::isSelected).map(User::getUserId).collect(Collectors.toList());
            if (ids.isEmpty()) {
                ScannerUtils.showError("Lỗi", "Vui lòng chọn ít nhất một người dùng!");
                return;
            }

            String status = cboChangeStatus.getSelectionModel().getSelectedItem();
            for(Status userStatus : Status.values()) {
                if(userStatus.toString().equals(status)) {
                    status = userStatus.name();
                    break;
                }
            }
            Boolean success = managerUserRepository.changeStatus(ids, status);
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

    private void formatHeaderLabel(){
        Label username = new Label("Tên đăng\nnhập");
        username.setWrapText(true);
        username.setMinHeight(50);
        username.setAlignment(Pos.CENTER);
        username.setMaxWidth(Double.MAX_VALUE);
        colUsername.setGraphic(username);

        Label date = new Label("Ngày tháng\nnăm sinh");
        date.setWrapText(true);
        date.setAlignment(Pos.CENTER);
        date.setMaxWidth(Double.MAX_VALUE);
        colYearold.setGraphic(date);

        Label unit = new Label("Lớp / Phòng\nban");
        unit.setWrapText(true);
        unit.setAlignment(Pos.CENTER);
        unit.setMaxWidth(Double.MAX_VALUE);
        colUnit.setGraphic(unit);
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
        for (Status userStatus : Status.values()) {
            if (userStatus.toString().equals(newValue)) {
                status = userStatus.name();
                break;
            }
        }
    }

    private void loadPage(int pageIndex) {
        int skip = pageIndex * limitItem;
        List<User> users = managerUserRepository.filterAndSearch(status, keyword, limitItem, skip);
        int totalPages = (int) Math.ceil((double) managerUserRepository.countDevices(status, keyword) / limitItem);
        pagination.setPageCount(totalPages);
        loadUserData(users);
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
            for (Status status : Status.values()) {
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

public void loadUserData(List<User> users) {
        if(colId != null && colImage != null && colFullname != null && colUsername != null && colYearold != null && colEmail != null && colPhoneNumber != null && colRole != null && colStatus != null && tblUsers != null) {
            colCheck.setCellFactory(tc -> new TableCell<User, Boolean>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOnAction(e -> {
                        User user = getTableView().getItems().get(getIndex());
                        user.setSelected(checkBox.isSelected());
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        User user = getTableView().getItems().get(getIndex());
                        checkBox.setSelected(user.isSelected());
                        setGraphic(checkBox);
                    }
                }
            });
            colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
            colImage.setCellValueFactory(new PropertyValueFactory<>("thumbnail"));

            colImage.setCellFactory(column -> new TableCell<User, String>() {
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
            colFullname.setCellValueFactory(new PropertyValueFactory<>("fullname"));
            colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
            colYearold.setCellValueFactory(new PropertyValueFactory<>("yearold"));
            colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            colEmail.setCellFactory(column -> {
                return new TableCell<User, String>() {
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
            colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
            colStatus.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));
            colStatus.setCellFactory(column -> {
                return new TableCell<User, String>() {
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
            colRole.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getRole().getRoleName().toString()));
            colUnit.setCellValueFactory(cellData -> {
                User user = cellData.getValue();
                String role = cellData.getValue().getRole().getRoleName();

                if ("Sinh Viên".equals(role)) {
                    return new ReadOnlyStringWrapper(user.getClasses());
                } else if ("Giáo viên".equals(role) || "Bảo trì".equals(role)) {
                    return new ReadOnlyStringWrapper(user.getDepartment());
                } else {
                    return new ReadOnlyStringWrapper("");
                }
            });
            colActions.setCellFactory(new Callback<TableColumn<User, Void>, TableCell<User, Void>>() {
                @Override
                public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                    return new TableCell<User, Void>() {
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

//                             Bắt sự kiện chỉnh sửa
                            iconWrapper.setOnMouseClicked(event -> {
                                User user = getTableView().getItems().get(getIndex());

                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/edit.fxml"));
                                    Parent editRoot = loader.load();

                                    EditUserController controller = loader.getController();

                                    controller.setUser(user);

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
                                User user = getTableView().getItems().get(getIndex());

                                if(ScannerUtils.showConfirm("Xác nhận xóa", "Người dùng: " + user.getFullname())){
                                    boolean success = managerUserRepository.delete(user.getUserId());
                                    if(success) {
                                        ScannerUtils.showInfo("Thông báo", "Xóa người dùng thành công!");
                                        loadPage(pagination.getCurrentPageIndex());
                                    } else {
                                        ScannerUtils.showError("Thông báo", "Xóa người dùng không thành công!");
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
            tblUsers.setItems(FXCollections.observableArrayList(users));
            tblUsers.setSelectionModel(null);
        }
    }
}
