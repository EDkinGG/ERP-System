package com.example.erp.batch;

import com.example.erp.entity.Customer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CustomProcessor implements ItemProcessor<Customer,Customer> {

    @Override
    public Customer process(Customer item) throws Exception {
        return item;
    }
}
