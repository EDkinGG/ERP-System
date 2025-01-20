package com.example.erp.chunk_oriented_batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FirstItemWriter implements ItemWriter<Long> {
    @Override
    public void write(Chunk<? extends Long> items) throws Exception {
        System.out.println("Inside Item Writer");
        items.forEach(System.out::println);
    }
}
