package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Device;
import model.DeviceStatus;
import model.User;
import repository.DashboardRepository;
import repository.ManagerUserRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class InfoDeviceController {
    @FXML
    private TableView<Device> deviceTable;
    @FXML
    private TableColumn<Device, String> deviceIdColumn;
    @FXML
    private TableColumn<Device, String> deviceNameColumn;
    @FXML
    private TableColumn<Device, String> deviceTypeColumn;
    @FXML
    private TableColumn<Device, String> purchaseDateColumn;
    @FXML
    private TableColumn<Device, String> supplierColumn;
    @FXML
    private TableColumn<Device, BigDecimal> priceColumn;
    @FXML
    private TableColumn<Device, String> statusColumn;
    @FXML
    private TableColumn<Device, String> roomColumn;
    @FXML
    private TableColumn<Device, Integer> quantityColumn;
    @FXML
    private TableColumn<Device, Integer> availableQuantityColumn;
    @FXML
    private TableColumn<Device, String> maintainedByNameColumn;
    @FXML
    private ComboBox<String> maintainerComboBox;

    private DashboardRepository dashboardRepository = new DashboardRepository();
    private ManagerUserRepository userRepository = new ManagerUserRepository();
    private String userId;
    private Device selectedDevice;
    private ManagerMaintenanceController parentController;

    public void setParentController(ManagerMaintenanceController parentController) {
        this.parentController = parentController;
    }

    public void setUserId(String userId) {
        this.userId = userId;
        initializeTable();
    }

    private void initializeTable() {
        try {
            // Cấu hình cột
            deviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            deviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
            deviceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
            purchaseDateColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getPurchaseDate() != null ? cellData.getValue().getPurchaseDate().toString() : "N/A"));
            supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));
            priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
            statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : "N/A"));
            roomColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getRoom() != null ? cellData.getValue().getRoom().getRoomNumber() : "N/A"));
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            availableQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
            maintainedByNameColumn.setCellValueFactory(cellData -> {
                String maintainedBy = cellData.getValue().getMaintainedBy();
                return new ReadOnlyStringWrapper(maintainedBy != null ? getUserFullname(maintainedBy) : "Chưa có");
            });

            // Lấy danh sách thiết bị khả dụng
            List<Device> devices = dashboardRepository.getAvailableDevices();
            System.out.println("Số thiết bị khả dụng: " + (devices != null ? devices.size() : 0));
            for (Device device : devices) {
                System.out.println("Thiết bị: " + device.getDeviceName() + ", Trạng thái: " + device.getStatus());
            }
            deviceTable.setItems(FXCollections.observableArrayList(devices));

            // Lắng nghe sự kiện chọn hàng
            deviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                selectedDevice = newSelection;
                System.out.println("Đã chọn thiết bị: " + (selectedDevice != null ? selectedDevice.getDeviceName() : "null") +
                        ", Trạng thái: " + (selectedDevice != null && selectedDevice.getStatus() != null ? selectedDevice.getStatus().toString() : "null"));
            });

            // Tải danh sách người bảo trì
            loadMaintainers();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tải dữ liệu thiết bị: " + e.getMessage());
        }
    }

    private void loadMaintainers() {
        System.out.println("Đang tải danh sách người bảo trì với role_id=6...");
        try {
            List<User> maintainers = userRepository.getUsersByRole(6);
            if (maintainers == null || maintainers.isEmpty()) {
                System.err.println("Không tìm thấy người bảo trì với role_id=6");
                maintainerComboBox.setItems(FXCollections.observableArrayList("Không có người bảo trì"));
                return;
            }
            ObservableList<String> maintainerNames = FXCollections.observableArrayList();
            for (User user : maintainers) {
                if (user != null && user.getFullname() != null && user.getUserId() != null) {
                    maintainerNames.add(user.getFullname() + " (" + user.getUserId() + ")");
                }
            }
            maintainerComboBox.setItems(maintainerNames);
            System.out.println("Đã tải " + maintainerNames.size() + " người bảo trì");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi SQL khi tải danh sách người bảo trì: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi không xác định khi tải danh sách người bảo trì: " + e.getMessage());
        }
    }

    private String getUserFullname(String userId) {
        System.out.println("Đang lấy thông tin người dùng với userId: " + userId);
        try {
            if (userId == null) {
                System.err.println("userId là null");
                return "Không xác định";
            }
            User user = userRepository.getUserById(userId);
            if (user == null) {
                System.err.println("Không tìm thấy người dùng với userId: " + userId);
                return "Không xác định";
            }
            return user.getFullname();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi SQL khi lấy thông tin người dùng: " + e.getMessage());
            return "Không xác định";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Lỗi không xác định khi lấy thông tin người dùng: " + e.getMessage());
            return "Không xác định";
        }
    }

    @FXML
    private void confirmMaintenance() {
        System.out.println("Nhấn nút Xác nhận cho thiết bị");
        System.out.println("Thiết bị được chọn: " + (selectedDevice != null ? selectedDevice.getDeviceName() : "null") +
                ", Trạng thái: " + (selectedDevice != null && selectedDevice.getStatus() != null ? selectedDevice.getStatus().toString() : "null"));

        if (selectedDevice != null) {
            String statusString = selectedDevice.getStatus() != null ? selectedDevice.getStatus().toString() : "null";
            System.out.println("Trạng thái thực tế: " + statusString);

            if ("AVAILABLE".equalsIgnoreCase(statusString) || "Có sẵn".equalsIgnoreCase(statusString)) { // Hỗ trợ cả "Có sẵn"
                String selectedMaintainer = maintainerComboBox.getValue();
                if (selectedMaintainer != null && !selectedMaintainer.isEmpty()) {
                    String selectedUserId = selectedMaintainer.substring(selectedMaintainer.indexOf("(") + 1, selectedMaintainer.indexOf(")"));
                    try {
                        System.out.println("Cập nhật thiết bị: " + selectedDevice.getDeviceName() + " từ " + statusString + " sang UNDER_MAINTENANCE với người bảo trì: " + selectedUserId);
                        selectedDevice.setStatus(DeviceStatus.UNDER_MAINTENANCE);
                        selectedDevice.setMaintainedBy(selectedUserId);

                        String statusToSend = selectedDevice.getStatus().toString();
                        System.out.println("Status value being sent: '" + statusToSend + "'");
                        System.out.println("Status length: " + statusToSend.length());

                        dashboardRepository.updateDeviceStatus(selectedDevice);
                        System.out.println("Cập nhật trạng thái thành công, kiểm tra database...");

                        // Làm mới dữ liệu ở controller cha
                        if (parentController != null) {
                            System.out.println("Đang làm mới dữ liệu bảo trì...");
                            parentController.loadMaintenanceData();
                        } else {
                            System.out.println("Parent controller chưa được set, bỏ qua việc làm mới dữ liệu");
                        }

                        // Đóng dialog
                        Stage stage = (Stage) deviceTable.getScene().getWindow();
                        stage.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.err.println("Lỗi khi cập nhật trạng thái thiết bị: " + e.getMessage());
                        showErrorDialog("Lỗi", "Không thể cập nhật trạng thái thiết bị: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        System.err.println("Giá trị trạng thái không hợp lệ: " + e.getMessage());
                        showErrorDialog("Lỗi", "Giá trị trạng thái không hợp lệ: " + e.getMessage());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Lỗi không xác định: " + e.getMessage());
                        showErrorDialog("Lỗi", "Đã xảy ra lỗi không xác định: " + e.getMessage());
                    }
                } else {
                    System.out.println("Vui lòng chọn người bảo trì");
                    showErrorDialog("Lỗi", "Vui lòng chọn người bảo trì trước khi xác nhận!");
                }
            } else {
                System.out.println("Thiết bị không ở trạng thái khả dụng, trạng thái hiện tại: " + statusString);
                showErrorDialog("Lỗi", "Thiết bị phải ở trạng thái AVAILABLE hoặc Có sẵn để bảo trì!");
            }
        } else {
            System.out.println("Không có thiết bị nào được chọn");
            showErrorDialog("Lỗi", "Vui lòng chọn một thiết bị để bảo trì!");
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}