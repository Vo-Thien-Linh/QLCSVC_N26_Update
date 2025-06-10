package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Room;
import model.RoomStatus;
import repository.ManagerRoomRepository;
import utils.ScannerUtils;

import java.util.ArrayList;
import java.util.UUID;

public class CreateRoomController {

    @FXML private TextField txtRoomNumber;
    @FXML private TextField txtSeatingCapacity;
    @FXML private TextField txtLocation;
    @FXML private ComboBox<String> cboRoomType;
    @FXML private RadioButton rbAvailable;
    @FXML private RadioButton rbOccupied;
    @FXML private RadioButton rbMaintenance;
    @FXML private Button btnSave;

    private Runnable onRoomAdded;

    public void setOnRoomAdded(Runnable onRoomAdded) {
        this.onRoomAdded = onRoomAdded;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String roomNumber = txtRoomNumber.getText().trim();
        String seatingCapacityText = txtSeatingCapacity.getText().trim();
        String location = txtLocation.getText().trim();
        String roomType = cboRoomType.getValue();
        RoomStatus status = null;

        if (rbAvailable.isSelected()) {
            status = RoomStatus.AVAILABLE;
        } else if (rbOccupied.isSelected()) {
            status = RoomStatus.OCCUPIED;
        } else if (rbMaintenance.isSelected()) {
            status = RoomStatus.MAINTENANCE;
        }

        if (roomNumber.isEmpty() || seatingCapacityText.isEmpty() || location.isEmpty() || roomType == null || status == null) {
            ScannerUtils.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        int seatingCapacity;
        try {
            seatingCapacity = Integer.parseInt(seatingCapacityText);
        } catch (NumberFormatException e) {
            ScannerUtils.showError("Lỗi", "Sức chứa không hợp lệ!");
            return;
        }

        String roomId = UUID.randomUUID().toString().substring(0, 8);
        Room room = new Room(roomId, status, roomNumber, seatingCapacity, roomType, location);
        boolean success = ManagerRoomRepository.create(room);
        if (success) {
            ScannerUtils.showInfo("Thông báo", "Phòng đã thêm thành công!");
            if (onRoomAdded != null) {
                onRoomAdded.run();
            }
            Stage currentStage = (Stage) btnSave.getScene().getWindow();
            currentStage.close();
        } else {
            ScannerUtils.showError("Thông báo", "Thêm phòng không thành công!");
        }
    }

    @FXML
    public void initialize() {
        loadRoomTypeData();
    }

    private void loadRoomTypeData() {
        if (cboRoomType != null) {
            ArrayList<String> roomTypes = ManagerRoomRepository.getAllRoomTypeNames();
            cboRoomType.setItems(FXCollections.observableArrayList(roomTypes));
            if (!cboRoomType.getItems().isEmpty()) {
                cboRoomType.getSelectionModel().selectFirst();
            }
        }
    }
}