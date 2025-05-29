package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Device;
import model.DeviceStatus;
import model.Room;
import model.UserSession;
import repository.ManagerDeviceRepository;
import repository.PermissionRepository;
import utils.CloudinaryUploaderUtils;
import utils.ScannerUtils;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class CreateDeviceController {
    @FXML
    private TextField txtName;
    @FXML private TextField txtType;
    @FXML private TextField txtSupplier;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantity;
    @FXML private DatePicker datePickerImport;
    @FXML private RadioButton rbAvailble;
    @FXML private RadioButton rbUnavailble;
    @FXML private RadioButton rbMaintenance;
    @FXML private RadioButton rbBroken;
    @FXML private RadioButton rbAllow;
    @FXML private RadioButton rbNotAllow;
    @FXML private ImageView imagePreview;
    @FXML private File selectedImageFile;
    @FXML private ComboBox<Room> roomComboBox;
    @FXML private Button btnSave;

    private ManagerDeviceRepository managerDeviceRepository = new ManagerDeviceRepository();
    private Runnable onDeviceAdded;

    public void setOnDeviceAdded(Runnable onDeviceAdded) {
        this.onDeviceAdded = onDeviceAdded;
    }

    @FXML
    private void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh thiết bị");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ảnh", "*.jpg", "*.png", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            selectedImageFile = file;
            Image image = new Image(file.toURI().toString());
            imagePreview.setImage(image);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = txtName.getText().trim();
        String type = txtType.getText().trim();
        LocalDate purchaseDate = datePickerImport.getValue();
        String supplier = txtSupplier.getText().trim();
        String priceText = txtPrice.getText().trim();
        String quantityText = txtQuantity.getText().trim();
        Room selectedRoom = roomComboBox.getValue();

        DeviceStatus status = null;
        if (rbAvailble.isSelected()) {
            status = DeviceStatus.AVAILABLE;
        } else if (rbUnavailble.isSelected()) {
            status = DeviceStatus.UNAVAILABLE;
        } else if (rbMaintenance.isSelected()) {
            status = DeviceStatus.UNDER_MAINTENANCE;
        } else if (rbBroken.isSelected()) {
            status = DeviceStatus.BROKEN;
        }

        String imageUrl = null;
        if (selectedImageFile != null) {
            try {
                imageUrl = CloudinaryUploaderUtils.uploadImage(selectedImageFile);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else {
            ScannerUtils.showError("Lỗi", "Vui lòng thêm hình ảnh");
            return;
        }

        if (name.isEmpty() || type.isEmpty() || supplier.isEmpty()
                || priceText.isEmpty() || quantityText.isEmpty() || purchaseDate == null) {
            ScannerUtils.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        BigDecimal price;
        int quantity;
        try {
            price = new BigDecimal(priceText);
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            ScannerUtils.showError("Lỗi", "Giá hoặc số lượng không hợp lệ!");
            return;
        }

        Boolean isAllow = true;
        if(rbNotAllow.isSelected()) {
            isAllow = false;
        }

        Device data = new Device(null, imageUrl, name, type, purchaseDate, supplier, price, status, selectedRoom, quantity, isAllow);
        Boolean success = ManagerDeviceRepository.create(data);
        if(success) {
            ScannerUtils.showInfo("Thông báo", "Thiết bị đã thêm thành công!");
            if (onDeviceAdded != null) {
                onDeviceAdded.run();
            }
            Stage currentStage = (Stage) btnSave.getScene().getWindow();
            currentStage.close();
        } else {
            ScannerUtils.showError("Thông báo", "Thiết bị đã thêm không thành công!");
        }
    }

    @FXML
    public void initialize() {
        loadRoomData();
    }

    private void loadRoomData() {
        if(roomComboBox != null){
            ArrayList<Room> roomList = managerDeviceRepository.getAllRooms();
            roomComboBox.setItems(FXCollections.observableArrayList(roomList));

            if (!roomComboBox.getItems().isEmpty()) {
                roomComboBox.getSelectionModel().selectFirst();
            }
        }
    }
}
