package model;

public class Schedule {
    private final String shiftName;
    private final BorrowRoom[] daySlots; // 7 ô từ thứ 2 → CN

    public Schedule(String shiftName, BorrowRoom[] daySlots) {
        this.shiftName = shiftName;
        this.daySlots = daySlots;
    }

    public String getShiftName() {
        return shiftName;
    }

    public BorrowRoom getDay(int index) {
        return daySlots[index];
    }
}
