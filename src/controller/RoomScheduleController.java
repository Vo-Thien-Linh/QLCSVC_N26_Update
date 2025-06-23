package controller;


import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import model.BorrowRoom;
import model.Schedule;
import model.WeekItem;
import repository.BorrowRoomRepository;

import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.util.*;

public class RoomScheduleController implements Initializable {

    @FXML private ComboBox<WeekItem> weekComboBox;
    @FXML private Button currentWeekButton;
    @FXML private Label weekInfoLabel;
    @FXML private TableView<Schedule> scheduleTable;
    @FXML private TableColumn<Schedule, String> shiftColumn;
    @FXML private TableColumn<Schedule, BorrowRoom> mondayColumn;
//    @FXML private TableColumn<ScheduleRow, String> tuesdayColumn;
//    @FXML private TableColumn<ScheduleRow, String> wednesdayColumn;
//    @FXML private TableColumn<ScheduleRow, String> thursdayColumn;
//    @FXML private TableColumn<ScheduleRow, String> fridayColumn;
//    @FXML private TableColumn<ScheduleRow, String> saturdayColumn;
//    @FXML private TableColumn<ScheduleRow, String> sundayColumn;
    @FXML private Button addEventButton;
    @FXML private Button editEventButton;
    @FXML private Button deleteEventButton;
    @FXML private Button exportButton;

    private BorrowRoomRepository borrowRoomRepository = new BorrowRoomRepository();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadCombox();
    }

    private int getWeeksInYear(int year) {
        return LocalDate.of(year, 12, 28).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }

    private LocalDate getStartOfWeek(int week, int year) {
        return LocalDate.ofYearDay(year, 1)
                .with(IsoFields.WEEK_OF_WEEK_BASED_YEAR, week)
                .with(ChronoField.DAY_OF_WEEK, 1); // Monday
    }



    private void loadCombox(){
        int currentYear = LocalDate.now().getYear();
        int currentWeek = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int totalWeeks = getWeeksInYear(currentYear);

        ObservableList<WeekItem> weekItems = FXCollections.observableArrayList();
        for (int i = 1; i <= totalWeeks; i++) {
            LocalDate start = getStartOfWeek(i, currentYear);
            LocalDate end = start.plusDays(6);
            weekItems.add(new WeekItem(i, start, end));
        }

        weekComboBox.setItems(weekItems);
        WeekItem currentWeekItem = weekItems.stream()
                .filter(w -> w.getWeekNumber() == currentWeek)
                .findFirst().orElse(null);

        weekComboBox.setValue(currentWeekItem);
        if (currentWeekItem != null) {
            weekInfoLabel.setText("Tuần " + currentWeekItem.getWeekNumber());
        }

        loadSchedule(weekComboBox.getValue());

        weekComboBox.setOnAction(e -> {
            WeekItem selected = weekComboBox.getValue();
            loadSchedule(selected);
            if (selected != null) {
                weekInfoLabel.setText("Tuần " + selected.getWeekNumber());
            }
        });
    }

    private int mapPeriodToShift(int startPeriod) {
        if (startPeriod >= 1 && startPeriod <= 5) return 0;
        if (startPeriod >= 6 && startPeriod <= 10) return 1;
        return 2;
    }

    private void loadSchedule(WeekItem selected) {
        List<BorrowRoom> datas = borrowRoomRepository.getBorrowedRoomsApproved();

        BorrowRoom[][] cellTable = new BorrowRoom[3][7];

        LocalDate[] weekDates = new LocalDate[7];

        for (int i = 0; i < 7; i++) {
            weekDates[i] = selected.getStartDate().plusDays(i);
        }

        for (BorrowRoom record : datas) {
            LocalDate borrowDate = record.getBorrowDate();

            int columnIndex = -1;
            for (int i = 0; i < 7; i++) {
                if (weekDates[i].equals(borrowDate)) {
                    columnIndex = i;
                    break;
                }
            }
            if (columnIndex == -1) continue;

            int rowIndex = mapPeriodToShift(record.getstartPeriod());
            if (rowIndex < 0 || rowIndex > 2) continue;

            cellTable[rowIndex][columnIndex] = record;
        }

        ObservableList<Schedule> rows = FXCollections.observableArrayList();
        rows.add(new Schedule("Sáng", cellTable[0]));
        rows.add(new Schedule("Chiều", cellTable[1]));
        rows.add(new Schedule("Tối", cellTable[2]));
        shiftColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getShiftName()));
        shiftColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        shiftColumn.setPrefWidth(100);
        shiftColumn.setMinWidth(100);
        shiftColumn.setMaxWidth(100);
        Platform.runLater(() -> {

            scheduleTable.setFixedCellSize(168.4);
            scheduleTable.setItems(rows);
            scheduleTable.setSelectionModel(null);
            scheduleTable.getColumns().clear();
            scheduleTable.getColumns().add(shiftColumn);
            scheduleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            scheduleTable.getColumns().addAll(
                    createDayColumn("Thứ 2", 0, weekDates[0]),
                    createDayColumn("Thứ 3", 1, weekDates[1]),
                    createDayColumn("Thứ 4", 2, weekDates[2]),
                    createDayColumn("Thứ 5", 3, weekDates[3]),
                    createDayColumn("Thứ 6", 4, weekDates[4]),
                    createDayColumn("Thứ 7", 5, weekDates[5]),
                    createDayColumn("Chủ nhật", 6, weekDates[6])
            );

            scheduleTable.refresh();
            scheduleTable.layout();
        });
    }

    private TableColumn<Schedule, BorrowRoom> createDayColumn(String title, int dayIndex, LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Label headerLabel = new Label(title + "\n(" + date.format(formatter) + ")");
        headerLabel.setStyle("-fx-text-alignment: center; -fx-alignment: center; -fx-font-weight: bold;");
        headerLabel.setWrapText(true);
        headerLabel.setMinHeight(50);

        TableColumn<Schedule, BorrowRoom> column = new TableColumn<>();
        column.setGraphic(headerLabel);

        column.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(cellData.getValue().getDay(dayIndex))
        );

        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BorrowRoom item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    VBox content = new VBox(3);
                    content.setStyle("-fx-padding: 5px;");

                    content.getChildren().addAll(
                            createTextFlow("Số phòng: ", item.getRoomNumber()),
                            createTextFlow("Người mượn: ", item.getBorrower().getFullname()),
                            createTextFlow("Số tiết: ", String.valueOf(item.getEndPeriod() - item.getstartPeriod() + 1)),
                            createTextFlow("Tiết: ", item.getstartPeriod() + "–" + item.getEndPeriod()),
                            createTextFlow("Mục đích: ", item.getBorrowReason())
                    );

                    setGraphic(content);
                    setText(null);
                }
            }
        });
        return column;
    }

    private TextFlow createTextFlow(String label, String value) {
        Text labelText = new Text(label);
        labelText.setStyle("-fx-font-weight: bold;");

        Text valueText = new Text(value);

        TextFlow flow = new TextFlow(labelText, valueText);
        flow.setStyle("-fx-padding: 1px;");
        return flow;
    }

}