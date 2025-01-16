package com.example.erp.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job demoJob;

    public BatchController(JobLauncher jobLauncher, Job demoJob) {
        this.jobLauncher = jobLauncher;
        this.demoJob = demoJob;
    }

    @PostMapping("/first-job")
    public ResponseEntity<String> firstJob() {
        try{
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("run.id", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(demoJob, jobParameters);
            return ResponseEntity.ok("Job Executed with status " + execution.getStatus());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
