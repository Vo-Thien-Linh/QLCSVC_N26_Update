package controller;

import javafx.animation.ScaleTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import model.Device;
import model.Role;
import model.UserSession;
import repository.PermissionRepository;
import repository.RoleRepository;
import utils.ScannerUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

public class RoleController implements Initializable {

    @FXML private VBox rootPane;
    @FXML private TableView<Role> tbGroupPermission;
    @FXML private TableColumn<Role, Integer> colRoleId;
    @FXML private TableColumn<Role, String>  colRoleName;
    @FXML private TableColumn<Role, Void> colActions;

    private PermissionRepository permissionRepository = new PermissionRepository();
    private RoleRepository roleRepository = new RoleRepository();
    private int roleId = UserSession.getRoleId();
    private boolean canCreate = permissionRepository.isAllowed(roleId, "Nhóm quyền", "Thêm mới");
    private boolean canEdit = permissionRepository.isAllowed(roleId, "Nhóm quyền", "Chỉnh sửa");
    private boolean canDelete = permissionRepository.isAllowed(roleId, "Nhóm quyền", "Xóa");

    @FXML
    private void handleAddNewAction(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role/create.fxml"));
            Parent root = loader.load();

            CreateRoleController controller = loader.getController();
            controller.setOnRoleAdded(() -> {
                List<Role> roles = roleRepository.getAllRoles();
                loadRoleData(roles);
            });

            Stage addNewStage = new Stage();
            addNewStage.setTitle("Thêm nhóm quyền mới");
            addNewStage.setScene(new Scene(root));
            addNewStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        setupButton();
        List<Role> roles = roleRepository.getAllRoles();
        loadRoleData(roles);
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

    public void loadRoleData(List<Role> roles) {
        if(colRoleId != null && colRoleName != null && tbGroupPermission != null) {
            colRoleId.setCellValueFactory(new PropertyValueFactory<>("roleId"));
            colRoleName.setCellValueFactory(new PropertyValueFactory<>("roleName"));
            colActions.setCellFactory(new Callback<TableColumn<Role, Void>, TableCell<Role, Void>>() {
                @Override
                public TableCell<Role, Void> call(final TableColumn<Role, Void> param) {
                    return new TableCell<Role, Void>() {
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
                                Role role = getTableView().getItems().get(getIndex());

                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/role/edit.fxml"));
                                    Parent editRoot = loader.load();

                                    EditRoleController controller = loader.getController();
                                    controller.setRole(role);
                                    controller.setOnRoleAdded(() -> {
                                        List<Role> roles = roleRepository.getAllRoles();
                                        loadRoleData(roles);
                                    });

                                    Stage editStage = new Stage();
                                    editStage.setTitle("Chỉnh sửa thiết bị");
                                    editStage.setScene(new Scene(editRoot));
                                    editStage.show();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });


                            deleteIcon.setOnMouseClicked(event -> {
                                Role role = getTableView().getItems().get(getIndex());

                                if(ScannerUtils.showConfirm("Xác nhận xóa", "Nhóm quyền: " + role.getRoleName())){
                                    boolean success = roleRepository.delete(role.getRoleId());
                                    if(success) {
                                        ScannerUtils.showInfo("Thông báo", "Xóa nhóm quyền thành công!");
                                        List<Role> roles = roleRepository.getAllRoles();
                                        loadRoleData(roles);
                                    } else {
                                        ScannerUtils.showError("Thông báo", "Xóa nhóm quyền không thành công!");
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
            tbGroupPermission.setItems(FXCollections.observableArrayList(roles));
            tbGroupPermission.setSelectionModel(null);
        }
    }
}
