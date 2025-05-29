package model;

public class UsageStat {
    private String deviceName;
    private String usageMonth;
    private int usageCount;

    public UsageStat(String deviceName, String usageMonth, int usageCount) {
        this.deviceName = deviceName;
        this.usageMonth = usageMonth;
        this.usageCount = usageCount;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getUsageMonth() {
        return usageMonth;
    }

    public int getUsageCount() {
        return usageCount;
    }
}
