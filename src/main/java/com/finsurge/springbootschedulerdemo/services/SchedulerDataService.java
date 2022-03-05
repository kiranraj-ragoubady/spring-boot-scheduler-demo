package com.finsurge.springbootschedulerdemo.services;

import com.finsurge.springbootschedulerdemo.jobs.SchedulerJob;
import com.finsurge.springbootschedulerdemo.models.SchedulerConfiguration;
import com.finsurge.springbootschedulerdemo.models.SchedulerJobConfiguration;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SchedulerDataService {
    private static final Logger log = LoggerFactory.getLogger(SchedulerDataService.class);
    private static final String JOB_PREFIX = "DOT_SCHEDULER_JOB_";
    private static final AtomicLong JOB_COUNTER = new AtomicLong(0L);

    @Autowired
    private Scheduler scheduler;

    @Value("${schedules.config.path}")
    private String schedulerConfigPath;

    private long configLastModified = 0L;

    @Scheduled(fixedDelay = 5000)
    public void reloadConfigurations() {
        File configFile = new File(schedulerConfigPath + File.separator + "schedules.json");
        if(!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
            boolean folderCreated = configFile.getParentFile().mkdirs();
            log.error("No readable scheduler configuration JSON found at path {}. {}", configFile.getAbsolutePath(), folderCreated ? "Folder was created." : "Folder already exists.");
            return;
        }
        //if no change in config then return
        if(configFile.lastModified() == configLastModified) {
            return;
        }
        log.info("New or updated scheduler configuration JSON found! Reloading the schedules.");
        configLastModified = configFile.lastModified();

        //load json
        SchedulerConfiguration schedulerConfiguration;
        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(configFile));
            schedulerConfiguration = gson.fromJson(reader, SchedulerConfiguration.class);
        } catch (Exception ex) {
            log.error("There was a problem reading the configuration JSON file. Exception: " + ex.getMessage(), ex);
            return;
        }
        log.info("New configuration: {}", schedulerConfiguration);

        //unload all quartz jobs
        try {
            List<TriggerKey> triggerKeys = new ArrayList<>();
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals("schedulergroup"))) {
                //get job's trigger
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                for(Trigger trigger: triggers) {
                    triggerKeys.add(trigger.getKey());
                }
            }
            if(!triggerKeys.isEmpty()) {
                log.info("Unloading {} jobs.", triggerKeys.size());
                scheduler.unscheduleJobs(triggerKeys);
            } else {
                log.info("No jobs to unload.");
            }
        } catch (SchedulerException ex) {
            log.error("There was a problem while unloading existing jobs. Exception: " + ex.getMessage(), ex);
            return;
        }

        //start new quartz jobs
        for(SchedulerJobConfiguration jobConfiguration : schedulerConfiguration.getSchedules()) {
            if(schedulerConfiguration.getEnabled() && jobConfiguration.getEnabled()) {
                try {
                    String jobName = JOB_PREFIX + JOB_COUNTER.incrementAndGet();
                    JobDetail jobDetail = JobBuilder.newJob(SchedulerJob.class).withDescription(jobConfiguration.getType()).withIdentity(jobName, "schedulergroup").build();
                    jobDetail.getJobDataMap().put("jobConfiguration", jobConfiguration);
                    Trigger trigger = TriggerBuilder
                            .newTrigger()
                            .withIdentity(jobName, "schedulergroup")
                            .withSchedule(CronScheduleBuilder.cronSchedule(jobConfiguration.getSchedule()))
                            .build();
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (SchedulerException ex) {
                    log.error("Cannot schedule job " + jobConfiguration.getType(), ex);
                } catch (Exception ex) {
                    log.error("An exception was encountered while scheduling job.", ex);
                }
            }
        }
    }

}
