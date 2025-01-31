package com.example.erp.batch;

import com.example.erp.entity.Customer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Configuration
public class CustomReaderConfig {

    @Bean
    public SynchronizedItemStreamReader<Customer> synchronizedReader() {
        SynchronizedItemStreamReader<Customer> synchronizedReader = new SynchronizedItemStreamReader<>();
        synchronizedReader.setDelegate(reader());
        return synchronizedReader;
    }

    @Bean
    public FlatFileItemReader<Customer> reader() {
        //FlatFileReader: 2 task -> a. setResource
        //                          b. setLineMapper

        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        //a
        itemReader.setResource(new FileSystemResource(
                new File("D:\\Rashed\\ERP system\\erp\\ERP-System\\InputFiles\\customersV2.csv")
        ));
        itemReader.setLinesToSkip(1);
        itemReader.setName("customers");
        //b
        itemReader.setLineMapper(lineMapper());
        return itemReader;
    }

    private LineMapper<Customer> lineMapper() {
        //Line Mapper(DefaultLineMapper): 2 Task -
        //          a. Set Line Tokenizer(DelimitedLineTokenizer) -  i. set column headers, ii. set delimiter(, for CSV)
        //          b. Set Bean Mapper(BeanWrapperFieldSetMapper) -  i. Map the read data to a POJO class
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        lineMapper.setLineTokenizer(new DelimitedLineTokenizer() {
            {
                setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");
                setDelimiter(",");
                setStrict(false);
            }
        });
        lineMapper.setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
            {
                setTargetType(Customer.class);
            }
        });
        return lineMapper;
    }


}
