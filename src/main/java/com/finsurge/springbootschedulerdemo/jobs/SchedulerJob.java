package com.finsurge.springbootschedulerdemo.jobs;

import com.finsurge.springbootschedulerdemo.models.SchedulerJobConfiguration;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(SchedulerJob.class);

    //autowire other services here

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        SchedulerJobConfiguration jobConfiguration = (SchedulerJobConfiguration) jobDataMap.get("jobConfiguration");

        if(jobConfiguration == null) {
            log.error("No configuration supplied to scheduler job. Exiting.");
            return;
        }

        switch (jobConfiguration.getType()) {
            case "SYNC_JOB": {
                log.info("Reports synchronization job to be triggered with parameters: {}", jobConfiguration.getParams());
                break;
            }
            case "ARCHIVE_JOB": {
                log.info("Reports archival job to be triggered with parameters: {}", jobConfiguration.getParams());
                break;
            }
            case "HOUSEKEEPING_JOB": {
                log.info("Housekeeping job to be triggered with parameters: {}", jobConfiguration.getParams());
                break;
            }
            default: {
                log.info("The job type {} is not supported yet.", jobConfiguration.getType());
            }
        }
    }

}
