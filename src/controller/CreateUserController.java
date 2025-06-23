package controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import repository.ManagerDeviceRepository;
import repository.ManagerUserRepository;
import utils.CloudinaryUploaderUtils;
import utils.PasswordEncryptionUtils;
import utils.PasswordGeneratorUtils;
import utils.ScannerUtils;


import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreateUserController {
    @FXML private ImageView imagePreview;
    @FXML private TextField txtFullname, txtUsername, txtEmail, txtPhoneNumber;
    @FXML private ComboBox<Role> roleComboBox;
    @FXML private ComboBox<String> cboClass, cbDepartment;
    @FXML private DatePicker datePickerImport;
    @FXML private RadioButton rbActive, rbInactive, rbMaintenance;
    @FXML private Button btnSave;
    @FXML private VBox VbClass, VbDepartment;

    private ManagerUserRepository managerUserRepository = new ManagerUserRepository();
    private File selectedImageFile = null;
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
        String fullname = txtFullname.getText().trim();
        String username = txtUsername.getText().trim();
        LocalDate yearold = datePickerImport.getValue();
        String phoneNumber = txtPhoneNumber.getText().trim();
        String email = txtEmail.getText().trim();
        Role selectedRole = roleComboBox.getValue();
        String selectedClass =  cboClass.getValue();
        String selectedDepartment = cbDepartment.getValue();
        String password = PasswordGeneratorUtils.generateStrongPassword();

        System.out.println(password);
        password = PasswordEncryptionUtils.hashPassword(password);

        Status status = null;
        if (rbActive.isSelected()) {
            status = Status.ACTIVE;
        } else if (rbInactive.isSelected()) {
            status = Status.INACTIVE;
        } else if(rbMaintenance.isSelected()) {
            status = Status.MAINTENANCE;
        }

        if(selectedRole.getRoleName().equals("Sinh Viên")){
            selectedDepartment = null;
        } else if(selectedRole.getRoleName().equals("Bảo trì") || selectedRole.getRoleName().equals("Giáo viên")){
            selectedClass = null;
        } else {
            selectedClass = null;
            selectedDepartment = null;
        }

        System.out.println(selectedClass + selectedDepartment);

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
            ScannerUtils.showError("Lỗi", "Vui lòng thêm hình ảnh");
            return;
        }

        if(!ScannerUtils.isValidEmail(email)) {
            ScannerUtils.showError("Lỗi", "Email không hợp lệ!");
            return;
        }

        if(!ScannerUtils.isValidPhoneNumber(phoneNumber)) {
            ScannerUtils.showError("Lỗi", "Số điện thoại không hợp lệ");
            return;
        }

        User data = new User(null, fullname, username, imageUrl, yearold, email, phoneNumber, password, status, selectedRole, selectedClass, selectedDepartment);
        Boolean success = managerUserRepository.addUserAndReturnID(data);
        if(success) {
            ScannerUtils.showInfo("Thông báo", "Thêm người dùng thành công thành công!");
            if (onDeviceAdded != null) {
                onDeviceAdded.run();
            }
            Stage currentStage = (Stage) btnSave.getScene().getWindow();
            currentStage.close();
        } else {
            ScannerUtils.showError("Thông báo", "Thêm không thành công!");
        }
    }

    @FXML
    public void initialize() {
        loadComboxData();
        roleComboBox.setOnAction(e -> updateUnitComboBox());

    }

    private void updateUnitComboBox() {
        String role = roleComboBox.getValue().getRoleName();

        VbClass.setDisable(true);
        VbDepartment.setDisable(true);

        if (role.equals("Sinh Viên")) {
            VbClass.setDisable(false);
        } else if (role.equals("Giáo viên") || role.equals("Bảo trì")) {
            VbDepartment.setDisable(false);
        }
    }

    private void loadComboxData() {
        if(roleComboBox != null && cboClass != null && cbDepartment != null) {
            List<Role> roles = managerUserRepository.getAllRoles();
            List<String> classes = managerUserRepository.getAllClass();
            List<String> departments = managerUserRepository.getAllDepartment();

            roleComboBox.setItems(FXCollections.observableArrayList(roles));
            cboClass.setItems(FXCollections.observableArrayList(classes));
            cbDepartment.setItems(FXCollections.observableArrayList(departments));

            if (!roleComboBox.getItems().isEmpty() && !cboClass.getItems().isEmpty() && !cbDepartment.getItems().isEmpty()) {
                roleComboBox.getSelectionModel().selectFirst();
                cboClass.getSelectionModel().selectFirst();
                cbDepartment.getSelectionModel().selectFirst();
                updateUnitComboBox();
            }
        }
    }
}
