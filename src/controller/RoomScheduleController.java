package controller;

import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import model.BorrowRoom;
import model.User;
import repository.BorrowRoomRepository;

public class RoomScheduleController implements Initializable {
    @FXML private VBox rootPane;
    @FXML private Label lblWeekRange;
    @FXML private Button btnPreviousWeek, btnNextWeek, btnClose;
    @FXML private TableView<ScheduleRow> tblSchedule;
    @FXML private TableColumn<ScheduleRow, String> colPeriod, colMonday, colTuesday, colWednesday, colThursday, colFriday, colSaturday, colSunday;

    private BorrowRoomRepository borrowRoomRepository = new BorrowRoomRepository();
    private String roomId;
    private LocalDate startOfWeek;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupButtons();
        startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        loadSchedule();
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
        loadSchedule();
    }

    private void setupTableColumns() {
        colPeriod.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPeriod()));
        colMonday.setCellValueFactory(cellData -> createCellContent(cellData, 0));
        colTuesday.setCellValueFactory(cellData -> createCellContent(cellData, 1));
        colWednesday.setCellValueFactory(cellData -> createCellContent(cellData, 2));
        colThursday.setCellValueFactory(cellData -> createCellContent(cellData, 3));
        colFriday.setCellValueFactory(cellData -> createCellContent(cellData, 4));
        colSaturday.setCellValueFactory(cellData -> createCellContent(cellData, 5));
        colSunday.setCellValueFactory(cellData -> createCellContent(cellData, 6));
    }

    private SimpleStringProperty createCellContent(TableColumn.CellDataFeatures<ScheduleRow, String> cellData, int dayIndex) {
        ScheduleRow row = cellData.getValue();
        BorrowRoom borrowRoom = row.getBorrowings().get(dayIndex);
        if (borrowRoom != null) {
            return new SimpleStringProperty(String.format("%s\nTiết %d-%d\n(%s)",
                    borrowRoom.getBorrower().getFullname() != null ? borrowRoom.getBorrower().getFullname() : "Unknown",
                    borrowRoom.getstartPeriod(),
                    borrowRoom.getEndPeriod(),
                    borrowRoom.getStatus().toString()));
        }
        return new SimpleStringProperty("");
    }

    private void setupButtons() {
        btnPreviousWeek.setOnAction(event -> {
            startOfWeek = startOfWeek.minusWeeks(1);
            loadSchedule();
        });

        btnNextWeek.setOnAction(event -> {
            startOfWeek = startOfWeek.plusWeeks(1);
            loadSchedule();
        });

        btnClose.setOnAction(event -> {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();
        });
    }

    private void loadSchedule() {
        lblWeekRange.setText(String.format("Tuần: %s - %s",
                startOfWeek.format(DATE_FORMATTER),
                startOfWeek.plusDays(6).format(DATE_FORMATTER)));

        List<ScheduleRow> rows = new ArrayList<>();
        // Thêm hàng nhóm ca học
        rows.add(new ScheduleRow("Ca Sáng"));
        for (int i = 1; i <= 5; i++) {
            rows.add(new ScheduleRow("Tiết " + i));
        }
        rows.add(new ScheduleRow("Ca Chiều"));
        for (int i = 6; i <= 10; i++) {
            rows.add(new ScheduleRow("Tiết " + i));
        }
        rows.add(new ScheduleRow("Ca Tối"));
        for (int i = 11; i <= 13; i++) {
            rows.add(new ScheduleRow("Tiết " + i));
        }

        if (roomId != null) {
            List<BorrowRoom> borrowings = borrowRoomRepository.getBorrowedRooms()
                    .stream()
                    .filter(r -> r.getRoomId() != null && r.getRoomId().equals(roomId))
                    .filter(r -> {
                        LocalDate borrowDate = r.getBorrowDate();
                        if (borrowDate == null) return false;
                        return (borrowDate.isEqual(startOfWeek) ||
                                (borrowDate.isAfter(startOfWeek) &&
                                        borrowDate.isBefore(startOfWeek.plusDays(7))) ||
                                borrowDate.isEqual(startOfWeek.plusDays(6)));
                    })
                    .toList();

            for (BorrowRoom br : borrowings) {
                if (br.getBorrowDate() == null || br.getstartPeriod() == 0 || br.getEndPeriod() == 0) {
                    continue;
                }
                int startPeriod = br.getstartPeriod();
                int endPeriod = br.getEndPeriod();
                int dayIndex = br.getBorrowDate().getDayOfWeek().getValue() - 2; // Thứ 2 = 0, CN = 6
                if (dayIndex >= 0 && dayIndex < 7) {
                    // Ánh xạ mượn phòng đến từng tiết trong khoảng startPeriod đến endPeriod
                    for (int period = startPeriod; period <= endPeriod; period++) {
                        if (period >= 1 && period <= 13) {
                            int rowIndex = getRowIndexForPeriod(period); // Lấy chỉ số hàng tương ứng
                            if (rowIndex >= 0) {
                                rows.get(rowIndex).getBorrowings().set(dayIndex, br);
                            }
                        }
                    }
                }
            }
        }

        tblSchedule.setItems(FXCollections.observableArrayList(rows));
    }

    private int getRowIndexForPeriod(int period) {
        if (period >= 1 && period <= 5) return period; // Tiết 1-5 sau "Ca Sáng" (hàng 1)
        if (period >= 6 && period <= 10) return period + 1; // Tiết 6-10 sau "Ca Chiều" (hàng 7)
        if (period >= 11 && period <= 13) return period + 2; // Tiết 11-13 sau "Ca Tối" (hàng 12)
        return -1;
    }

    private static class ScheduleRow {
        private final String period;
        private final List<BorrowRoom> borrowings;

        public ScheduleRow(String period) {
            this.period = period;
            this.borrowings = new ArrayList<>(7);
            for (int i = 0; i < 7; i++) {
                borrowings.add(null);
            }
        }

        public String getPeriod() {
            return period;
        }

        public List<BorrowRoom> getBorrowings() {
            return borrowings;
        }
    }
}