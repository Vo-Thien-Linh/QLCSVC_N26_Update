package controller;

import com.mysql.cj.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Role;
import model.UserSession;
import repository.RoleRepository;
import utils.ScannerUtils;

public class EditRoleController {
    @FXML
    private TextField txtName;
    @FXML private Button btnUpdate;

    private RoleRepository roleRepository = new RoleRepository();
    private Runnable onRoleAdded;
    public void setOnRoleAdded(Runnable onRoleAdded) {
        this.onRoleAdded = onRoleAdded;
    }
    private Role role;

    public void setRole(Role role) {
        this.role = role;
        loadRoleData();
    }

    private void loadRoleData(){
        Role data = roleRepository.getRoleById(role.getRoleId());
        txtName.setText(data.getRoleName());
    }

    @FXML
    private void handleUpdate(ActionEvent event) {
        String name = txtName.getText();
        Role data = new Role(role.getRoleId(), name);
        Boolean success = roleRepository.edit(data);
        if(success) {
            ScannerUtils.showInfo("Thông báo", "Thêm nhóm quyền thành công!");
            if (onRoleAdded != null) {
                onRoleAdded.run();
            }
            Stage currentStage = (Stage) btnUpdate.getScene().getWindow();
            currentStage.close();
        } else {
            ScannerUtils.showError("Thông báo", "Thêm nhóm quyền không thành công!");
        }
    }
}
