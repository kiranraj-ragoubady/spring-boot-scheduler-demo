package com.finsurge.springbootschedulerdemo.models;

import lombok.Data;

import java.util.List;

public @Data class SchedulerConfiguration {

    private Boolean enabled;
    private List<SchedulerJobConfiguration> schedules;

    @Override
    public String toString() {
        return "SchedulerConfiguration{" +
                "enabled=" + enabled +
                ", schedules=" + schedules.toString() +
                '}';
    }
}
