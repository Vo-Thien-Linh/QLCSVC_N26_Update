package model;

import java.time.LocalDate;

public class WeekItem {
    private final int weekNumber;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public WeekItem(int weekNumber, LocalDate startDate, LocalDate endDate) {
        this.weekNumber = weekNumber;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        return String.format("Từ %d/%d/%d - %d/%d/%d",
                startDate.getDayOfMonth(), startDate.getMonthValue(), startDate.getYear(),
                endDate.getDayOfMonth(), endDate.getMonthValue(), endDate.getYear());
    }
}

