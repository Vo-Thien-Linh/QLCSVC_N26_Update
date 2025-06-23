package controller;

import javafx.animation.ScaleTransition;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import model.Device;
import model.DeviceStatus;
import repository.DashboardRepository;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Rectangle beforeLayer;
    @FXML private BarChart<String, Number> deviceUsageChart;
    @FXML private Label quantityDevice;
    @FXML private Label quantityDeviceMaintenance;
    @FXML private Label quantityDeviceUnvailable;
    @FXML private ComboBox<String> cboSearchType;
    @FXML private TableColumn<Device, String> idColumn, nameColumn, roomColumn, statusColumn;
    @FXML private TableColumn<Device, Integer> quantityColumn;
    @FXML private TableView<Device> deviceTable;
    @FXML private Pagination dashboard_pagination;
    @FXML private StackPane searchButton;

    private String roomNumber = null;
    private String keyword = null;
    private int limitItem = 8;

    private DashboardRepository dashboardRepository= new DashboardRepository();

    private void setQuantityDevice(){
        int quantity= dashboardRepository.countDevices();
        quantityDevice.setText(String.valueOf(quantity));
    }

    private void setQuantityDeviceMaintenance(){
        int quantity= dashboardRepository.countDevicesMaintenance();
        quantityDeviceMaintenance.setText(String.valueOf(quantity));
    }

    private void setQuantityDeviceUnavailable(){
        int quantity= dashboardRepository.countDevicesUnavailable();
        quantityDeviceUnvailable.setText(String.valueOf(quantity));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSearchButton();
        graphDataDump();
        setQuantityDevice();
        setQuantityDeviceMaintenance();
        setQuantityDeviceUnavailable();
        dataDumpCbo();

        cboSearchType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            roomNumber = newVal;
            loadPage(0);
        });

        searchButton.setOnMouseClicked(event -> {
            keyword = searchField.getText();
            loadPage(0);
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            keyword = newValue.trim();
            loadPage(0);
        });

        dashboard_pagination.setPageFactory(pageIndex -> {
            loadPage(pageIndex);
            return new VBox();
        });
    }

    private void setupSearchButton() {
        // Animation for search button
        ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(0.3), beforeLayer);
        scaleIn.setFromX(0);
        scaleIn.setToX(1);

        ScaleTransition scaleOut = new ScaleTransition(Duration.seconds(0.3), beforeLayer);
        scaleOut.setFromX(1);
        scaleOut.setToX(0);

        if(searchButton != null){
            searchButton.setOnMouseEntered(e -> scaleIn.playFromStart());
            searchButton.setOnMouseExited(e -> scaleOut.playFromStart());
        }
    }


    private void dataDumpCbo() {
        if (cboSearchType != null) {
            List<String> searchTypes = new ArrayList<>();
            searchTypes.add("Tất cả");
            List<String> listRooms = dashboardRepository.getRoomAll();
            for (String room : listRooms) {
                searchTypes.add(room);
            }
            cboSearchType.setItems(FXCollections.observableArrayList(searchTypes));

            if (!cboSearchType.getItems().isEmpty()) {
                cboSearchType.getSelectionModel().selectFirst();
            }
        }
    }

    private void graphDataDump() {
        deviceUsageChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        int maxCount = 0;
        List<Map<String, Object>> datas = dashboardRepository.graphDataDump();
        for (Map<String, Object> row : datas) {
            String roomName = (String) row.get("device_name");
            int count = ((int) row.get("total_quantity"));
            if(count > maxCount){
                maxCount = count;
            }
            series.getData().add(new XYChart.Data<>(roomName, count));
        }

        deviceUsageChart.getData().add(series);
//        deviceUsageChart.setBarGap(5);

        // Style and add tooltips to bars
        for (XYChart.Data<String, Number> item : series.getData()) {
            item.getNode().setStyle("-fx-bar-fill: #1273E0");
            item.getNode().getStyleClass().add("custom-bar");

            // Add hover effects
            item.getNode().setOnMouseEntered(event -> {
                item.getNode().setStyle("-fx-bar-fill: #0E59AD;");
                String tooltipText = "Loại thiết bị: " + item.getXValue() + "\nSố lượng: " + item.getYValue();;
                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(item.getNode(), tooltip);
            });

            item.getNode().setOnMouseExited(event -> {
                item.getNode().setStyle("-fx-bar-fill: #1273E0;");
            });
        }

        NumberAxis yAxis = (NumberAxis) deviceUsageChart.getYAxis();

        int numColumns = series.getData().size();
        double minWidth = numColumns * 80;
        deviceUsageChart.setMinWidth(minWidth);
        deviceUsageChart.setMaxWidth(minWidth);
        yAxis.setUpperBound(maxCount + 1);
    }

    private void loadPage(int pageIndex) {
        int skip = pageIndex * limitItem;
        List<Device> devices = dashboardRepository.filterAndSearch(roomNumber, keyword, limitItem, skip);
        int totalPages = (int) Math.ceil((double) dashboardRepository.countDevices(roomNumber, keyword) / limitItem);
        dashboard_pagination.setPageCount(totalPages);
        loadDeviceData(devices);
    }

    public void loadDeviceData(List<Device> devices) {
        if(idColumn != null && nameColumn != null && roomColumn != null && quantityColumn != null && deviceTable != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
            statusColumn.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString()));
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            roomColumn.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(cellData.getValue().getRoom().getRoomNumber()));
            deviceTable.setItems(FXCollections.observableArrayList(devices));
            deviceTable.setSelectionModel(null);
        }
    }
}