package com.example.erp.batch;

import com.example.erp.chunk_oriented_batch.FirstItemProcessor;
import com.example.erp.chunk_oriented_batch.FirstItemReader;
import com.example.erp.chunk_oriented_batch.FirstItemWriter;
import com.example.erp.listener.FirstJobListener;
import com.example.erp.listener.FirstStepListener;
import com.example.erp.service.SecondTasklet;
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

    @Autowired
    private final SecondTasklet secondTasklet;

    @Autowired
    private final FirstJobListener firstJobListener;

    @Autowired
    private final FirstStepListener firstStepListener;

    @Autowired
    private final FirstItemReader firstItemReader;

    @Autowired
    private final FirstItemProcessor firstItemProcessor;

    @Autowired
    private final FirstItemWriter firstItemWriter;

    public ErpBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, SecondTasklet secondTasklet, FirstJobListener firstJobListener, FirstStepListener firstStepListener, FirstItemReader firstItemReader, FirstItemProcessor firstItemProcessor, FirstItemWriter firstItemWriter) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.secondTasklet = secondTasklet;
        this.firstJobListener = firstJobListener;
        this.firstStepListener = firstStepListener;
        this.firstItemReader = firstItemReader;
        this.firstItemProcessor = firstItemProcessor;
        this.firstItemWriter = firstItemWriter;
    }

    //1st Job
    @Bean
    public Job firstJob() {
        /* JobBuilder: Creates and configures the batch Job.
           "First Job": The name of the job.
                  jobRepository: The JobRepository used to persist job execution metadata.
           start(firstStep()): Links the first step (firstStep()) to this job. */

        return new JobBuilder("First Job", jobRepository)
                .start(firstStep())
                .next(secondStep())
                .listener(firstJobListener)
                .build();
    }


    //Steps

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
                .listener(firstStepListener)
                .build();
    }

    private Step secondStep() {
        //calling tasklet service from here
        return new StepBuilder("Second Step", jobRepository)
                .tasklet(secondTasklet, transactionManager)
                .build();
    }

    //Task

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
                System.out.println("This is first tasklet Step");
                System.out.println("Step exec cont = " + chunkContext.getStepContext().getStepExecutionContext());
                return RepeatStatus.FINISHED;
            }
        };
    }

/*
    private Tasklet secondTask() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Second Step");
                return RepeatStatus.FINISHED;
            }
        };
    }
 */

    //2nd Job
    @Bean
    public Job secondJob() {
        return new JobBuilder("Second Job", jobRepository)
                .start(firstChunkStep())
                .build();
    }

    private Step firstChunkStep() {
        return new StepBuilder("First Chunk Step", jobRepository)
                .<Integer, Long>chunk(3, transactionManager)
                .reader(firstItemReader)
                .processor(firstItemProcessor)
                .writer(firstItemWriter)
                .build();
    }

}
