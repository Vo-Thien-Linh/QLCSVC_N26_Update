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
            if (borrowRoomRepository.isRoomScheduleConflict(roomId, dateBorrow, startPeriod, endPeriod)) {
                ScannerUtils.showError("Lỗi", "Phòng này đã có người đăng ký trong khoảng thời gian này!");
                return;
            }

            BorrowRoom data = new BorrowRoom(0, roomId, null, UserSession.getUserId(), dateBorrow, startPeriod, endPeriod, reason,null, null, borrowDevices);
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
                                ScannerUtils.showError("Thông báo", "Số lượng mượn không được vượt quá số lượng có sẵn");
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
