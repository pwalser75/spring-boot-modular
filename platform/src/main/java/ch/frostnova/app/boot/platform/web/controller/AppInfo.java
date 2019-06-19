package ch.frostnova.app.boot.platform.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * App Info DTO
 *
 * @author pwalser
 * @since 19.06.2019
 */
@JsonPropertyOrder({"name", "description", "version", "cpu", "memory"})
public class AppInfo {

    private final static NumberFormat PERCENT_FORMAT = new DecimalFormat("0.00%");
    private final static NumberFormat NUMBER_FORMAT = new DecimalFormat("0.##");

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("version")
    private String version;

    @JsonProperty("cpu")
    private final Cpu cpu = new Cpu();

    @JsonProperty("memory")
    private final Memory memory = new Memory();

    public Cpu getCpu() {
        return cpu;
    }

    public Memory getMemory() {
        return memory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public static class Cpu {

        @JsonIgnore
        private double usage;

        public double getUsage() {
            return usage;
        }

        public void setUsage(double usage) {
            this.usage = usage;
        }

        @JsonProperty("usage")
        public String getUsagePercent() {
            return PERCENT_FORMAT.format(usage);
        }
    }

    @JsonPropertyOrder({"used", "allocated", "usage"})
    public static class Memory {

        @JsonIgnore
        private long used;

        @JsonIgnore
        private long allocated;

        @JsonIgnore
        private double getUsage() {
            if (allocated == 0) {
                return 0;
            }
            return (double) used / (double) allocated;
        }

        public long getUsed() {
            return used;
        }

        public void setUsed(long used) {
            this.used = used;
        }

        public long getAllocated() {
            return allocated;
        }

        public void setAllocated(long allocated) {
            this.allocated = allocated;
        }

        @JsonProperty("usage")
        public String getUsagePercent() {
            return PERCENT_FORMAT.format(getUsage());
        }

        @JsonProperty("used")
        public String getUsedDisplay() {
            return formatMemory(used);
        }

        @JsonProperty("allocated")
        public String getAllocatedDisplay() {
            return formatMemory(allocated);
        }
    }

    private static String formatMemory(long memory) {

        if (memory < 0) {
            return "-" + formatMemory(-memory);
        }

        String[] units = {"Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

        double value = (double) memory;
        for (int i = 0; i < units.length; i++) {
            if (value <= 1024) {
                return NUMBER_FORMAT.format(value) + units[i];
            }
            value /= 1024;
        }
        return value + "YB";
    }
}
