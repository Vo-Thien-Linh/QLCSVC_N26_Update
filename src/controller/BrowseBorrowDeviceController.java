package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import model.BorrowDevice;
import model.BorrowDeviceDetail;
import model.BorrowRoom;
import repository.ManagerDeviceRepository;
import repository.ManagerRoomRepository;
import utils.ScannerUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BrowseBorrowDeviceController implements Initializable {
    @FXML
    private TableView<BorrowDevice> tblBorrowBrowse;
    @FXML private TableColumn<BorrowDevice, String> colId, colBorrower, colRoomName, colBorrowQuantity, colDateSent, colBorrowDate, colHour, colPurpose, colStatus;
    @FXML private TableColumn<BorrowDevice, Void> colAction;

    private ManagerDeviceRepository managerDeviceRepository = new ManagerDeviceRepository();
    public void initialize(URL location, ResourceBundle resources) {
        loadDataBrowse();
    }

    private void loadDataBrowse(){
        List<BorrowDevice> datas = managerDeviceRepository.getDataBorrowDevices();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBorrower.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrower().getFullname()) {});
        colRoomName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowDeviceDetail().getDevice().getDeviceName()));
        colBorrowQuantity.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getBorrowDeviceDetail().getQuantity())));

        colBorrowDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        colDateSent.setCellValueFactory(cellData -> {
            BorrowDevice borrowDevice = cellData.getValue();
            LocalDateTime dateTime = borrowDevice.getCreatedAt();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return new ReadOnlyStringWrapper(dateTime.format(formatter));
        });

        colHour.setCellValueFactory(cellData -> {
            BorrowDevice borrowDevice = cellData.getValue();
            int start = borrowDevice.getStartPeriod();
            int end = borrowDevice.getEndPeriod();

            String hourRange = start + "-" + end;
            return new ReadOnlyStringWrapper(hourRange);
        });
        colPurpose.setCellValueFactory(cellData -> {
            BorrowDevice borrowDevice = cellData.getValue();
            String borrowReason = borrowDevice.getBorrowReason();
            if(borrowReason == null || borrowReason.equals("")){
                return new ReadOnlyStringWrapper("Không có");
            }

            return  new ReadOnlyStringWrapper(borrowReason);
        });
        colPurpose.setCellFactory(column -> {
            return new TableCell<BorrowDevice, String>() {
                private final Text text = new Text();

                {
                    text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
                    text.setLineSpacing(2);
                    setGraphic(text);
                    setAlignment(Pos.CENTER);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText("");
                    } else {
                        text.setText(item);
                    }
                }
            };
        });
        colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowStatus().toString()));
        colAction.setCellFactory(new Callback<TableColumn<BorrowDevice, Void>, TableCell<BorrowDevice, Void>>() {
            @Override
            public TableCell<BorrowDevice, Void> call(final TableColumn<BorrowDevice, Void> param) {
                return new TableCell<BorrowDevice, Void>() {
                    private final Button browseButton = new Button("Duyệt");
                    private final Button refuseButton = new Button("Từ chối");
                    private final HBox actionBox = new HBox(10);


                    {
                        browseButton.getStyleClass().add("browseButton");
                        refuseButton.getStyleClass().add("refuseButton");

                        actionBox.setAlignment(Pos.CENTER);
                        actionBox.getChildren().addAll(browseButton, refuseButton);

                        browseButton.setOnAction(event -> {
                        if(ScannerUtils.showConfirm("Xác nhận", "Bạn có chắc chắn muốn duyệt yêu cầu này không!")){
                            BorrowDevice borrowDevice = getTableView().getItems().get(getIndex());

                            Boolean success = managerDeviceRepository.approveRequest(borrowDevice.getId());
                            if(success){
                                ScannerUtils.showInfo("Thông báo", "Đã duyệt yêu cầu");
                                loadDataBrowse();
                            } else {
                                ScannerUtils.showError("Thông báo", "Duyệt yêu cầu thất bại!");
                            }
                        }

                    });
//
                        refuseButton.setOnAction(event -> {
                            Dialog<String> dialog = new Dialog<>();
                            dialog.setTitle("Lý do từ chối");
                            dialog.setHeaderText("Nhập lý do từ chối yêu cầu mượn:");

                            ButtonType okButtonType = new ButtonType("Xác nhận", ButtonBar.ButtonData.OK_DONE);
                            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

                            TextArea textArea = new TextArea();
                            textArea.setPromptText("Nhập lý do tại đây...");
                            textArea.setWrapText(true);
                            textArea.setPrefRowCount(4);

                            dialog.getDialogPane().setContent(textArea);

                            dialog.setResultConverter(dialogButton -> {
                                if (dialogButton == okButtonType) {
                                    return textArea.getText();
                                }
                                return null;
                            });

                            Optional<String> result = dialog.showAndWait();
                            result.ifPresent(reason -> {
                                if(ScannerUtils.showConfirm("Xác nhận", "Bạn có chắc chắn muốn từ chồi yêu cầu này không")){
                                    if (reason.trim().isEmpty()) {
                                        ScannerUtils.showError("Thông báo", "Vui lòng nhập lý do từ chối");
                                        return;
                                    }

                                    BorrowDevice borrowDevice = getTableView().getItems().get(getIndex());
                                    Boolean success =  managerDeviceRepository.refuseRequest(borrowDevice.getId(), reason);
                                    if(success){
                                        loadDataBrowse();
                                        ScannerUtils.showConfirm("Thông báo", "Đã từ chối yêu cầu");
                                    } else {
                                        ScannerUtils.showError("Thông báo", "Từ chối thất bại");
                                    }
                                }

                            });
                        });
                    }


                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(actionBox);
                        }
                    }
                };
            }
        });
        tblBorrowBrowse.setItems(FXCollections.observableArrayList(datas));
        tblBorrowBrowse.setSelectionModel(null);
        tblBorrowBrowse.setFixedCellSize(60);
    }
}
