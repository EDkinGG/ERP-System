package com.example.erp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecondJobScheduler {

    @Autowired
    JobLauncher jobLauncher;

    @Qualifier("secondJob")
    @Autowired
    Job secondJob;

    //use cron maker
    //scheduled every 1 hour
    @Scheduled(cron = "0 0 0/1 1/1 * ? ")
    public void secondJobStarter() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("currentTime", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(secondJob, jobParameters);
            System.out.println("Job Execution ID = " + jobExecution.getId());

        } catch (Exception e) {
            log.error(e.getMessage());
            System.out.println("Exception while starting job");
        }
    }
}
