package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DeviceStatus;
import model.Role;
import model.Status;
import model.User;
import repository.ManagerUserRepository;
import utils.CloudinaryUploaderUtils;
import utils.PasswordEncryptionUtils;
import utils.PasswordGeneratorUtils;
import utils.ScannerUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class EditUserController {
    @FXML
    private ImageView imagePreview;
    @FXML private TextField txtFullname, txtUsername, txtEmail, txtPhoneNumber;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private DatePicker datePickerImport;
    @FXML private RadioButton rbActive, rbInactive, rbMaintenance;
    @FXML private Button btnUpdate;

    private ManagerUserRepository managerUserRepository = new ManagerUserRepository();
    private User user;
    private File selectedImageFile = null;

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
    private void handleUpdate(ActionEvent event) {
        String fullname = txtFullname.getText().trim();
        String username = txtUsername.getText().trim();
        LocalDate yearold = datePickerImport.getValue();
        String phoneNumber = txtPhoneNumber.getText().trim();
        String email = txtEmail.getText().trim();
        Role selectedRole = roleComboBox.getValue();

        Status status = null;
        if (rbActive.isSelected()) {
            status = Status.ACTIVE;
        } else if (rbInactive.isSelected()) {
            status = Status.INACTIVE;
        } else if(rbMaintenance.isSelected()) {
            status = Status.MAINTENANCE;
        }

        if (fullname.isEmpty() || username.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || yearold == null) {
            ScannerUtils.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin!");
            return;
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
            imageUrl = user.getThumbnail();
        }

        if(!ScannerUtils.isValidEmail(email)) {
            ScannerUtils.showError("Lỗi", "Email không hợp lệ!");
            return;
        }

        if(!ScannerUtils.isValidPhoneNumber(phoneNumber)) {
            ScannerUtils.showError("Lỗi", "Số điện thoại không hợp lệ");
            return;
        }

        User data = new User(user.getUserId(), fullname, username, imageUrl, yearold, email, phoneNumber, null, status, selectedRole);
        Boolean success = managerUserRepository.edit(data);
        if(success) {
            ScannerUtils.showInfo("Thông báo", "Cập nhật người dùng thành công thành công!");
            Stage currentStage = (Stage) btnUpdate.getScene().getWindow();
            currentStage.close();
        } else {
            ScannerUtils.showError("Thông báo", "Cập nhật không thành công!");
        }
    }

    public void setUser(User user) {
        this.user = user;
        loadDeviceInfo();
    }

    public void loadDeviceInfo() {
        try {
            User data = managerUserRepository.getUser(user.getUserId());

            txtFullname.setText(data.getFullname());
            txtUsername.setText(data.getUsername());
            txtEmail.setText(data.getEmail());
            txtPhoneNumber.setText(data.getPhoneNumber());
            datePickerImport.setValue(data.getYearold());

            Status status = data.getStatus();
            switch (status) {
                case ACTIVE -> rbActive.setSelected(true);
                case INACTIVE -> rbInactive.setSelected(true);
                case MAINTENANCE -> rbMaintenance.setSelected(true);
            }

            if (data.getThumbnail() != null && !data.getThumbnail().isEmpty()) {
                Image image = new Image(data.getThumbnail(), 100, 100, true, true);
                imagePreview.setImage(image);
            }

            if (data.getRole() != null) {
                roleComboBox.getSelectionModel().select(data.getRole());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadRoleData();
    }

    private void loadRoleData() {
        if(roleComboBox != null){
            List<Role> roles = managerUserRepository.getAllRoles();
            roleComboBox.setItems(FXCollections.observableArrayList(roles));

            if (!roleComboBox.getItems().isEmpty()) {
                roleComboBox.getSelectionModel().selectFirst();
            }
        }
    }
}
