package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import javafx.event.ActionEvent;
import javafx.stage.Stage;
import repository.RoleRepository;
import utils.ScannerUtils;

public class CreateRoleController {
    @FXML private TextField txtName;
    @FXML private Button btnSave;

    private RoleRepository roleRepository = new RoleRepository();
    private Runnable onRoleAdded;
    public void setOnRoleAdded(Runnable onRoleAdded) {
        this.onRoleAdded = onRoleAdded;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = txtName.getText();
        Boolean success = roleRepository.create(name);
        if(success) {
            ScannerUtils.showInfo("Thông báo", "Thêm nhóm quyền thành công!");
            if (onRoleAdded != null) {
                onRoleAdded.run();
            }
            Stage currentStage = (Stage) btnSave.getScene().getWindow();
            currentStage.close();
        } else {
            ScannerUtils.showError("Thông báo", "Thêm nhóm quyền không thành công!");
        }
    }
}
