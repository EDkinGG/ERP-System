package com.example.erp.controller;

import com.example.erp.dto.BatchParamRequest;
import com.example.erp.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job demoTaskletJob;
    private final Job demoChunkOrientedJob;
    private final JobService jobService;
    private final JobOperator jobOperator;

    //the @Qualifier annotation is used to resolve ambiguity when multiple beans of the same type exist in the Spring context.
    //It helps Spring identify which specific bean to inject into a particular dependency when multiple options are available.
    public BatchController(JobLauncher jobLauncher, @Qualifier("firstJob") Job demoTaskletJob,
                           @Qualifier("secondJob") Job demoChunkOrientedJob, JobService jobService, JobOperator jobOperator) {
        this.jobLauncher = jobLauncher;
        this.demoTaskletJob = demoTaskletJob;
        this.demoChunkOrientedJob = demoChunkOrientedJob;
        this.jobService = jobService;
        this.jobOperator = jobOperator;
    }

    @PostMapping("/tasklet-job")
    public ResponseEntity<String> taskletJob() {
        try {
            //Job Parameters in Spring Batch are used to pass external, dynamic data to a batch job at runtime.
            //These parameters help make batch jobs configurable and reusable, as they allow you to execute the same job
            //with different input values without modifying the code.
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("run.id", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();

            //The JobLauncher in Spring Batch is used to execute a batch job programmatically. It provides the mechanism
            // to launch jobs with specific configurations, including the job parameters required for execution. This makes
            // it a central component for triggering batch processing workflows.
            JobExecution execution = jobLauncher.run(demoTaskletJob, jobParameters);
            return ResponseEntity.ok("Job Executed with status " + execution.getStatus());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/chunk-oriented-job")
    public ResponseEntity<String> chunkOrientedJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("run.id", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(demoChunkOrientedJob, jobParameters);
            return ResponseEntity.ok("Job Executed with status " + execution.getStatus());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/passing-params/{jobName}")
    public ResponseEntity<String> passingJobParamWithRestApi(@PathVariable String jobName, @RequestBody List<BatchParamRequest> params) {
        try {
            jobService.startJob(jobName, params);
            System.out.println("Job started!");
            return ResponseEntity.ok("Job Executed");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/stop/{jobExecutionId}")
    public ResponseEntity<String> stopJob(@PathVariable Long jobExecutionId) {
        try {
            jobOperator.stop(jobExecutionId);
            return ResponseEntity.ok("Job Stopped!");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
