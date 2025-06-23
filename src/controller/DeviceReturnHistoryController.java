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
import model.*;
import repository.ManagerDeviceRepository;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class DeviceReturnHistoryController implements Initializable {
    @FXML
    private TableView<DeviceReturnHistory> tblHistory;
    @FXML private TableColumn<DeviceReturnHistory, String> colBorrower, colDeviceName, colBorrowDate, colReturnDate, colPurpose, colReturnNote, colBorrowTime, colBorrowQuantity, colReturnQuantity;

    private ManagerDeviceRepository  managerDeviceRepository =  new ManagerDeviceRepository();
    public void initialize(URL location, ResourceBundle resources){
        loadReturnHistory();
    }

    private  void loadReturnHistory(){
        List<DeviceReturnHistory> datas = managerDeviceRepository.getReturnHistory();

        colBorrower.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowDevice().getBorrower().getFullname()));
        colDeviceName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowDevice().getBorrowDeviceDetail().getDevice().getDeviceName()));
        colBorrowQuantity.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getBorrowDevice().getBorrowDeviceDetail().getQuantity())));
        colReturnQuantity.setCellValueFactory(new PropertyValueFactory<>("returnQuantity"));
        colBorrowDate.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(String.valueOf(cellData.getValue().getBorrowDevice().getBorrowDate())));
        colReturnDate.setCellValueFactory(cellData -> {
            BorrowDevice borrowDevice = cellData.getValue().getBorrowDevice();
            LocalDateTime dateTime = borrowDevice.getCreatedAt();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            return new ReadOnlyStringWrapper(dateTime.format(formatter));
        });
        colBorrowTime.setCellValueFactory(cellData -> {
            BorrowDevice borrowDevice = cellData.getValue().getBorrowDevice();
            int start = borrowDevice.getStartPeriod();
            int end = borrowDevice.getEndPeriod();

            String hourRange = start + "-" + end;
            return new ReadOnlyStringWrapper(hourRange);
        });
        colPurpose.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getBorrowDevice().getBorrowReason()));
        colReturnNote.setCellValueFactory(new PropertyValueFactory<>("conditionNote"));
        tblHistory.setSelectionModel(null);
        tblHistory.setItems(FXCollections.observableArrayList(datas));
    }
}
