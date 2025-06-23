package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.*;
import repository.BorrowDeviceRepository;
import repository.BorrowRoomRepository;
import utils.ScannerUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class BorrowDeviceConfirmController implements Initializable {
    @FXML private Label lbDeviceName;
    @FXML private DatePicker dataPickerBorrow;
    @FXML private Button confirmButton;
    @FXML private TextField startPeriodCombo, endPeriodCombo, txtBorrowQuantity;
    @FXML private ListView<String> listAvailableTime;
    @FXML private TextArea txtReason;

    private BorrowDeviceRepository borrowDeviceRepository = new BorrowDeviceRepository();
    private Device device;

    @FXML
    private void handleConfirmButtonAction(ActionEvent event) {
        String borrowQuantity = txtBorrowQuantity.getText().trim();
        LocalDate dateBorrow = dataPickerBorrow.getValue();
        String startPeriodText = startPeriodCombo.getText().trim();
        String endPeriodText = endPeriodCombo.getText().trim();
        String reason = txtReason.getText().trim();

        if(borrowQuantity == null || borrowQuantity.isEmpty()){
            ScannerUtils.showError("Lỗi", "Vui lòng nhập số lượng mượn");
            return;
        }

        if(Integer.parseInt(borrowQuantity) > device.getAvailableQuantity()){
            ScannerUtils.showError("Lỗi", "Số lượng mượn không được vượt quá số lượng còn trống là " + device.getAvailableQuantity());
            return;
        }

        if(dateBorrow == null){
            ScannerUtils.showError("Lỗi", "Vui lòng nhập ngày tháng năm mượn");
            return;
        }

        if (dateBorrow.isBefore(LocalDate.now())) {
            ScannerUtils.showError("Lỗi", "Ngày mượn không được nhỏ hơn ngày hiện tại");
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

        if(ScannerUtils.showConfirm("Xác nhận", "Bạn có chắc chắn muốn mượn thiết bị " + lbDeviceName.getText() + " với số lượng là " + borrowQuantity)){
            if (borrowDeviceRepository.isDeviceScheduleConflict(device.getId(), dateBorrow, startPeriod, endPeriod)) {
                ScannerUtils.showError("Lỗi", "Thiết bị này đã có người đăng ký trong khoảng thời gian này!");
                return;
            }

            User borrower = new User(UserSession.getUserId(), null, null, null);
            BorrowDevice data = new BorrowDevice(0, borrower, dateBorrow, startPeriod, endPeriod, null, null,null, reason, null, new BorrowDeviceDetail(0, 0, device, Integer.parseInt(borrowQuantity)));
            Boolean success = borrowDeviceRepository.createBorrowDevice(data);
            if(success){
                ScannerUtils.showInfo("Thông báo", "Gửi yêu cầu mượn thiết bị thành công! Vui lòng chờ xét duyệt");

                Stage stage = (Stage) confirmButton.getScene().getWindow();
                stage.close();
            } else {
                ScannerUtils.showError("Thông báo", "Mượn thiết bị không thành công!");
            }
        }
    }

    public void initialize(URL location, ResourceBundle resources){
        dataPickerBorrow.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && device.getId() != null) {
                List<String> availableSlots = getAvailablePeriodSlots(device.getId(), newDate);
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

    private List<String> getAvailablePeriodSlots(String deviceId, LocalDate borrowDate) {
        List<Pair<Integer, Integer>> booked = borrowDeviceRepository.getBookedPeriodSlots(deviceId, borrowDate);
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

    public void setDevice(Device device){
        this.device =  device;
        lbDeviceName.setText(device.getDeviceName());
    }


}
