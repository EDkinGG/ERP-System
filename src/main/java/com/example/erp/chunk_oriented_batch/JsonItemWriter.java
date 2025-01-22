package com.example.erp.chunk_oriented_batch;

import com.example.erp.model.EmployeeJson;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class JsonItemWriter implements ItemWriter<EmployeeJson> {
    @Override
    public void write(Chunk<? extends EmployeeJson> items) throws Exception {
        System.out.println("Inside Json Item Writer");
        items.forEach(System.out::println);
    }
}
