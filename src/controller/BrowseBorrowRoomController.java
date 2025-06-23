package controller;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.BorrowDevice;
import model.BorrowDeviceDetail;
import model.BorrowRoom;
import model.Room;
import repository.BorrowRoomRepository;
import repository.ManagerRoomRepository;
import utils.ScannerUtils;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BrowseBorrowRoomController implements Initializable {
    @FXML private TableView<BorrowRoom> tblBorrowBrowse;
    @FXML private TableColumn<BorrowRoom, String> colId, colBorrower, colRoomName, colDeviceList, colDateSent, colBorrowDate, colHour, colPurpose, colStatus;
    @FXML private TableColumn<BorrowRoom, Void> colAction;

    private ManagerRoomRepository managerRoomRepository = new  ManagerRoomRepository();
    public void initialize(URL location, ResourceBundle resources) {
        loadDataBrowse();
    }

    private void loadDataBrowse(){
        List<BorrowRoom> datas = managerRoomRepository.getDataBorrowRooms();

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBorrower.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrower().getFullname()) {});
        colRoomName.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        colDeviceList.setCellValueFactory(cellData -> {
            BorrowRoom borrowRoom = cellData.getValue();
            List<BorrowDeviceDetail> detailList = borrowRoom.getBorrowDeviceDetail();

            if (detailList == null || detailList.isEmpty()) {
                return new ReadOnlyStringWrapper("Không có");
            }

            StringBuilder listDevice = new StringBuilder();
            for (int i = 0; i < detailList.size(); i++) {
                BorrowDeviceDetail detail = detailList.get(i);
                listDevice.append(detail.getDevice().getDeviceName())
                        .append("(")
                        .append(detail.getQuantity())
                        .append(")");
                if (i < detailList.size() - 1) {
                    listDevice.append(", ");
                }
            }

            return new ReadOnlyStringWrapper(listDevice.toString());
        });

        colDeviceList.setCellFactory(column -> {
            return new TableCell<BorrowRoom, String>() {
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

        colBorrowDate.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        colDateSent.setCellValueFactory(cellData -> {
            BorrowRoom borrowRoom = cellData.getValue();
            LocalDateTime dateTime = borrowRoom.getCreatedAt();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return new ReadOnlyStringWrapper(dateTime.format(formatter));
        });
        colHour.setCellValueFactory(cellData -> {
            BorrowRoom borrowRoom = cellData.getValue();
            int start = borrowRoom.getstartPeriod();
            int end = borrowRoom.getEndPeriod();

            String hourRange = start + "-" + end;
            return new ReadOnlyStringWrapper(hourRange);
        });
        colPurpose.setCellValueFactory(cellData -> {
            BorrowRoom borrowRoom = cellData.getValue();
            String borrowReason = borrowRoom.getBorrowReason();
            if(borrowReason == null || borrowReason.equals("")){
                return new ReadOnlyStringWrapper("Không có");
            }

            return  new ReadOnlyStringWrapper(borrowReason);
        });
        colPurpose.setCellFactory(column -> {
            return new TableCell<BorrowRoom, String>() {
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
        colStatus.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));
        colAction.setCellFactory(new Callback<TableColumn<BorrowRoom, Void>, TableCell<BorrowRoom, Void>>() {
            @Override
            public TableCell<BorrowRoom, Void> call(final TableColumn<BorrowRoom, Void> param) {
                return new TableCell<BorrowRoom, Void>() {
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
                                BorrowRoom borrowRoom = getTableView().getItems().get(getIndex());

                                Boolean success = managerRoomRepository.approveRequest(borrowRoom.getId());
                                if(success){
                                    ScannerUtils.showInfo("Thông báo", "Đã duyệt yêu cầu");
                                    loadDataBrowse();
                                } else {
                                    ScannerUtils.showError("Thông báo", "Duyệt yêu cầu thất bại!");
                                }
                            }

                        });

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

                                    BorrowRoom borrowRoom = getTableView().getItems().get(getIndex());
                                    Boolean success =  managerRoomRepository.refuseRequest(borrowRoom.getId(), reason);
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
