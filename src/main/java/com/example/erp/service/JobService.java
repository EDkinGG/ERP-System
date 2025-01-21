package com.example.erp.service;

import com.example.erp.dto.BatchParamRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;


//Purpose of the service class,
//  1. To dynamically start Spring Batch jobs by their names (firstJob, secondJob).
//  2. To pass custom job parameters at runtime.

@Service
@Slf4j
public class JobService {

    @Autowired
    JobLauncher jobLauncher;

    @Qualifier("firstJob")
    @Autowired
    Job firstJob;

    @Qualifier("secondJob")
    @Autowired
    Job secondJob;


    //The @Async annotation allows the startJob method to run in a separate thread.
    //This enables non-blocking execution, meaning the method returns immediately, and the job runs in the background.
    //Requires @EnableAsync in the application's configuration to enable asynchronous execution.
    @Async
    public void startJob(String jobName, List<BatchParamRequest> jobParamsRequestList) {

        // Use JobParametersBuilder to build job parameters
        // A JobParametersBuilder is used to build JobParameters, which are required to pass data to a batch job at runtime.
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

        // Add a unique parameter to avoid job execution conflicts
        // Adds a unique parameter (currentTime) to ensure each job execution is treated as a new instance.
        // Prevents Spring Batch from rejecting the job due to duplicate execution attempts.
        jobParametersBuilder.addLong("currentTime", System.currentTimeMillis());

        // Add parameters from the request list
        // Spring Batch considers all the key-value pairs provided in the JobParameters as a single logical job instance.
        // That's why in the table we will find same job_execution id for all the key pair value
        jobParamsRequestList.forEach(jobParam -> {
            jobParametersBuilder.addString(jobParam.getParamKey(), jobParam.getParamValue());
        });

        // Build JobParameters
        JobParameters jobParameters = jobParametersBuilder.toJobParameters();

        try {
            JobExecution jobExecution = null;
            if (jobName.equals("firstJob")) {
                jobExecution = jobLauncher.run(firstJob, jobParameters);
            } else if (jobName.equals("secondJob")) {
                jobExecution = jobLauncher.run(secondJob, jobParameters);
            } else {
                log.error("No Job Found Named " + jobName);
                throw new RuntimeException("No Job Found Named " + jobName);
            }
            System.out.println("Job Execution Id : " + jobExecution.getId());
        } catch (Exception e) {
            System.out.println("Exception while starting job");
        }

    }

}
