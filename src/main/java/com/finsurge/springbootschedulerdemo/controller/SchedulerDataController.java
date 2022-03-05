package com.finsurge.springbootschedulerdemo.controller;

import com.finsurge.springbootschedulerdemo.services.SchedulerDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
public class SchedulerDataController {
    private static final Logger log = LoggerFactory.getLogger(SchedulerDataController.class);

    @Autowired
    private SchedulerDataService schedulerDataService;

    @GetMapping("/api/schedules/refresh")
    public ResponseEntity<?> refreshConfig() {
        schedulerDataService.reloadConfigurations();
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
