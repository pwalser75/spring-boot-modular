package ch.frostnova.spring.boot.platform.web.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * App Info DTO
 *
 * @author pwalser
 * @since 19.06.2019
 */
@ApiModel("AppInfo")
@JsonPropertyOrder({"name", "description", "version", "cpu", "memory"})
public class AppInfo {

    private final static NumberFormat PERCENT_FORMAT = new DecimalFormat("0.00%");
    private final static NumberFormat NUMBER_FORMAT = new DecimalFormat("0.##");
    @JsonProperty("cpu")
    @ApiModelProperty(notes = "CPU usage information", position = 4)
    private final Cpu cpu = new Cpu();
    @JsonProperty("memory")
    @ApiModelProperty(notes = "Memory usage information", position = 4)
    private final Memory memory = new Memory();
    @JsonProperty("name")
    @ApiModelProperty(notes = "application name", example = "spring-multi-module", position = 1)
    private String name;
    @JsonProperty("description")
    @ApiModelProperty(notes = "application description", example = "Spring Boot Multi Module Project", position = 2)
    private String description;
    @JsonProperty("version")
    @ApiModelProperty(notes = "application version", example = "1.0.0-SNAPSHOT", position = 3)
    private String version;

    private static String formatMemory(long memory) {

        if (memory < 0) {
            return "-" + formatMemory(-memory);
        }

        String[] units = {"Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};

        double value = (double) memory;
        for (String unit : units) {
            if (value <= 1024) {
                return NUMBER_FORMAT.format(value) + unit;
            }
            value /= 1024;
        }
        return value + "YB";
    }

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

    @ApiModel(value = "AppInfo-CPU", description = "CPU usage information")
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
        @ApiModelProperty(notes = "CPU usage [%]", example = "5.7%")
        public String getUsagePercent() {
            return PERCENT_FORMAT.format(usage);
        }
    }

    @ApiModel(value = "AppInfo-Memory", description = "Memory usage information")
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
        @ApiModelProperty(notes = "Memory usage [%]", example = "36.94%", position = 1)
        public String getUsagePercent() {
            return PERCENT_FORMAT.format(getUsage());
        }

        @JsonProperty("used")
        @ApiModelProperty(notes = "Memory used by the VM", example = "360.5MB", position = 2)
        public String getUsedDisplay() {
            return formatMemory(used);
        }

        @JsonProperty("allocated")
        @ApiModelProperty(notes = "Memory allocated by the VM", example = "976.02MB", position = 3)
        public String getAllocatedDisplay() {
            return formatMemory(allocated);
        }
    }
}
