package model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public abstract class Facilities {
    private String id;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public Facilities() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    public Facilities(String id, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.createdAt = (createdAt != null ? createdAt : LocalDate.now());
        this.updatedAt = (updatedAt != null ? updatedAt : LocalDate.now());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public abstract void informationDisplay();
}