package controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import model.BorrowDeviceDetail;
import model.BorrowRoom;
import model.RoomReturnHistory;
import repository.ManagerRoomRepository;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class RoomReturnHistoryController implements Initializable {
    @FXML
    private TableView<RoomReturnHistory> tblHistory;
    @FXML private TableColumn<RoomReturnHistory, String> colBorrower, colRoomName, colBorrowDate, colReturnDate, colPurpose, colDeviceList, colReturnNote, colBorrowTime;

    private ManagerRoomRepository managerRoomRepository = new ManagerRoomRepository();
    public void initialize(URL location, ResourceBundle resources){
        loadReturnHistory();
    }

    private void loadReturnHistory(){
        List<RoomReturnHistory> datas = managerRoomRepository.getRoomReturnHistory();
        colBorrower.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowRoom().getBorrower().getFullname()));
        colRoomName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowRoom().getRoomNumber()));
        colBorrowDate.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getBorrowRoom().getBorrowDate())));
        colReturnDate.setCellValueFactory(cellData -> {
            BorrowRoom borrowRoom = cellData.getValue().getBorrowRoom();
            LocalDateTime dateTime = borrowRoom.getCreatedAt();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return new ReadOnlyStringWrapper(dateTime.format(formatter));
        });
        colBorrowTime.setCellValueFactory(cellData -> {
            BorrowRoom borrowRoom = cellData.getValue().getBorrowRoom();
            int start = borrowRoom.getstartPeriod();
            int end = borrowRoom.getEndPeriod();

            String hourRange = start + "-" + end;
            return new ReadOnlyStringWrapper(hourRange);
        });
        colPurpose.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowRoom().getBorrowReason()));
        colDeviceList.setCellValueFactory(cellData -> {
            RoomReturnHistory roomReturnHistory = cellData.getValue();
            BorrowRoom borrowRoom = roomReturnHistory.getBorrowRoom();
            List<BorrowDeviceDetail> detailList = borrowRoom.getBorrowDeviceDetail();

            if (detailList == null || detailList.isEmpty()) {
                return new ReadOnlyStringWrapper("Không có");
            }

            StringBuilder listDevice = new StringBuilder();
            for (int i = 0; i < detailList.size(); i++) {
                BorrowDeviceDetail detail = detailList.get(i);
                listDevice.append(detail.getDevice().getDeviceName())
                        .append("(mượn: ")
                        .append(detail.getQuantity())
                        .append(" / trả: ")
                        .append(roomReturnHistory.getReturnQuantity())
                        .append(")");
                if (i < detailList.size() - 1) {
                    listDevice.append(", ");
                }
            }

            return new ReadOnlyStringWrapper(listDevice.toString());
        });

        colDeviceList.setCellFactory(column -> {
            return new TableCell<RoomReturnHistory, String>() {
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
        colReturnNote.setCellValueFactory(new PropertyValueFactory<>("conditionNote"));
        tblHistory.setSelectionModel(null);
        tblHistory.setItems(FXCollections.observableArrayList(datas));

    }
}
