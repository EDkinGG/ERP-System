package com.example.erp.batch;

import com.example.erp.entity.Customer;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerRowMapper implements RowMapper<Customer> {

    @Override
    public Customer mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Customer(resultSet.getInt("customer_id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("email"),
                resultSet.getString("gender"),
                resultSet.getString("contact"),
                resultSet.getString("country"),
                resultSet.getString("dob"));
    }
}