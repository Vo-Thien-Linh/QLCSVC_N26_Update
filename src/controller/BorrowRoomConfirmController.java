package controller;

import javafx.animation.ScaleTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import model.*;
import repository.BorrowRoomRepository;
import utils.ScannerUtils;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class BorrowRoomConfirmController implements Initializable {
    @FXML private Label lbRoomName;
    @FXML private TableView<Device> tblDevice;
    @FXML private TableColumn<Device, Boolean> colBorrow;
    @FXML private TableColumn<Device, String> colId ,colName, colStatus, colQuantityAvailable;
    @FXML private TableColumn<Device, Void> colQuantityBorrow;
    @FXML private Button confirmButton;
    @FXML private DatePicker dataPickerBorrow;
    @FXML private TextField startTimeField, endTimeField;
    @FXML private ListView<String> listAvailableTime;
    @FXML private TextArea txtReason;

    private BorrowRoomRepository borrowRoomRepository = new BorrowRoomRepository();
    private String roomId;
    private Runnable reloadRoomListCallback;

    @FXML
    private void handleConfirmButtonAction(ActionEvent event) {
        LocalDate dateBorrow = dataPickerBorrow.getValue();
        String startTimeText = startTimeField.getText().trim();
        String endTimeText = endTimeField.getText().trim();
        String reason = txtReason.getText().trim();

        if(dateBorrow == null){
            ScannerUtils.showError("Lỗi", "Vui lòng nhập ngày tháng năm mượn");
            return;
        }

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        if (startTimeText.isEmpty() || endTimeText.isEmpty()) {
            ScannerUtils.showError("Lỗi", "Vui lòng nhập đầy đủ thời gian bắt đầu và kết thúc.");
            return;
        }

        LocalTime startTime, endTime;
        try {
            startTime = LocalTime.parse(startTimeText, timeFormatter);
            endTime = LocalTime.parse(endTimeText, timeFormatter);
        } catch (DateTimeParseException e) {
            ScannerUtils.showError("Lỗi", "Thời gian không đúng định dạng (HH:mm). Vui lòng nhập lại, ví dụ: 08:30.");
            return;
        }

        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            ScannerUtils.showError("Lỗi", "Thời gian kết thúc phải sau thời gian bắt đầu.");
            return;
        }

        if(startTime.isBefore(LocalTime.of(7, 0)) || endTime.isAfter(LocalTime.of(22, 0))) {
            ScannerUtils.showError("Thông báo", "Thời gian đặt phòng ngoài giờ mở cửa (07:00 - 22:00).");
            return;
        }

        List<Device> allDevices = tblDevice.getItems();
        List<Device> selectedDevices = allDevices.stream()
                .filter(Device::isSelectedForBorrow)
                .toList();
        Optional<Device> invalidDevice = selectedDevices.stream()
                .filter(device -> device.getQuantityToBorrow() <= 0)
                .findFirst();
        if (invalidDevice.isPresent()) {
            ScannerUtils.showError("Lỗi", "Vui lòng nhập số lượng mượn!");
            return;
        }

        List<BorrowDevice> borrowDevices = new ArrayList<>();
        for(Device device : selectedDevices) {
            borrowDevices.add(new BorrowDevice(0, 0, device.getId(), device.getQuantityToBorrow()));
        }

        if(ScannerUtils.showConfirm("Xác nhận", "Bạn có chắc chắn muốn mượn phòng " + lbRoomName.getText() + " và " + borrowDevices.size() + " thiết bị kèm theo?")){
            if (borrowRoomRepository.isRoomScheduleConflict(roomId, dateBorrow, startTime, endTime)) {
                ScannerUtils.showError("Lỗi", "Phòng này đã có người đăng ký trong khoảng thời gian này!");
                return;
            }

            BorrowRoom data = new BorrowRoom(0, roomId, null, UserSession.getUserId(), dateBorrow, startTime, endTime, reason,null, null, borrowDevices);
            Boolean success = borrowRoomRepository.createBorrowRoom(data);
            if(success){
                ScannerUtils.showInfo("Thông báo", "Gửi yêu cầu mượn phòng thành công! Vui lòng chờ xét duyệt");
                List<String> availableSlots = getAvailableTimeSlots(roomId, dateBorrow);
                if(availableSlots.isEmpty()){
                    borrowRoomRepository.updateRoomStatus(roomId, "OCCUPIED");
                    Stage stage = (Stage) confirmButton.getScene().getWindow();
                    stage.close();

                    if (reloadRoomListCallback != null) {
                        reloadRoomListCallback.run();
                    }
                } else {
                    Stage stage = (Stage) confirmButton.getScene().getWindow();
                    stage.close();
                }
            } else {
                ScannerUtils.showError("Thông báo", "Mượn phòng không thành công!");
            }
        }

    }

    public void setReloadRoomListCallback(Runnable callback) {
        this.reloadRoomListCallback = callback;
    }

    private void loadAvailableTime(String roomId, LocalDate dateBorrow) {
        List<String> slots = getAvailableTimeSlots(roomId, dateBorrow);
        listAvailableTime.getItems().setAll(slots);
    }

    private List<String> getAvailableTimeSlots(String roomId, LocalDate borrowDate) {
        LocalTime openTime = LocalTime.of(7, 0);
        LocalTime closeTime = LocalTime.of(21, 0);

        List<Pair<LocalTime, LocalTime>> booked = borrowRoomRepository.getBookedTimeSlots(roomId, borrowDate);
        List<String> availableSlots = new ArrayList<>();

        booked.sort(Comparator.comparing(Pair::getKey));

        LocalTime current = openTime;
        for (Pair<LocalTime, LocalTime> slot : booked) {
            if (current.isBefore(slot.getKey())) {
                availableSlots.add(current + " - " + slot.getKey());
            }
            current = current.isAfter(slot.getValue()) ? current : slot.getValue();
        }

        if (current.isBefore(closeTime)) {
            availableSlots.add(current + " - " + closeTime);
        }

        return availableSlots;
    }

    public void initialize(URL location, ResourceBundle resources) {
        dataPickerBorrow.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null && roomId != null) {
                loadAvailableTime(roomId, newDate);
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
            colQuantityAvailable.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colQuantityBorrow.setCellFactory(column -> new TableCell<>() {
                private final TextField quantityField = new TextField();
                private ChangeListener<String> quantityListener;

                {
                    quantityListener = (obs, oldVal, newVal) -> {
                        int index = getIndex();
                        if (index < 0 || index >= getTableView().getItems().size()) return;
                        Device device = getTableView().getItems().get(index);
                        if (newVal.isEmpty()) {
                            device.setQuantityToBorrow(0);
                            return;
                        }

                        try {
                            int value = Integer.parseInt(newVal);
                            int available = device.getQuantity();

                            if (value < 1) {
                                quantityField.textProperty().removeListener(quantityListener);
                                ScannerUtils.showError("Thông báo", "Vui lòng nhập số lượng từ 1 trở lên!");
                                quantityField.setText(oldVal);
                                quantityField.textProperty().addListener(quantityListener);
                            } else if (value > available) {
                                quantityField.textProperty().removeListener(quantityListener);
                                ScannerUtils.showError("Thông báo", "Số lượng mượn không được vượt quá " + available + "!");
                                quantityField.setText(oldVal);
                                quantityField.textProperty().addListener(quantityListener);
                            } else {
                                device.setQuantityToBorrow(value);
                            }
                        } catch (NumberFormatException e) {
                            quantityField.textProperty().removeListener(quantityListener);
                            ScannerUtils.showError("Lỗi", "Vui lòng chỉ nhập số nguyên!");
                            quantityField.setText(oldVal);
                            quantityField.textProperty().addListener(quantityListener);
                            device.setQuantityToBorrow(0);
                        }
                    };

                    quantityField.setPrefWidth(80);
                    quantityField.setPromptText("Nhập số lượng");
                    quantityField.textProperty().addListener(quantityListener);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        Device device = getTableView().getItems().get(getIndex());

                        quantityField.textProperty().removeListener(quantityListener);
                        quantityField.setPromptText("Nhập số lượng");
                        quantityField.textProperty().addListener(quantityListener);

                        quantityField.disableProperty().bind(device.selectedForBorrowProperty().not());

                        setGraphic(quantityField);
                    }
                }
            });



            colBorrow.setCellFactory(tc -> new TableCell<Device, Boolean>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOnAction(e -> {
                        Device device = getTableView().getItems().get(getIndex());
                        device.setSelectedForBorrow(checkBox.isSelected());
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Device device = getTableView().getItems().get(getIndex());
                        checkBox.setSelected(device.isSelectedForBorrow());
                        setGraphic(checkBox);
                    }
                }
            });

            tblDevice.setItems(FXCollections.observableArrayList(datas));
            tblDevice.setSelectionModel(null);
        }
    }
}
