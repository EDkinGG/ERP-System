package com.example.erp.chunk_oriented_batch;

import com.example.erp.model.EmployeeCsv;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FlatItemWriter implements ItemWriter <EmployeeCsv>{

    @Override
    public void write(Chunk<? extends EmployeeCsv> items) throws Exception {
        System.out.println("Inside Flat Item Writer");
        items.forEach(System.out::println);
    }
}
