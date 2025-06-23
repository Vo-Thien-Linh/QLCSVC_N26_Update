package controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.PermissionRow;
import model.Role;
import repository.PermissionRepository;
import repository.RoleRepository;
import utils.ScannerUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PermissionController implements Initializable {
    @FXML TableView<PermissionRow> tbPermission;
    @FXML private StackPane btnUpdate;

    private PermissionRepository permissionRepository = new PermissionRepository();

    public void initialize(URL location, ResourceBundle resources){
        roleTableColumn();
        updatePermission();
        loadPermissionData();
    }

    private void updatePermission(){
        btnUpdate.setOnMouseClicked(event -> {
            ObservableList<PermissionRow> allRows = tbPermission.getItems();

            String currentHeader = null;
            boolean allSuccess = true;
            for (PermissionRow row : allRows) {
                if (!row.isHeader()) {
                    String permissionType = row.getFeature().trim();
                    System.out.println(permissionType);

                    int functionId = permissionRepository.getFunctionIdByName(currentHeader);
                    int permissionTypeId = permissionRepository.getIdByFeatureName(permissionType);
                    System.out.println(functionId + " " + permissionTypeId);

                    Map<String, Boolean> permissions = row.getPermissions();
                    for (String roleName : permissions.keySet()) {

                        int roleId = permissionRepository.getRoleIdByName(roleName);
                        boolean isAllowed = permissions.get(roleName);
                        System.out.println(roleId + " " + isAllowed);

                        boolean success = permissionRepository.updatePermission(roleId, functionId, permissionTypeId, isAllowed);
                        if (!success) {
                            allSuccess = false;
                        }
                    }
                } else {
                    currentHeader = row.getFeature().trim();
                }
            }

            if(allSuccess){
                ScannerUtils.showInfo("Thông báo", "Cập nhật thành công!");
                loadPermissionData();
            } else {
                ScannerUtils.showError("Thông báo", "Cập nhật không thành công!");
            }
        });
    }

    private void loadPermissionData() {
        List<Role> allRoles = permissionRepository.getAllRoles();

        List<PermissionRow> rows = getPermissionRowsFromDatabase(allRoles);
        tbPermission.setItems(FXCollections.observableArrayList(rows));
    }


    private List<PermissionRow> getPermissionRowsFromDatabase(List<Role> allRoles) {
        List<PermissionRow> rows = new ArrayList<>();
        List<String> functions = permissionRepository.getAllFunctions();

        for (String function : functions) {
            System.out.println(function);
            rows.add(new PermissionRow(function, allRoles, true));

            List<String> permissionTypes = permissionRepository.getPermissionTypesByFunction(function);
            for (String type : permissionTypes) {
                PermissionRow row = new PermissionRow("    " + type, allRoles, false);
                for (Role role : allRoles) {
                    boolean isAllowed = permissionRepository.isAllowed(role.getRoleId(), function, type);
                    row.setPermission(role.toString(), isAllowed);
                }
                rows.add(row);
            }
        }

        return rows;
    }



    private void roleTableColumn(){
        List<Role> roles = permissionRepository.getAllRoles();
        tbPermission.getColumns().clear();

        // Cột "Tính năng"
        TableColumn<PermissionRow, String> featureCol = new TableColumn<>("Tính năng");
        featureCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getFeature()));
        tbPermission.getColumns().add(featureCol);
        featureCol.setCellFactory(column -> new TableCell<PermissionRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setText(null);
                    setStyle("");
                } else {
                    PermissionRow row = getTableView().getItems().get(getIndex());
                    setText(item);
                    setStyle("-fx-alignment: CENTER-LEFT; " + (row.isHeader() ? "-fx-font-weight: bold; -fx-font-size: 18px " : ""));
                }
            }
        });

        // Cột động theo vai trò
        for (Role role : roles) {
            String roleName = role.toString();
            TableColumn<PermissionRow, Boolean> roleCol = new TableColumn<>(roleName);
            roleCol.setCellValueFactory(cellData -> {
                Boolean value = cellData.getValue().getPermission(roleName);
                return new ReadOnlyObjectWrapper<>(value);
            });

            // Đặt CellFactory để cho phép sửa ô bằng checkbox
            roleCol.setCellFactory(tc -> new TableCell<PermissionRow, Boolean>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setDisable(false);
                    checkBox.setOnAction(e -> {
                        PermissionRow row = getTableView().getItems().get(getIndex());
                        if (row != null) {
                            row.setPermission(roleName, checkBox.isSelected());
                        }
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        PermissionRow row = getTableView().getItems().get(getIndex());
                        if (row.isHeader()) {
                            setGraphic(null);
                        } else {
                            checkBox.setSelected(item != null && item);
                            setGraphic(checkBox);
                        }
                    }
                }
            });

            tbPermission.getColumns().add(roleCol);
        }
        ObservableList<PermissionRow> rows = FXCollections.observableArrayList();

//        Tính năng tổng quan
        rows.add(new PermissionRow("Tổng quan", roles, true));
        rows.add(new PermissionRow("Xem", roles, false));

//        Tính năng quản lý thiết bị
        rows.add(new PermissionRow("Quản lý thiết bị", roles, true));
        rows.add(new PermissionRow("Thêm mới", roles, false));
        rows.add(new PermissionRow("Chỉnh sửa", roles, false));
        rows.add(new PermissionRow("Xóa", roles, false));
        rows.add(new PermissionRow("Xem", roles, false));

//        Tính năng quản lý phòng
        rows.add(new PermissionRow("Quản lý phòng", roles, true));
        rows.add(new PermissionRow("Thêm mới", roles, false));
        rows.add(new PermissionRow("Chỉnh sửa", roles, false));
        rows.add(new PermissionRow("Xóa", roles, false));
        rows.add(new PermissionRow("Xem", roles, false));

//        Tính năng quản lý người dùng
        rows.add(new PermissionRow("Quản lý người dùng", roles, true));
        rows.add(new PermissionRow("Thêm mới", roles, false));
        rows.add(new PermissionRow("Chỉnh sửa", roles, false));
        rows.add(new PermissionRow("Xóa", roles, false));
        rows.add(new PermissionRow("Xem", roles, false));

//        Tính năng nhóm quyền
        rows.add(new PermissionRow("Nhóm quyền", roles, true));
        rows.add(new PermissionRow("Thêm mới", roles, false));
        rows.add(new PermissionRow("Chỉnh sửa", roles, false));
        rows.add(new PermissionRow("Xóa", roles, false));
        rows.add(new PermissionRow("Xem", roles, false));
        rows.add(new PermissionRow("Phân quyền", roles, false));

//        Tính năng thống kê
        rows.add(new PermissionRow("Thống kê", roles, true));
        rows.add(new PermissionRow("Xem", roles, false));

//        Tính năng mượn thiết bị
        rows.add(new PermissionRow("Mượn thiết bị", roles, true));
        rows.add(new PermissionRow("Xem", roles, false));

//        Tính năng mượn phòng
        rows.add(new PermissionRow("Mượn phòng", roles, true));
        rows.add(new PermissionRow("Xem", roles, false));

 //     Tính năng báo cáo sự cố
        rows.add(new PermissionRow("Báo cáo sự cố", roles, true));
        rows.add(new PermissionRow("Xem", roles, false));

        //     Tính năng xử lý sự cố
        rows.add(new PermissionRow("Xử lý sự cố", roles, true));
        rows.add(new PermissionRow("Xem", roles, false));

        tbPermission.setItems(rows);
    }
}
