package com.example.erp.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration //Marks this class as a Spring configuration class where beans are defined
public class ErpBatchConfig {

    @Autowired
    private final JobRepository jobRepository;
    //Manages the metadata of the job and its execution (e.g., job instances, step executions). It’s injected via the constructor.

    @Autowired
    private final PlatformTransactionManager transactionManager;
    //Manages transactions for the batch steps. It’s also injected via the constructor.

    public ErpBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job firstJob() {
        /* JobBuilder: Creates and configures the batch Job.
           "First Job": The name of the job.
                  jobRepository: The JobRepository used to persist job execution metadata.
           start(firstStep()): Links the first step (firstStep()) to this job. */

        return new JobBuilder("First Job", jobRepository)
                .start(firstStep())
                .build();
    }


    private Step firstStep() {
        /*
            StepBuilder: Creates and configures a batch Step.
            "First Step": The name of the step.
            jobRepository: The JobRepository for persisting step execution metadata.
            tasklet(firstTask(), transactionManager):
            Attaches a Tasklet (a unit of work) to this step.
        */
        return new StepBuilder("First Step", jobRepository)
                .tasklet(firstTask(), transactionManager)
                .build();
    }

    private Tasklet firstTask() {
        /*
        Tasklet: A functional interface that represents a single unit of work for a step.
        execute():
        Executes the work of the task.
        Prints "First Step" to the console.
        Returns RepeatStatus.FINISHED, signaling that the tasklet is complete and the step should not repeat
        */
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("First Step");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
