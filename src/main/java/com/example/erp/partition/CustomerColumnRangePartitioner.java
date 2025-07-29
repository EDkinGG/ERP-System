package com.example.erp.partition;

import lombok.Setter;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class CustomerColumnRangePartitioner implements Partitioner {

    private JdbcOperations jdbcTemplate;
    @Setter
    private String tableName;
    @Setter
    private String columnName;

    public void setDataSource(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Integer min = jdbcTemplate.queryForObject("SELECT MIN("+columnName+") from "+tableName, Integer.class);
        Integer max = jdbcTemplate.queryForObject("SELECT MAX("+columnName+") from "+tableName, Integer.class);

        if (min == null || max == null || min.equals(max)) {
            throw new IllegalStateException("No data available for partitioning.");
        }

        int targetSize = (max - min + 1) / gridSize;


        Map<String, ExecutionContext> partitions = new HashMap<>();

        int start = min;

        for( int i = 0 ; i < gridSize ; i ++) {
            int end = start + targetSize - 1;
            if( i == gridSize - 1 ) {
                end = max;
            }

            ExecutionContext context = new ExecutionContext();
            context.putInt("minValue", start);
            context.putInt("maxValue", end);
            partitions.put("Partition "+ i, context);

            start = end + 1;
        }
        System.out.println("Partition result = " + partitions.toString());

        return partitions;
    }


}
