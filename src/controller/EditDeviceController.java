package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import model.Device;
import model.DeviceStatus;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Room;
import repository.ManagerDeviceRepository;
import utils.CloudinaryUploaderUtils;
import utils.ScannerUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class EditDeviceController {

    @FXML private TextField txtName;
    @FXML private ComboBox<String> deviceTypeCombox;
    @FXML private TextField txtSupplier;
    @FXML private TextField txtPrice;
    @FXML private TextField txtQuantity;
    @FXML private DatePicker datePickerImport;
    @FXML private RadioButton rbAvailble;
    @FXML private RadioButton rbUnavailble;
    @FXML private RadioButton rbMaintenance;
    @FXML private RadioButton rbBroken;
    @FXML private ImageView imagePreview;
    @FXML private ComboBox<Room> roomComboBox;
    @FXML private File selectedImageFile;
    @FXML private Button btnUpdate;

    private ManagerDeviceRepository managerDeviceRepository = new ManagerDeviceRepository();
    private Device device;

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
    private void handleEdit(ActionEvent event) {
        String name = txtName.getText().trim();
        String type = deviceTypeCombox.getValue();
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

            imageUrl = device.getThumbnail();
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

        Device data = new Device(device.getId(), imageUrl, name, type, purchaseDate, supplier, price, status, selectedRoom, quantity, quantity);
        Boolean success = ManagerDeviceRepository.edit(data);
        if(success) {
            ScannerUtils.showInfo("Thông báo", "Thiết bị đã cập nhật thành công!");
            Stage currentStage = (Stage) btnUpdate.getScene().getWindow();
            currentStage.close();
        } else {
            ScannerUtils.showError("Thông báo", "Thiết bị cập nhật không thành công!");
        }
    }

    public void setDevice(Device device) {
        this.device = device;
        loadDeviceInfo();
    }

    public void loadDeviceInfo() {
        try {
            Device data = ManagerDeviceRepository.DataDevice(device.getId());

            txtName.setText(data.getDeviceName());
            txtSupplier.setText(data.getSupplier());
            txtPrice.setText(data.getPrice().toString());
            txtQuantity.setText(String.valueOf(data.getQuantity()));
            datePickerImport.setValue(data.getPurchaseDate());

            DeviceStatus status = data.getStatus();
            switch (status) {
                case AVAILABLE -> rbAvailble.setSelected(true);
                case UNAVAILABLE -> rbUnavailble.setSelected(true);
                case UNDER_MAINTENANCE -> rbMaintenance.setSelected(true);
                case BROKEN -> rbBroken.setSelected(true);
            }

            if (data.getThumbnail() != null && !data.getThumbnail().isEmpty()) {
                Image image = new Image(data.getThumbnail(), 100, 100, true, true);
                imagePreview.setImage(image);
            }

            if(data.getDeviceType() != null){
                deviceTypeCombox.getSelectionModel().select(data.getDeviceType());
            }

            if (data.getRoom() != null) {
                roomComboBox.getSelectionModel().select(data.getRoom());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showEditDeviceWindow(Device device, Runnable onClose) {
        try {
            FXMLLoader loader = new FXMLLoader(EditDeviceController.class.getResource("/fxml/device/EditDevice.fxml"));
            Parent editRoot = loader.load();

            EditDeviceController controller = loader.getController();
            controller.setDevice(device);

            Stage editStage = new Stage();
            editStage.setTitle("Chỉnh sửa thiết bị");
            editStage.setScene(new Scene(editRoot));
            editStage.setOnHidden(e -> onClose.run());
            editStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadDeviceTypeData();
        loadRoomData();
    }

    private void loadDeviceTypeData(){
        if(deviceTypeCombox != null){
            ArrayList<String> deviceTypeList = managerDeviceRepository.getAllDeviceTypes();
            deviceTypeCombox.setItems(FXCollections.observableArrayList(deviceTypeList));

            if(!deviceTypeCombox.getItems().isEmpty()){
                deviceTypeCombox.getSelectionModel().selectFirst();
            }
        }
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
