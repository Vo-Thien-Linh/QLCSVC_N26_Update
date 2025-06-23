package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import model.User;
import model.UserSession;
import repository.ManagerUserRepository;
import repository.PermissionRepository;
import view.*;

import java.io.IOException;
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
    private Button btnSchedule;

    @FXML
    private ImageView imgAvatar;

    @FXML
    private HBox userBox;

    @FXML
    private Label lblUsername;



    private List<Button> navButtons = new ArrayList<>();
    private PermissionRepository permissionRepository = new PermissionRepository();
    private ManagerUserRepository  managerUserRepository = new ManagerUserRepository();
    private User infoUser = managerUserRepository.getUser(UserSession.getUserId());

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblUsername.setText(infoUser.getFullname());
        imgAvatar.setImage(new Image(infoUser.getThumbnail(), true));

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
        navButtons.add(btnSchedule);


        Map<Button, String> buttonFunctionMap = new LinkedHashMap<>();
        buttonFunctionMap.put(btnDashboard, "Tổng quản");
        buttonFunctionMap.put(btnDevice, "Quản lý thiết bị");
        buttonFunctionMap.put(btnRoom, "Quản lý phòng");
        buttonFunctionMap.put(btnUser, "Quản lý người dùng");
        buttonFunctionMap.put(btnGroupPermission, "Nhóm quyền");
        buttonFunctionMap.put(btnStatistical, "Thống kê");
        buttonFunctionMap.put(btnBorrowEquipment, "Mượn thiết bị");
        buttonFunctionMap.put(btnBorrowClassroom, "Mượn thiết bị");
        buttonFunctionMap.put(btnSchedule, "Lịch");

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
            switchView(BorrowDeviceView.getView());
            highlightButton(btnBorrowEquipment);
        });

        btnBorrowClassroom.setOnAction(e -> {
            switchView(BorrowRoomView.getView());
            highlightButton(btnBorrowClassroom);
        });

        btnSchedule.setOnAction(e -> {
            switchView(RoomScheduleView.getView());
            highlightButton(btnSchedule);
        });

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
            case "Thống kê" -> StatisticalView.getView();
            case "Mượn thiết bị" -> BorrowDeviceView.getView();
            case "Mượn phòng" -> BorrowRoomView.getView();
            case "Lịch" -> RoomScheduleView.getView();
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
        profileItem.setOnAction(e -> {
            showUserDetailsDialog(infoUser);
        });

        SVGPath logoutIcon = createIcon("M16 13v-2H7V8l-5 4 5 4v-3h9z M20 3H10v2h10v14H10v2h10c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z", Color.BLACK);
        MenuItem logoutItem = new MenuItem("Đăng xuất");
        logoutItem.setGraphic(logoutIcon);
        logoutItem.getStyleClass().add("manager_menu-item");
        logoutItem.setOnAction(e -> handleLogout());


        contextMenu.getItems().addAll(profileItem, logoutItem);

        // Sự kiện click hiện menu
        userBox.setOnMouseClicked(event -> {
            contextMenu.show(userBox, Side.BOTTOM, 100, 0);
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
    public void loadContentBasedOnRole() {
        try {
            String contentPath = "";
            int roleId = UserSession.getRoleId();
            switch (roleId) {
                case 3: // Quản trị viên
                    contentPath = "/fxml/layout/PageManagerView.fxml"; // Tổng quan quản lý
                    break;
                case 5: // Giáo viên
                    contentPath = "/fxml/borrow-device/index.fxml.fxml"; // Mượn thiết bị
                    break;
                case 6: // Bảo trì
                    contentPath = "/fxml/statistical/index.fxml.fxml"; // Thống kê
                    break;
                default:
                    contentPath = "/fxml/login/index.fxml"; // Fallback
                    break;
            }
            if (getClass().getResource(contentPath) != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(contentPath));
                Pane content = loader.load();
                contentPane.getChildren().setAll(content);
            } else {
                System.err.println("File FXML không tồn tại: " + contentPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi tải nội dung: " + e.getMessage());
        }
    }
    private void handleLogout() {
        UserSession.clearSession();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login/index.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userBox.getScene().getWindow(); // Lấy Stage từ userBox
            stage.setScene(new Scene(root, 1350, 721));
            stage.setTitle("Đăng nhập");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUserDetailsDialog(User user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Thông tin người dùng");

        dialog.getDialogPane().setPrefWidth(400);
        dialog.getDialogPane().setPrefHeight(300);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label lblTitle = new Label("Thông tin người dùng");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label lblFullname = new Label("Họ tên: " + user.getFullname());
        Label lblEmail = new Label("Email: " + user.getEmail());
        Label lblPhone = new Label("SĐT: " + user.getPhoneNumber());
        Label lblRole = new Label("Vai trò: " + user.getRole());

        // Style chung cho label thông tin
        for (Label label : List.of(lblFullname, lblEmail, lblPhone, lblRole)) {
            label.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        }

        content.getChildren().addAll(lblTitle, lblFullname, lblEmail, lblPhone, lblRole);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        dialog.showAndWait();
    }


}