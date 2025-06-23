package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import model.IncidentReport;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportHistoryController {

    @FXML private TableView<IncidentReport> tblReportHistory;
    @FXML private TableColumn<IncidentReport, String> colIdReport;
    @FXML private TableColumn<IncidentReport, String> colRoomNumber; // Thay: colRoomNumber đã đúng, giữ nguyên
    @FXML private TableColumn<IncidentReport, String> colReportDate;
    @FXML private TableColumn<IncidentReport, String> colDescription;
    @FXML private TableColumn<IncidentReport, String> colStatus;
    @FXML private Button btnClose;

    private List<IncidentReport> reports;

    @FXML
    private void initialize() {
        colIdReport.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIdReport()));
        colRoomNumber.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRoomNumber() != null ? cellData.getValue().getRoomNumber() : "")); // Thay: Sử dụng getRoomNumber
        colReportDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getReportDate() != null ? cellData.getValue().getReportDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
        colDescription.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusDisplayName()));

        if (reports != null) {
            tblReportHistory.setItems(FXCollections.observableArrayList(reports));
        } else {
            tblReportHistory.setPlaceholder(new Label("Không có báo cáo nào."));
        }

        btnClose.setOnAction(event -> handleClose());
    }

    public void setReports(List<IncidentReport> reports) {
        this.reports = reports;
        if (tblReportHistory != null) {
            tblReportHistory.setItems(FXCollections.observableArrayList(reports));
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}