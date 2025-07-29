package com.example.erp.batch;

import com.example.erp.entity.Customer;
import com.example.erp.partition.CustomerColumnRangePartitioner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Configuration
@Slf4j
public class OreillyLocalPartitioningBatchConfig {

    @Autowired
    private final JobRepository jobRepository;

    @Autowired
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private final DataSource dataSource;

    @Value("${thread.maxPool.size}")
    private Integer threadPoolSize;

    @Value("${thread.maxPool.size}")
    private Integer threadCorePoolSize;

    @Value("${thread.queuePool.size}")
    private Integer threadQueuePoolSize;

    @Value("${spring.batch.commit.chunk}")
    private Integer chunkSize;

    public OreillyLocalPartitioningBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
    }

    //Local Partition
    //https://github.com/ngecom/springBatchLocalParition/tree/master
    @Bean
    public Job oreillyLocalPartitionJob() throws Exception {
        return new JobBuilder("Local Partition", jobRepository)
                .start(oreillyMasterStep())
                .build();
    }

    @Bean
    public Step oreillyMasterStep() throws Exception {
        return new StepBuilder("Oreilly MasterStep", jobRepository)
                .partitioner(oreillySlaveStep().getName(), oreillyPartitioner())
                .step(oreillySlaveStep())
                .gridSize(threadPoolSize)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @SuppressWarnings("unchecked")
    @Bean
    public Step oreillySlaveStep() throws Exception {
        return new StepBuilder("Oreilly Partition Step", jobRepository)
                .<Customer, Customer>chunk(chunkSize, transactionManager)
                .reader(partitionPagingItemReader(null,null))
                .processor(itemProcessorLocal())
                .writer(customerItemWriterLocal())
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Customer> partitionPagingItemReader(
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue)
    {
        System.out.println("reading " + minValue + " to " + maxValue);


        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(100);
        reader.setPageSize(100);
        reader.setRowMapper(new CustomerRowMapper());

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("customer_id", Order.ASCENDING);

        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("customer_id,first_name,last_name,email,gender,contact,country,dob");
        queryProvider.setFromClause("from erp_v1.customers_info");
        queryProvider.setWhereClause("where customer_id >= " + minValue + " and customer_id <= " + maxValue);
        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);
        reader.setSaveState(false);

        return reader;
    }

    @Bean
    public ItemProcessor itemProcessorLocal() {
        try {
            return new ItemProcessor<Customer, Customer>() {

                @Override
                public Customer process(Customer item) throws Exception {
                    //Thread.sleep(new Random().nextInt(10));
                    return new Customer(
                            item.getId(),
                            item.getFirstName(),
                            item.getLastName(),
                            item.getEmail(),
                            item.getGender(),
                            item.getContactNo(),
                            item.getCountry(),
                            item.getDob()
                    );
                }
            };// Return processed item
        } catch (Exception e) {
            log.error("Error processing item: ", e);
            throw e; // Rethrow to let Spring Batch handle the exception
        }

    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Customer> customerItemWriterLocal() {

        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(this.dataSource);
        writer.setSql("INSERT INTO erp_v1.customers_info_write VALUES(:id,:firstName,:lastName," +
                ":email,:gender,:contactNo,:country,:dob)");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();

        return writer;
    }


    @Bean
    public CustomerColumnRangePartitioner oreillyPartitioner() {
        CustomerColumnRangePartitioner customerColumnRangePartitioner = new CustomerColumnRangePartitioner();
        customerColumnRangePartitioner.setDataSource(this.dataSource);
        customerColumnRangePartitioner.setTableName("erp_v1.customers_info");
        customerColumnRangePartitioner.setColumnName("customer_id");
        return customerColumnRangePartitioner;
    }

    @Bean
    public ThreadPoolTaskExecutor oreillyTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(threadPoolSize);
        taskExecutor.setCorePoolSize(threadCorePoolSize);
        taskExecutor.setQueueCapacity(threadQueuePoolSize);
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

}
