//package controller;
//
//import javafx.beans.property.SimpleStringProperty;
//import javafx.collections.FXCollections;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.layout.HBox;
//import javafx.stage.Stage;
//import model.BorrowDevice;
//import model.BorrowRoom;
//import model.BorrowRoomStatus;
//import repository.BorrowRoomRepository;
//import utils.ScannerUtils;
//
//import java.util.List;
//
//public class BorrowRequestListController {
//    @FXML private TableView<BorrowRoom> tblBorrowRequests;
//    @FXML private TableColumn<BorrowRoom, String> colId, colRoomNumber, colBorrower, colBorrowDate, colPeriod, colReason, colStatus;
//    @FXML private TableColumn<BorrowRoom, Void> colActions;
//    @FXML private Button btnClose;
//
//    private BorrowRoomRepository borrowRoomRepository = new BorrowRoomRepository();
//    private Runnable onRequestProcessed;
//
//    public void initialize() {
//        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
//        colRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
//        colBorrower.setCellValueFactory(new PropertyValueFactory<>("borrowerName"));
//        colBorrowDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
//        colPeriod.setCellValueFactory(cellData -> {
//            BorrowRoom br = cellData.getValue();
//            return new SimpleStringProperty(br.getStartPeriod() + " - " + br.getEndPeriod());
//        });
//        colReason.setCellValueFactory(new PropertyValueFactory<>("borrowReason"));
//        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
//
//        colActions.setCellFactory(param -> new TableCell<BorrowRoom, Void>() {
//            private final Button btnApprove = new Button("Phê duyệt");
//            private final Button btnReject = new Button("Từ chối");
//            private final Button btnDetail = new Button("Chi tiết");
//
//            {
//                btnApprove.setOnAction(e -> handleApproveAction());
//                btnReject.setOnAction(e -> handleRejectAction());
//                btnDetail.setOnAction(e -> handleDetailAction());
//                HBox actionBox = new HBox(10, btnDetail, btnApprove, btnReject);
//                setGraphic(actionBox);
//            }
//
//            @Override
//            protected void updateItem(Void item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || getTableView().getItems().get(getIndex()).getStatus() != BorrowRoomStatus.PENDING) {
//                    setGraphic(null);
//                } else {
//                    setGraphic(getGraphic());
//                }
//            }
//
//            private void handleApproveAction() {
//                BorrowRoom request = getTableView().getItems().get(getIndex());
//                if (ScannerUtils.showConfirm("Xác nhận", "Phê duyệt yêu cầu mượn phòng " + request.getRoomNumber() + "?")) {
//                    boolean success = borrowRoomRepository.approveBorrowRequest(request.getId());
//                    if (success) {
//                        ScannerUtils.showInfo("Thông báo", "Phê duyệt thành công!");
//                        loadData();
//                        if (onRequestProcessed != null) onRequestProcessed.run();
//                    } else {
//                        ScannerUtils.showError("Lỗi", "Phê duyệt thất bại!");
//                    }
//                }
//            }
//
//            private void handleRejectAction() {
//                BorrowRoom request = getTableView().getItems().get(getIndex());
//                TextInputDialog dialog = new TextInputDialog();
//                dialog.setTitle("Từ chối yêu cầu");
//                dialog.setHeaderText("Nhập lý do từ chối:");
//                dialog.setContentText("Lý do:");
//                dialog.showAndWait().ifPresent(reason -> {
//                    if (reason.trim().isEmpty()) {
//                        ScannerUtils.showError("Lỗi", "Vui lòng nhập lý do từ chối!");
//                        return;
//                    }
//                    boolean success = borrowRoomRepository.rejectBorrowRequest(request.getId(), reason);
//                    if (success) {
//                        ScannerUtils.showInfo("Thông báo", "Từ chối thành công!");
//                        loadData();
//                        if (onRequestProcessed != null) onRequestProcessed.run();
//                    } else {
//                        ScannerUtils.showError("Lỗi", "Từ chối thất bại!");
//                    }
//                });
//            }
//
//            private void handleDetailAction() {
//                BorrowRoom request = getTableView().getItems().get(getIndex());
//                StringBuilder content = new StringBuilder();
//                content.append("Phòng: ").append(request.getRoomNumber()).append("\n");
//                content.append("Người mượn: ").append(request.getBorrowerName()).append("\n");
//                content.append("Ngày mượn: ").append(request.getBorrowDate()).append("\n");
//                content.append("Tiết học: ").append(request.getStartPeriod()).append(" - ").append(request.getEndPeriod()).append("\n");
//                content.append("Lý do: ").append(request.getBorrowReason()).append("\n");
//                content.append("Trạng thái: ").append(request.getStatus()).append("\n");
//
//                List<BorrowDevice> devices = request.getBorrowDevices();
//                if (devices != null && !devices.isEmpty()) {
//                    content.append("Thiết bị mượn kèm:\n");
//                    for (BorrowDevice d : devices) {
//                        content.append(" - Mã thiết bị: ").append(d.getDeviceId()).append(", Số lượng: ").append(d.getQuantity()).append("\n");
//                    }
//                } else {
//                    content.append("Không có thiết bị mượn kèm.\n");
//                }
//
//                Alert alert = new Alert(Alert.AlertType.INFORMATION);
//                alert.setTitle("Chi tiết yêu cầu mượn phòng");
//                alert.setHeaderText("Thông tin chi tiết:");
//                alert.setContentText(content.toString());
//                alert.showAndWait();
//            }
//        });
//
//        loadData();
//    }
//
//    @FXML
//    private void handleCloseAction() {
//        Stage stage = (Stage) btnClose.getScene().getWindow();
//        stage.close();
//    }
//
//    private void loadData() {
//        List<BorrowRoom> requests = borrowRoomRepository.getPendingBorrowRequests();
//        tblBorrowRequests.setItems(FXCollections.observableArrayList(requests));
//    }
//
//    public void setOnRequestProcessed(Runnable onRequestProcessed) {
//        this.onRequestProcessed = onRequestProcessed;
//    }
//}