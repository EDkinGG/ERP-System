package com.example.erp.listener;

import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

@Component
public class FirstStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Before Step " + stepExecution.getStepName());
        System.out.println("Job Exec Context " + stepExecution.getJobExecution().getExecutionContext());
        System.out.println("Step Exec Context " + stepExecution.getExecutionContext());

        stepExecution.getExecutionContext().put("step exec context", "sec value");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("after Step " + stepExecution.getStepName());
        System.out.println("Job Exec Context " + stepExecution.getJobExecution().getExecutionContext());
        System.out.println("Step Exec Context " + stepExecution.getExecutionContext());
        return null;
    }

}
