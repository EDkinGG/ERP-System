package com.example.erp.batch;

import com.example.erp.entity.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OreillyBatchConfig {
    @Autowired
    private final JobRepository jobRepository;

    @Autowired
    private final PlatformTransactionManager transactionManager;

    @Autowired
    private final DataSource dataSource;

    public OreillyBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
    }


    //Multithreaded Job
    @Bean
    public Job oreillyMultithreadJob() {
        return new JobBuilder("Multithreaded job", jobRepository)
                .start(stepMulti())
                .build();
    }

    @Bean
    public Step stepMulti() {
        return new StepBuilder("Multithreaded step", jobRepository)
                .<Customer, Customer>chunk(1000, transactionManager)
                .reader(pagingItemReader())
                .writer(customerItemWriter())
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<Customer> pagingItemReader() {
        JdbcPagingItemReader<Customer> reader = new JdbcPagingItemReader<>();

        reader.setDataSource(this.dataSource);
        reader.setFetchSize(100);
        reader.setPageSize(100);
        reader.setRowMapper(new CustomerRowMapper());

        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("customer_id,first_name,last_name,email,gender,contact,country,dob");
        queryProvider.setFromClause("from erp_v1.customers_info");

        Map<String,Order> sortKeys = new HashMap<>(1);

        sortKeys.put("customer_id",Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        reader.setQueryProvider(queryProvider);
        reader.setSaveState(false);

        return reader;
    }

    @Bean
    public JdbcBatchItemWriter<Customer> customerItemWriter() {

        JdbcBatchItemWriter<Customer> writer = new JdbcBatchItemWriter<>();

        writer.setDataSource(this.dataSource);
        writer.setSql("INSERT INTO erp_v1.customers_info_write VALUES(:id,:firstName,:lastName," +
                ":email,:gender,:contactNo,:country,:dob)");
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.afterPropertiesSet();

        return writer;
    }


}
