package com.example.erp.chunk_oriented_batch;

import com.example.erp.model.EmployeeJdbc;
import org.hibernate.annotations.Cache;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class JdbcItemWriter implements ItemWriter <EmployeeJdbc>{
    @Override
    public void write(Chunk<? extends EmployeeJdbc> chunk) throws Exception {
        System.out.println("JdbcItemWriter start");
        chunk.forEach(System.out::println);
    }
}
