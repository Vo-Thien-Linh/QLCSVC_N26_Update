package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Room;
import model.RoomStatus;
import model.User;
import repository.DashboardRepository;
import repository.ManagerUserRepository;

import java.sql.SQLException;
import java.util.List;

public class InfoRoomController {
    @FXML
    private TableView<Room> roomTable;
    @FXML
    private TableColumn<Room, String> roomIdColumn;
    @FXML
    private TableColumn<Room, String> roomNumberColumn;
    @FXML
    private TableColumn<Room, String> statusColumn;
    @FXML
    private TableColumn<Room, Integer> seatingCapacityColumn;
    @FXML
    private TableColumn<Room, String> roomTypeColumn;
    @FXML
    private TableColumn<Room, String> locationColumn;
    @FXML
    private TableColumn<Room, String> maintainedByNameColumn;
    @FXML
    private ComboBox<String> maintainerComboBox;

    private DashboardRepository dashboardRepository = new DashboardRepository();
    private ManagerUserRepository userRepository = new ManagerUserRepository();
    private String userId;
    private Room selectedRoom;
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
            roomIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
            statusColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : "N/A"));
            seatingCapacityColumn.setCellValueFactory(new PropertyValueFactory<>("seatingCapacity"));
            roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("room_type"));
            locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
            maintainedByNameColumn.setCellValueFactory(cellData -> {
                String maintainedBy = cellData.getValue().getMaintainedBy();
                return new ReadOnlyStringWrapper(maintainedBy != null ? getUserFullname(maintainedBy) : "Chưa có");
            });

            // Lấy danh sách phòng khả dụng
            List<Room> rooms = dashboardRepository.getAvailableRooms();
            System.out.println("Số phòng khả dụng: " + (rooms != null ? rooms.size() : 0));
            for (Room room : rooms) {
                System.out.println("Phòng: " + room.getRoomNumber() + ", Trạng thái: " + room.getStatus());
            }
            roomTable.setItems(FXCollections.observableArrayList(rooms));

            // Lắng nghe sự kiện chọn hàng
            roomTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                selectedRoom = newSelection;
                System.out.println("Đã chọn phòng: " + (selectedRoom != null ? selectedRoom.getRoomNumber() : "null") +
                        ", Trạng thái: " + (selectedRoom != null && selectedRoom.getStatus() != null ? selectedRoom.getStatus().toString() : "null"));
            });

            // Tải danh sách người bảo trì
            loadMaintainers();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi tải dữ liệu phòng: " + e.getMessage());
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
        System.out.println("Nhấn nút Xác nhận cho phòng");
        System.out.println("Phòng được chọn: " + (selectedRoom != null ? selectedRoom.getRoomNumber() : "null") +
                ", Trạng thái: " + (selectedRoom != null && selectedRoom.getStatus() != null ? selectedRoom.getStatus().toString() : "null"));

        if (selectedRoom != null) {
            String statusString = selectedRoom.getStatus() != null ? selectedRoom.getStatus().toString() : "null";
            System.out.println("Trạng thái thực tế: " + statusString);

            if ("AVAILABLE".equalsIgnoreCase(statusString) || "Có sẵn".equalsIgnoreCase(statusString)) {
                String selectedMaintainer = maintainerComboBox.getValue();
                if (selectedMaintainer != null && !selectedMaintainer.isEmpty()) {
                    String selectedUserId = selectedMaintainer.substring(selectedMaintainer.indexOf("(") + 1, selectedMaintainer.indexOf(")"));
                    try {
                        System.out.println("Cập nhật phòng: " + selectedRoom.getRoomNumber() + " từ " + statusString + " sang MAINTENANCE với người bảo trì: " + selectedUserId);
                        selectedRoom.setStatus(RoomStatus.MAINTENANCE);
                        selectedRoom.setMaintainedBy(selectedUserId);

                        String statusToSend = selectedRoom.getStatus().name();
                        System.out.println("Status value being sent: '" + selectedRoom.getStatus() + "'");
                        System.out.println("Status length: " + selectedRoom.getStatus().toString().length());

                        dashboardRepository.updateRoomStatus(selectedRoom);
                        System.out.println("Cập nhật trạng thái thành công, kiểm tra database...");

                        // **FIX: Xử lý trường hợp parentController null**
                        if (parentController != null) {
                            System.out.println("Đang làm mới dữ liệu bảo trì...");
                            parentController.loadMaintenanceData();
                        } else {
                            System.out.println("Parent controller chưa được set, bỏ qua việc làm mới dữ liệu");
                        }
                        // Đóng dialog
                        Stage stage = (Stage) roomTable.getScene().getWindow();
                        stage.close();

                    } catch (SQLException e) {
                        e.printStackTrace();
                        System.err.println("Lỗi khi cập nhật trạng thái phòng: " + e.getMessage());
                        showErrorDialog("Lỗi", "Không thể cập nhật trạng thái phòng: " + e.getMessage());
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
                System.out.println("Phòng không ở trạng thái khả dụng, trạng thái hiện tại: " + statusString);
                showErrorDialog("Lỗi", "Phòng phải ở trạng thái khả dụng để bảo trì!");
            }
        } else {
            System.out.println("Không có phòng nào được chọn");
            showErrorDialog("Lỗi", "Vui lòng chọn một phòng để bảo trì!");
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