package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Room;
import model.RoomStatus;
import repository.ManagerRoomRepository;
import utils.ScannerUtils;

import java.io.IOException;
import java.util.ArrayList;

public class EditRoomController {

    @FXML private TextField txtRoomNumber;
    @FXML private TextField txtSeatingCapacity;
    @FXML private TextField txtLocation;
    @FXML private ComboBox<String> cboRoomType;
    @FXML private RadioButton rbAvailable;
    @FXML private RadioButton rbOccupied;
    @FXML private RadioButton rbMaintenance;
    @FXML private Button btnUpdate;

    private Room room;

    @FXML
    private void handleEdit(ActionEvent event) {
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

        Room updatedRoom = new Room(room.getId(), status, roomNumber, seatingCapacity, roomType, location);
        boolean success = ManagerRoomRepository.edit(updatedRoom);
        if (success) {
            ScannerUtils.showInfo("Thông báo", "Phòng đã cập nhật thành công!");
            Stage currentStage = (Stage) btnUpdate.getScene().getWindow();
            currentStage.close();
        } else {
            ScannerUtils.showError("Thông báo", "Cập nhật phòng không thành công!");
        }
    }

    public void setRoom(Room room) {
        this.room = room;
        loadRoomInfo();
    }

    private void loadRoomInfo() {
        try {
            Room data = ManagerRoomRepository.DataRoom(room.getId());
            txtRoomNumber.setText(data.getRoomNumber());
            txtSeatingCapacity.setText(String.valueOf(data.getSeatingCapacity()));
            txtLocation.setText(data.getLocation());
            cboRoomType.getSelectionModel().select(data.getRoom_type());
            RoomStatus status = data.getStatus();
            switch (status) {
                case AVAILABLE -> rbAvailable.setSelected(true);
                case OCCUPIED -> rbOccupied.setSelected(true);
                case MAINTENANCE -> rbMaintenance.setSelected(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showEditRoomWindow(Room room, Runnable onClose) throws IOException {
        FXMLLoader loader = new FXMLLoader(EditRoomController.class.getResource("/fxml/room/EditRoom.fxml"));
        Parent editRoot = loader.load();
        EditRoomController controller = loader.getController();
        controller.setRoom(room);
        Stage editStage = new Stage();
        editStage.setTitle("Chỉnh sửa phòng học");
        editStage.setScene(new Scene(editRoot));
        editStage.setOnHidden(e -> onClose.run());
        editStage.show();
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