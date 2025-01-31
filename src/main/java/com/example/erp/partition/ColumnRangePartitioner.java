package com.example.erp.partition;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import java.util.HashMap;
import java.util.Map;

public class ColumnRangePartitioner implements Partitioner {

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        int min = 1;
        int max = 100000;
        int targetSize = (max - min + 1) / gridSize;

        System.out.println("targetSize = " + targetSize);
        Map<String, ExecutionContext> partition = new HashMap<>();

        int start = min;
        for( int i = 0 ; i < gridSize ; i ++) {
            int end = start + targetSize - 1;
            if( i == gridSize - 1 ) {
                end = max;
            }

            ExecutionContext context = new ExecutionContext();
            context.putInt("minValue", start);
            context.putInt("maxValue", end);
            partition.put("Partition "+ i, context);

            start = end + 1;
        }
        System.out.println("Partition result = " + partition.toString());

        return partition;
    }
}
