package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.BorrowDevice;
import model.BorrowRoom;
import model.BorrowRoomStatus;
import repository.BorrowRoomRepository;
import utils.ScannerUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BorrowRoomListController implements Initializable {
    @FXML private TableView<BorrowRoom> tblBorrowList;
    @FXML private TableColumn<BorrowRoom, String> colId, colName, colDate, colHour, colStatus;
    @FXML private TableColumn<BorrowRoom, Void> colAction;

    private BorrowRoomRepository borrowRoomRepository = new BorrowRoomRepository();

    public void initialize(URL location, ResourceBundle resources) {
        loadBorrowRoom();
    }

    private void loadBorrowRoom(){
        List<BorrowRoom> datas = borrowRoomRepository.getBorrowedRooms();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        colHour.setCellValueFactory(cellData -> {
            BorrowRoom br = cellData.getValue();
            String hourRange = br.getstartPeriod() + " - " + br.getEndPeriod();
            return new SimpleStringProperty(hourRange);
        });
        colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnCancel = new Button("Hủy yêu cầu");
            private final Button btnDetail = new Button("Chi tiết");
            private final Button btnReason = new Button("Lý do");

            {
                btnCancel.setOnAction(event -> {
                    BorrowRoom br = getTableView().getItems().get(getIndex());

                    if(ScannerUtils.showConfirm("Xác nhận", "Bạn có chắc chắn muốn hủy yêu cầu không?")){
                        Boolean success = borrowRoomRepository.cancle(br.getId());
                        if(success){
                            ScannerUtils.showInfo("Thông báo", "Hủy yêu cầu thành công!");
                            loadBorrowRoom();
                        } else{
                            ScannerUtils.showError("Thông báo", "Hủy yêu cầu thất bại!");
                        }
                    }

                });

                btnDetail.setOnAction(event -> {
                    BorrowRoom br = getTableView().getItems().get(getIndex());

                    StringBuilder content = new StringBuilder();
                    content.append("Phòng: ").append(br.getRoomId()).append("\n");
                    content.append("Ngày mượn: ").append(br.getBorrowDate()).append("\n");
                    content.append("Từ tiết đến tiết: ").append(br.getstartPeriod()).append(" - ").append(br.getEndPeriod()).append("\n");
                    content.append("Lý do: ").append(br.getBorrowReason()).append("\n");
                    content.append("Trạng thái: ").append(br.getStatus()).append("\n");

                    // Nếu có thiết bị mượn kèm
                    if (br.getBorrowDevices() != null && !br.getBorrowerId().isEmpty()) {
                        content.append("Thiết bị mượn kèm:\n");
                        for (BorrowDevice d : br.getBorrowDevices()) {
                            content.append(" - ").append(d.getDeviceId()).append("\n");
                        }
                    }

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Chi tiết yêu cầu mượn phòng");
                    alert.setHeaderText("Thông tin chi tiết:");
                    alert.setContentText(content.toString());
                    alert.showAndWait();
                });

                btnReason.setOnAction(event -> {
                    BorrowRoom br = getTableView().getItems().get(getIndex());
                    String reason = br.getRejectReason();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Lý do từ chối");
                    alert.setHeaderText("Yêu cầu bị từ chối");
                    alert.setContentText(reason != null && !reason.isEmpty() ? reason : "Không có lý do cụ thể.");
                    alert.showAndWait();
                });

                // Tuỳ chỉnh style nếu muốn
                btnCancel.getStyleClass().add("btn-cancel");
                btnDetail.getStyleClass().add("btn-detail");
                btnReason.getStyleClass().add("btn-reason");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                BorrowRoom br = getTableView().getItems().get(getIndex());
                switch (br.getStatus()) {
                    case PENDING -> setGraphic(btnCancel);
                    case APPROVED -> setGraphic(btnDetail);
                    case REJECTED -> setGraphic(btnReason);
                    default -> setGraphic(null);
                }
            }
        });


        tblBorrowList.setItems(FXCollections.observableArrayList(datas));
        tblBorrowList.setSelectionModel(null);
    }
}