package com.example.erp.batch;

import com.example.erp.entity.Customer;
import com.example.erp.partition.ColumnRangePartitioner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TechieBatchConfig {
    @Autowired
    private final JobRepository jobRepository;

    @Autowired
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private final SynchronizedItemStreamReader<Customer> reader;

    @Autowired
    private final CustomWriter customWriter;

    @Autowired
    private final CustomProcessor customProcessor;

    public TechieBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                             SynchronizedItemStreamReader<Customer> reader, CustomWriter customWriter,
                             CustomProcessor customProcessor) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.reader = reader;
        this.customWriter = customWriter;
        this.customProcessor = customProcessor;
    }

    @Bean
    public Job techieJob() {
        return new JobBuilder("import customers", jobRepository)
                .flow(masterStep()).end().build();
    }

    @Bean
    public Step masterStep() {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner(slaveStep().getName(), partitioner())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public Step slaveStep() {
        return new StepBuilder("slaveStep", jobRepository)
                .<Customer, Customer>chunk(10000, transactionManager)
                .reader(reader)
                .processor(customProcessor)
                .writer(customWriter)
                .build();
    }

    @Bean
    public ColumnRangePartitioner partitioner() {
        //Ei class o autowired kore class variable declared kora jaito
        //oitar jonno amdr ColumnRangePartitioner class e @Component use kora lagto
        //oikhane component use kori nai dekhe ekhane age @Bean bole dite hoise
        return new ColumnRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(10);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep());

        return taskExecutorPartitionHandler;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setMaxPoolSize(20);
        taskExecutor.setQueueCapacity(30);

        return taskExecutor;
    }

}
