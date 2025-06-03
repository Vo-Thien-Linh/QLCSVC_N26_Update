package controller;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import model.UserSession;
import repository.PermissionRepository;
import view.*;

import java.net.URL;
import java.util.*;

import javafx.fxml.Initializable;

public class PageManagerController implements Initializable {
    @FXML
    private BorderPane root;

    @FXML
    private StackPane contentPane;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnDevice;

    @FXML
    private Button btnRoom;

    @FXML
    private Button btnMaintenance;

    @FXML
    private Button btnUser;

    @FXML
    private Button btnGroupPermission;

    @FXML
    private Button btnPermission;

    @FXML
    private Button btnStatistical;

    @FXML
    private Button btnSetting;

    @FXML
    private Button btnBorrowEquipment;

    @FXML
    private Button btnBorrowClassroom;

    @FXML
    private ImageView imgAvatar;

    @FXML
    private HBox userBox;



    private List<Button> navButtons = new ArrayList<>();
    private PermissionRepository permissionRepository = new PermissionRepository();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Add all buttons to navButtons list
        navButtons.add(btnDashboard);
        navButtons.add(btnDevice);
        navButtons.add(btnRoom);
        navButtons.add(btnMaintenance);
        navButtons.add(btnUser);
        navButtons.add(btnGroupPermission);
        navButtons.add(btnPermission);
        navButtons.add(btnStatistical);
        navButtons.add(btnSetting);
        navButtons.add(btnBorrowEquipment);
        navButtons.add(btnBorrowClassroom);


        Map<Button, String> buttonFunctionMap = new LinkedHashMap<>();
        buttonFunctionMap.put(btnDashboard, "Tổng quản");
        buttonFunctionMap.put(btnDevice, "Quản lý thiết bị");
        buttonFunctionMap.put(btnRoom, "Quản lý phòng");
        buttonFunctionMap.put(btnUser, "Quản lý người dùng");
        buttonFunctionMap.put(btnGroupPermission, "Nhóm quyền");
        buttonFunctionMap.put(btnStatistical, "Thống kê");
        buttonFunctionMap.put(btnBorrowEquipment, "Mượn thiết bị");
        buttonFunctionMap.put(btnBorrowClassroom, "Mượn thiết bị");

        // Phân quyền hiển thị button
        int roleId = UserSession.getRoleId();
        for (Map.Entry<Button, String> entry : buttonFunctionMap.entrySet()) {
            boolean canView = permissionRepository.isAllowed(roleId, entry.getValue(), "Xem");
            entry.getKey().setManaged(canView);
            entry.getKey().setVisible(canView);
        }

        boolean canPermission = permissionRepository.isAllowed(roleId, "Nhóm quyền", "Phân quyền");
        btnPermission.setManaged(canPermission);
        btnPermission.setVisible(canPermission);

        // Set up button actions
        btnDashboard.setOnAction(e -> {
            switchView(DashboardView.getView());
            highlightButton(btnDashboard);
        });

        btnDevice.setOnAction(e -> {
            switchView(DeviceView.getView());
            highlightButton(btnDevice);
        });

        btnRoom.setOnAction(e -> {
            switchView(RoomManagerView.getView());
            highlightButton(btnRoom);
        });

        btnMaintenance.setOnAction(e -> {
            switchView(new Label("🔧 Đây là Quản lý bảo trì"));
            highlightButton(btnMaintenance);
        });

        btnMaintenance.setOnAction(e -> {
            switchView(new Label("🔧 Đây là Quản lý bảo trì"));
            highlightButton(btnMaintenance);
        });

        btnUser.setOnAction(e -> {
            switchView(UserView.getView());
            highlightButton(btnUser);
        });

        btnGroupPermission.setOnAction(e -> {
            switchView(RoleView.getView());
            highlightButton(btnGroupPermission);
        });

        btnPermission.setOnAction(e -> {
            switchView(PermissionView.getView());
            highlightButton(btnPermission);
        });

        btnStatistical.setOnAction(e -> {
            switchView(StatisticalView.getView());
            highlightButton(btnStatistical);
        });

        btnSetting.setOnAction(e -> {
            switchView(new Label("⚙️ Đây là Cài đặt"));
            highlightButton(btnSetting);
        });

        btnBorrowEquipment.setOnAction(e -> {
            switchView(new Label("⚙️ Đây là mượn thiết bị"));
            highlightButton(btnBorrowEquipment);
        });

        btnBorrowClassroom.setOnAction(e -> {
            switchView(BorrowRoomView.getView());
            highlightButton(btnBorrowClassroom);
        });

        // Initialize with dashboard view
        for (Map.Entry<Button, String> entry : buttonFunctionMap.entrySet()) {
            System.out.println(entry.getValue());
            boolean canView = permissionRepository.isAllowed(roleId, entry.getValue(), "Xem");
            if (canView) {
                switchView(getViewByFunction(entry.getValue()));
                highlightButton(entry.getKey());
                break;
            }
        }

        Circle clip = new Circle(22.5, 22.5, 22.5);
        imgAvatar.setClip(clip);

        dropdownUser();
    }

    private Node getViewByFunction(String functionName) {
        return switch (functionName) {
            case "Tổng quản" -> DashboardView.getView();
            case "Quản lý thiết bị" -> DeviceView.getView();
            case "Quản lý phòng" -> RoomManagerView.getView();
            case "Quản lý người dùng" -> UserView.getView();
            case "Nhóm quyền" -> PermissionView.getView();
            case "Thống kê" -> new Label("📊 Đây là Thống kê");
            case "Mượn thiết bị" -> new Label("📦 Đây là mượn thiết bị");
            case "Mượn phòng" -> new Label("🏫 Đây là mượn phòng");
            case "Cài đặt" -> new Label("⚙️ Đây là Cài đặt");
            default -> new Label("❓ Chức năng không tồn tại");
        };
    }

    private SVGPath createIcon(String pathData, Color color) {
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("manager_dropdown-icon");
        icon.setContent(pathData);
        icon.setFill(color);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    private void dropdownUser(){
        ContextMenu contextMenu = new ContextMenu();

        SVGPath userIcon = createIcon("M12 12c2.7 0 4.9-2.2 4.9-4.9S14.7 2.2 12 2.2 7.1 4.4 7.1 7.1 9.3 12 12 12zm0 2.2c-3.2 0-9.6 1.6-9.6 4.9v2.2h19.2v-2.2c0-3.3-6.4-4.9-9.6-4.9z", Color.BLACK);
        MenuItem profileItem = new MenuItem("Thông tin cá nhân");
        profileItem.setGraphic(userIcon);
        profileItem.getStyleClass().add("manager_menu-item");

        SVGPath settingsIcon = createIcon("M19.43 12.98c.04-.32.07-.66.07-1s-.03-.68-.07-1l2.11-1.65a.5.5 0 0 0 .12-.66l-2-3.46a.5.5 0 0 0-.61-.22l-2.49 1a7.04 7.04 0 0 0-1.5-.88l-.38-2.65A.5.5 0 0 0 14 2h-4a.5.5 0 0 0-.5.43l-.38 2.65a7.04 7.04 0 0 0-1.5.88l-2.49-1a.5.5 0 0 0-.61.22l-2 3.46a.5.5 0 0 0 .12.66L4.57 11c-.04.32-.07.66-.07 1s.03.68.07 1l-2.11 1.65a.5.5 0 0 0-.12.66l2 3.46a.5.5 0 0 0 .61.22l2.49-1c.46.36.96.66 1.5.88l.38 2.65A.5.5 0 0 0 10 22h4a.5.5 0 0 0 .5-.43l.38-2.65c.54-.22 1.04-.52 1.5-.88l2.49 1a.5.5 0 0 0 .61-.22l2-3.46a.5.5 0 0 0-.12-.66L19.43 12.98z", Color.BLACK);
        MenuItem settingsItem = new MenuItem("Cài đặt");
        settingsItem.setGraphic(settingsIcon);
        settingsItem.getStyleClass().add("manager_menu-item");

        SVGPath logoutIcon = createIcon("M16 13v-2H7V8l-5 4 5 4v-3h9z M20 3H10v2h10v14H10v2h10c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z", Color.BLACK);
        MenuItem logoutItem = new MenuItem("Đăng xuất");
        logoutItem.setGraphic(logoutIcon);
        logoutItem.getStyleClass().add("manager_menu-item");


        contextMenu.getItems().addAll(profileItem, settingsItem, logoutItem);

        // Sự kiện click hiện menu
        userBox.setOnMouseClicked(event -> {
            contextMenu.show(userBox, Side.BOTTOM, 80, 0);
        });

    }

    private void switchView(Node view) {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(view);
    }

    private void highlightButton(Button activeButton) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("active");
        }
        activeButton.getStyleClass().add("active");
    }
}