package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.*;
import repository.BorrowRoomRepository;
import utils.ScannerUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BorrowRoomConfirmController implements Initializable {
    @FXML private Label lbRoomName;
    @FXML private TableView<Device> tblDevice;
    @FXML private TableColumn<Device, Boolean> colBorrow;
    @FXML private TableColumn<Device, String> colId ,colName, colStatus, colQuantityAvailable;
    @FXML private TableColumn<Device, Void> colQuantityBorrow;
    @FXML private Button confirmButton;
    @FXML private DatePicker dataPickerBorrow;
    @FXML private TextField startPeriodCombo, endPeriodCombo;
    @FXML private ListView<String> listAvailableTime;
    @FXML private TextArea txtReason;

    private BorrowRoomRepository borrowRoomRepository = new BorrowRoomRepository();
    private String roomId;
    private Runnable reloadRoomListCallback;

    @FXML
    private void handleConfirmButtonAction(ActionEvent event) {
        LocalDate dateBorrow = dataPickerBorrow.getValue();
        String startPeriodText = startPeriodCombo.getText().trim();
        String endPeriodText = endPeriodCombo.getText().trim();
        String reason = txtReason.getText().trim();

        if(dateBorrow == null){
            ScannerUtils.showError("Lỗi", "Vui lòng nhập ngày tháng năm mượn");
            return;
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if (startPeriodText.isEmpty() || endPeriodText.isEmpty()) {
            ScannerUtils.showError("Lỗi", "Vui lòng nhập đầy đủ tiết bắt đầu và kết thúc.");
            return;
        }

        int startPeriod, endPeriod;
        try {
            startPeriod = Integer.parseInt(startPeriodText);
            endPeriod = Integer.parseInt(endPeriodText);
        } catch (NumberFormatException e) {
            ScannerUtils.showError("Lỗi", "Tiết học không hợp lệ. Vui lòng chọn lại.");
            return;
        }

        if (endPeriod < startPeriod) {
            ScannerUtils.showError("Lỗi", "Tiết kết thúc phải bằng hoặc sau tiết bắt đầu.");
            return;
        }

        if (startPeriod < 1 || endPeriod > 14) {
            ScannerUtils.showError("Thông báo", "Tiết học phải trong khoảng 1 đến 14.");
            return;
        }

        List<Device> allDevices = tblDevice.getItems();

        List<BorrowDeviceDetail> borrowDevicesDetail = new ArrayList<>();
        for(Device device : allDevices) {
            borrowDevicesDetail.add(new BorrowDeviceDetail(0, 0, new Device(device.getId(), device.getDeviceName(), null), device.getAvailableQuantity()));
        }

        if(ScannerUtils.showConfirm("Xác nhận", "Bạn có chắc chắn muốn mượn phòng " + lbRoomName.getText() + " và " + borrowDevicesDetail.size() + " thiết bị kèm theo?")){
            if (borrowRoomRepository.isRoomScheduleConflict(roomId, dateBorrow, startPeriod, endPeriod)) {
                ScannerUtils.showError("Lỗi", "Phòng này đã có người đăng ký trong khoảng thời gian này!");
                return;
            }

            User borrower = new User(UserSession.getUserId(), null);
            BorrowRoom data = new BorrowRoom(0, roomId, null, borrower, dateBorrow, startPeriod, endPeriod, reason,null, null, borrowDevicesDetail);
            Boolean success = borrowRoomRepository.createBorrowRoom(data);
            if(success){
                ScannerUtils.showInfo("Thông báo", "Gửi yêu cầu mượn phòng thành công! Vui lòng chờ xét duyệt");

                Stage stage = (Stage) confirmButton.getScene().getWindow();
                stage.close();
            } else {
                ScannerUtils.showError("Thông báo", "Mượn phòng không thành công!");
            }
        }

    }

    public void setReloadRoomListCallback(Runnable callback) {
        this.reloadRoomListCallback = callback;
    }

    private void loadAvailableTime(String roomId, LocalDate dateBorrow) {
        List<String> slots = getAvailablePeriodSlots(roomId, dateBorrow);
        listAvailableTime.getItems().setAll(slots);
    }

    private List<String> getAvailablePeriodSlots(String roomId, LocalDate borrowDate) {
        List<Pair<Integer, Integer>> booked = borrowRoomRepository.getBookedPeriodSlots(roomId, borrowDate);
        List<String> availableSlots = new ArrayList<>();

        booked.sort(Comparator.comparing(Pair::getKey));

        int current = 1;
        int lastPeriod = 14;

        for (Pair<Integer, Integer> slot : booked) {
            int bookedStart = slot.getKey();
            int bookedEnd = slot.getValue();

            if (current < bookedStart) {
                availableSlots.add((current == (bookedStart - 1)) ? ("Tiết " + current) : ("Tiết " + current + " - Tiết " + (bookedStart - 1)));
            }

            current = Math.max(current, bookedEnd + 1);
        }

        if (current <= lastPeriod) {
            availableSlots.add((current == lastPeriod) ? ("Tiết " + current) : ("Tiết " + current + " - Tiết " + lastPeriod));
        }

        return availableSlots;
    }


    public void initialize(URL location, ResourceBundle resources) {
        dataPickerBorrow.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && roomId != null) {
                List<String> availableSlots = getAvailablePeriodSlots(roomId, newDate);
                listAvailableTime.getItems().clear();

                if (availableSlots.isEmpty()) {
                    listAvailableTime.getItems().add("Không còn thời gian trống");
                    confirmButton.setDisable(true);
                } else {
                    listAvailableTime.getItems().addAll(availableSlots);
                    confirmButton.setDisable(false);
                }
            }
        });
    }

    public void setRoom(String id, String name) {
        roomId = id;
        lbRoomName.setText(name);
        loadDeviceData(id);
    }

    public void loadDeviceData(String roomId) {
        List<Device> datas = borrowRoomRepository.dataDeviceByRoom(roomId);

        if(colId != null && colName != null && colStatus != null && colQuantityAvailable != null && tblDevice != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colName.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
            colStatus.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));
            colQuantityAvailable.setCellValueFactory(new PropertyValueFactory<>("availableQuantity"));
            tblDevice.setItems(FXCollections.observableArrayList(datas));
            tblDevice.setSelectionModel(null);
        }
    }
}
