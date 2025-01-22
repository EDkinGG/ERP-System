package com.example.erp.chunk_oriented_batch;

import com.example.erp.model.EmployeeXml;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class XmlItemWriter implements ItemWriter<EmployeeXml> {
    @Override
    public void write(Chunk<? extends EmployeeXml> items) throws Exception {
        System.out.println("Inside Xml file Writer");
        items.forEach(System.out::println);
    }
}
