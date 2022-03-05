package com.finsurge.springbootschedulerdemo.models;

import lombok.Data;

public @Data class SchedulerJobConfiguration {

    private String type;
    private String schedule;
    private Boolean enabled;
    private String[] params;

}
