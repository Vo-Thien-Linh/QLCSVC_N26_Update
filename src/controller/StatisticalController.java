package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;
import model.Device;
import model.DeviceStatus;
import model.Room;
import model.UsageStat;
import repository.StatisticalRepository;

import java.net.URL;
import java.util.*;

public class StatisticalController implements Initializable {

    @FXML
    private PieChart pieChartTinhTrang;

    @FXML
    private BarChart<String, Number> barChartPhongHoc;

    @FXML
    private CategoryAxis xAxisPhongHoc;

    @FXML
    private NumberAxis yAxisPhongHoc;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private ComboBox<Integer> yearComboBox;

    @FXML
    private ComboBox<String> roomComboBox;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Label lblDaXuLy;

    @FXML
    private Label lblChuaXuLy;

    @FXML
    private Label lblThietBiNhieu;

    @FXML
    private Label lblTongYeuCauBottom;

    private StatisticalRepository statisticalRepository = new StatisticalRepository();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadPieChart();
        setupBarChart();
        setupLabels();
        loadRoomData();

        yearComboBox.getItems().addAll(2024, 2025, 2026);
        yearComboBox.setValue(2025);

        roomComboBox.setOnAction(e -> loadLineChart(yearComboBox.getValue(), roomComboBox.getValue()));

        yearComboBox.setOnAction(e -> loadLineChart(yearComboBox.getValue(), roomComboBox.getValue()));
        loadLineChart(2025, roomComboBox.getValue());
    }

    private void loadRoomData() {
        if(roomComboBox != null){
            List<String> searchTypes = new ArrayList<>();
            searchTypes.add("Tất cả");
            ArrayList<Room> roomList = statisticalRepository.getAllRooms();
            for(Room room : roomList){
                searchTypes.add(room.getRoomNumber());
            }
            roomComboBox.setItems(FXCollections.observableArrayList(searchTypes));

            if (!roomComboBox.getItems().isEmpty()) {
                roomComboBox.getSelectionModel().selectFirst();
            }
        }
    }

    private void loadPieChart() {
        Map<DeviceStatus, Integer> datas = statisticalRepository.getDeviceStatusCount();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<DeviceStatus, Integer> entry : datas.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey().getLabel(), entry.getValue()));
        }

        double total = pieChartData.stream().mapToDouble(PieChart.Data::getPieValue).sum();

        // Gắn nhãn có phần trăm
        for (PieChart.Data data : pieChartData) {
            double percentage = (data.getPieValue() / total) * 100;
            String label = String.format("%s (%.1f%%)", data.getName(), percentage);
            data.setName(label);
        }

        pieChartTinhTrang.setData(pieChartData);
        pieChartTinhTrang.setLegendVisible(false);

        // Thiết lập màu sắc cho từng phần
        pieChartData.get(0).getNode().setStyle("-fx-pie-color: #4F8DD5;");
        pieChartData.get(1).getNode().setStyle("-fx-pie-color: #F5A623;");
        pieChartData.get(2).getNode().setStyle("-fx-pie-color: #D0021B;");
    }

    private void setupBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        List<Map<String, Object>> datas = statisticalRepository.graphDataDump();

        int maxCount = 0;
        for (Map<String, Object> row : datas) {
            String roomName = (String) "Phòng " + row.get("room_number");
            int count = ((int) row.get("total_quantity"));
            if(count > maxCount){
                maxCount = count;
            }
            series.getData().add(new XYChart.Data<>(roomName, count));
        }

        barChartPhongHoc.getData().add(series);
        barChartPhongHoc.setLegendVisible(false);

        // Thiết lập màu xanh cho các cột
        for (XYChart.Data<String, Number> item : series.getData()) {
            item.getNode().setStyle("-fx-bar-fill: #1273E0");
            item.getNode().getStyleClass().add("custom-bar");

            // Add hover effects
            item.getNode().setOnMouseEntered(event -> {
                item.getNode().setStyle("-fx-bar-fill: #0E59AD;");
                String tooltipText = item.getXValue() + " có " + item.getYValue() + " thiết bị";
                Tooltip tooltip = new Tooltip(tooltipText);
                Tooltip.install(item.getNode(), tooltip);
            });

            item.getNode().setOnMouseExited(event -> {
                item.getNode().setStyle("-fx-bar-fill: #1273E0;");
            });
        }

        NumberAxis yAxis = (NumberAxis) barChartPhongHoc.getYAxis();

        int numColumns = series.getData().size();
        double minWidth = numColumns * 90;
        barChartPhongHoc.setMinWidth(minWidth);
        barChartPhongHoc.setMaxWidth(minWidth);
        yAxis.setUpperBound(maxCount + 1);

        // Thiết lập trục
        xAxisPhongHoc.setLabel("Phòng học");
        yAxisPhongHoc.setLabel("Số lượng");
        yAxisPhongHoc.setLowerBound(0);
        yAxisPhongHoc.setUpperBound(10);
        yAxisPhongHoc.setTickUnit(2);
    }

    private void loadLineChart(int year, String roomNumber) {
        lineChart.getData().clear();
        xAxis.getCategories().clear();

        List<String> months = List.of("01","02","03","04","05","06","07","08","09","10","11","12");
        xAxis.setCategories(FXCollections.observableArrayList(months));

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setTickUnit(1);

        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                if (object == null) return "";
                return String.format("%d", object.intValue());
            }
            @Override
            public Number fromString(String string) {
                return Integer.parseInt(string);
            }
        });

        List<UsageStat> stats = statisticalRepository.getUsageStatsByYear(year, roomNumber); // chú ý thứ tự tham số

        // Map thiết bị -> tháng -> count
        Map<String, Map<String, Integer>> deviceMonthCountMap = new HashMap<>();

        int maxCount = 0;

        // Gom dữ liệu lại
        for (UsageStat stat : stats) {
            String device = stat.getDeviceName();
            String month = stat.getUsageMonth().substring(5); // lấy tháng dạng "01", "02", ...
            int count = (int) stat.getUsageCount();

            deviceMonthCountMap.putIfAbsent(device, new HashMap<>());
            deviceMonthCountMap.get(device).put(month, count);

            if (count > maxCount) {
                maxCount = count;
            }
        }

        // Tạo series cho từng thiết bị, đủ 12 tháng, nếu thiếu thì cho count=0
        for (String device : deviceMonthCountMap.keySet()) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(device);

            Map<String, Integer> monthCount = deviceMonthCountMap.get(device);

            for (String month : months) {
                int count = monthCount.getOrDefault(month, 0);

                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(month, count);

                series.getData().add(dataPoint);

                // Gắn tooltip và style sau khi node có sẵn
                dataPoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        Tooltip.install(newNode, new Tooltip(device + " (" + year + "-" + month + "): " + count));
                    }
                });
            }
            lineChart.getData().add(series);
        }

        yAxis.setUpperBound(maxCount + 1);
    }



    private void setupLabels() {
        lblDaXuLy.setText("10");
        lblChuaXuLy.setText("5");
        lblThietBiNhieu.setText("Máy chiều");
        lblTongYeuCauBottom.setText("15");
    }

    // Phương thức để cập nhật dữ liệu
    public void updateData(int tongYeuCau, int daXuLy, int chuaXuLy, String thietBiNhieu) {
        lblDaXuLy.setText(String.valueOf(daXuLy));
        lblChuaXuLy.setText(String.valueOf(chuaXuLy));
        lblThietBiNhieu.setText(thietBiNhieu);
    }

    public void updatePieChart(double dangSuDung, double dangBaoTri, double daHong) {
        ObservableList<PieChart.Data> newData = FXCollections.observableArrayList(
                new PieChart.Data("Đang sử dụng", dangSuDung),
                new PieChart.Data("Đang bảo trì", dangBaoTri),
                new PieChart.Data("Đã hỏng", daHong)
        );

        pieChartTinhTrang.setData(newData);
    }

}
