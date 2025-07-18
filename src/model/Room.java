package model;

public class Room {
    private String id;
    private RoomStatus status;
    private String roomNumber;
    private int seatingCapacity;
    private String room_type;
    private String location;
    private boolean selected;
    private String maintainedBy;


    public Room() {}

    public Room(String id, String roomNumber, String room_type) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.room_type =  room_type;
    }

    public Room(String id, RoomStatus status, String roomNumber, int seatingCapacity, String room_type, String location) {
        this.id = id;
        this.status = status;
        this.roomNumber = roomNumber;
        this.seatingCapacity = seatingCapacity;
        this.room_type = room_type;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public String getRoom_type() {
        return room_type;
    }

    public void setRoom_type(String room_type) {
        this.room_type = room_type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getMaintainedBy() {
        return maintainedBy;
    }

    public void setMaintainedBy(String maintainedBy) {
        this.maintainedBy = maintainedBy;
    }
    public void informationDisplay() {
        System.out.println("Room ID: " + id);
        System.out.println("Status: " + status);
        System.out.println("Room Number: " + roomNumber);
        System.out.println("Seating Capacity: " + seatingCapacity);
    }

    @Override
    public String toString() {
        return roomNumber + " - " + room_type;
    }
}