package com.example.erp.chunk_oriented_batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class FirstItemProcessor implements ItemProcessor<Integer, Long> {
//Input datatype Integer
//output dataType Long
    @Override
    public Long process(Integer item) throws Exception {
        System.out.println("Inside of Item processor!");
        return Long.valueOf(item + 20);
    }
}
