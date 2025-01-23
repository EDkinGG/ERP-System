package com.example.erp.chunk_oriented_batch;

import com.example.erp.model.EmployeeResponse;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class ApiItemWriter implements ItemWriter <EmployeeResponse>{
    @Override
    public void write(Chunk<? extends EmployeeResponse> items) throws Exception {
        System.out.println("Inside Api Writer");
        items.forEach(System.out::println);
    }
}
