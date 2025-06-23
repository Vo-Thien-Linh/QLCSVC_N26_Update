package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Device;
import model.Room;
import repository.DashboardRepository;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ManagerMaintenanceController {
    @FXML
    private TableView<Room> roomMaintenanceTable;
    @FXML
    private TableColumn<Room, String> roomIdColumn;
    @FXML
    private TableColumn<Room, String> roomNumberColumn;
    @FXML
    private TableColumn<Room, String> roomStatusColumn;
    @FXML
    private TableColumn<Room, String> roomMaintainedByColumn;

    @FXML
    private TableView<Device> deviceMaintenanceTable;
    @FXML
    private TableColumn<Device, String> deviceIdColumn;
    @FXML
    private TableColumn<Device, String> deviceNameColumn;
    @FXML
    private TableColumn<Device, String> deviceStatusColumn;
    @FXML
    private TableColumn<Device, String> deviceMaintainedByColumn;

    private DashboardRepository dashboardRepository = new DashboardRepository();
    private String currentUserId = "MTL0003"; // Giả định user_id của role bảo trì

    @FXML
    public void initialize() {
        // Cấu hình cột cho phòng
        roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomStatusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : "N/A"));
        roomMaintainedByColumn.setCellValueFactory(new PropertyValueFactory<>("maintainedBy"));

        // Cấu hình cột cho thiết bị
        deviceIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        deviceNameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        deviceStatusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : "N/A"));
        deviceMaintainedByColumn.setCellValueFactory(new PropertyValueFactory<>("maintainedBy"));

        loadMaintenanceData();
    }

    @FXML
    private void handleMaintainRoom() {
        System.out.println("Nhấn nút Bảo trì Phòng");
        try {
            String resourcePath = "/fxml/Maintenance/infoRoom.fxml"; // Điều chỉnh đường dẫn
            URL resource = getClass().getResource(resourcePath);
            System.out.println("Đang tải: " + resourcePath + ", Tồn tại: " + (resource != null));
            if (resource == null) {
                throw new IOException("Tài nguyên FXML không tìm thấy: " + resourcePath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            InfoRoomController controller = loader.getController();
            controller.setUserId(currentUserId);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Bảo trì Phòng");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
            System.out.println("Đã mở infoRoom.fxml thành công");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi mở infoRoom.fxml: " + e.getMessage());
            showErrorDialog("Lỗi", "Không thể mở cửa sổ Bảo trì Phòng: " + e.getMessage());
        }
    }

    @FXML
    private void handleMaintainDevice() {
        System.out.println("Nhấn nút Bảo trì Thiết bị");
        try {
            String resourcePath = "/fxml/Maintenance/infoDevice.fxml"; // Điều chỉnh đường dẫn
            URL resource = getClass().getResource(resourcePath);
            System.out.println("Đang tải: " + resourcePath + ", Tồn tại: " + (resource != null));
            if (resource == null) {
                throw new IOException("Tài nguyên FXML không tìm thấy: " + resourcePath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            InfoDeviceController controller = loader.getController();
            controller.setUserId(currentUserId);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Bảo trì Thiết bị");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
            System.out.println("Đã mở infoDevice.fxml thành công");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi mở infoDevice.fxml: " + e.getMessage());
            showErrorDialog("Lỗi", "Không thể mở cửa sổ Bảo trì Thiết bị: " + e.getMessage());
        }
    }

    void loadMaintenanceData() {
        System.out.println("Đang tải dữ liệu bảo trì...");
        List<Room> maintenanceRooms = dashboardRepository.getMaintenanceRooms();
        List<Device> maintenanceDevices = dashboardRepository.getMaintenanceDevices();

        System.out.println("Số phòng đang bảo trì: " + (maintenanceRooms != null ? maintenanceRooms.size() : 0));
        System.out.println("Số thiết bị đang bảo trì: " + (maintenanceDevices != null ? maintenanceDevices.size() : 0));
        if (maintenanceRooms != null) {
            for (Room room : maintenanceRooms) {
                try {
                    String roomNumber = (room != null && room.getRoomNumber() != null) ? room.getRoomNumber() : "Không xác định";
                    String status = (room != null && room.getStatus() != null) ? room.getStatus().toString() : "Không xác định";
                    System.out.println("Phòng: " + roomNumber + ", Trạng thái: " + status);
                } catch (Exception e) {
                    System.err.println("Lỗi khi xử lý phòng: " + e.getMessage());
                }
            }
        }
        if (maintenanceDevices != null) {
            for (Device device : maintenanceDevices) {
                try {
                    String deviceName = (device != null && device.getDeviceName() != null) ? device.getDeviceName() : "Không xác định";
                    String status = (device != null && device.getStatus() != null) ? device.getStatus().toString() : "Không xác định";
                    System.out.println("Thiết bị: " + deviceName + ", Trạng thái: " + status);
                } catch (Exception e) {
                    System.err.println("Lỗi khi xử lý thiết bị: " + e.getMessage());
                }
            }
        }

        if (roomMaintenanceTable != null) {
            roomMaintenanceTable.setItems(FXCollections.observableArrayList(maintenanceRooms != null ? maintenanceRooms : new ArrayList<>()));
        }
        if (deviceMaintenanceTable != null) {
            deviceMaintenanceTable.setItems(FXCollections.observableArrayList(maintenanceDevices != null ? maintenanceDevices : new ArrayList<>()));
        }
        System.out.println("Đã tải " + (maintenanceRooms != null ? maintenanceRooms.size() : 0) + " phòng và " + (maintenanceDevices != null ? maintenanceDevices.size() : 0) + " thiết bị vào bảng");
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}