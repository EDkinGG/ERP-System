package com.example.erp.batch;

import com.example.erp.chunk_oriented_batch.*;
import com.example.erp.listener.FirstJobListener;
import com.example.erp.listener.FirstStepListener;
import com.example.erp.model.EmployeeCsv;
import com.example.erp.model.EmployeeJson;
import com.example.erp.service.SecondTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

@Configuration //Marks this class as a Spring configuration class where beans are defined
public class ErpBatchConfig {

    @Autowired
    private final JobRepository jobRepository;
    //Manages the metadata of the job and its execution (e.g., job instances, step executions). It’s injected via the constructor.

    @Autowired
    private final PlatformTransactionManager transactionManager;
    //Manages transactions for the batch steps. It’s also injected via the constructor.

    @Autowired
    private final SecondTasklet secondTasklet;

    @Autowired
    private final FirstJobListener firstJobListener;

    @Autowired
    private final FirstStepListener firstStepListener;

    @Autowired
    private final FirstItemReader firstItemReader;

    @Autowired
    private final FirstItemProcessor firstItemProcessor;

    @Autowired
    private final FirstItemWriter firstItemWriter;

    @Autowired
    private final FlatItemWriter flatItemWriter;

    @Autowired
    private final JsonItemWriter jsonItemWriter;

    public ErpBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, SecondTasklet secondTasklet,
                          FirstJobListener firstJobListener, FirstStepListener firstStepListener, FirstItemReader firstItemReader,
                          FirstItemProcessor firstItemProcessor, FirstItemWriter firstItemWriter, FlatItemWriter flatItemWriter, JsonItemWriter jsonItemWriter) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.secondTasklet = secondTasklet;
        this.firstJobListener = firstJobListener;
        this.firstStepListener = firstStepListener;
        this.firstItemReader = firstItemReader;
        this.firstItemProcessor = firstItemProcessor;
        this.firstItemWriter = firstItemWriter;
        this.flatItemWriter = flatItemWriter;
        this.jsonItemWriter = jsonItemWriter;
    }

    //---------------------------------------------1st Job--------------------------------------------------
    //---------------------------------------------Tasklet----------------------------------------------------
    @Bean
    public Job firstJob() {
        /* JobBuilder: Creates and configures the batch Job.
           "First Job": The name of the job.
                  jobRepository: The JobRepository used to persist job execution metadata.
           start(firstStep()): Links the first step (firstStep()) to this job. */

        return new JobBuilder("First Job", jobRepository)
                .start(firstStep())
                .next(secondStep())
                .listener(firstJobListener)
                .build();
    }

    //Steps
    private Step firstStep() {
        /*
            StepBuilder: Creates and configures a batch Step.
            "First Step": The name of the step.
            jobRepository: The JobRepository for persisting step execution metadata.
            tasklet(firstTask(), transactionManager):
            Attaches a Tasklet (a unit of work) to this step.
        */
        return new StepBuilder("First Step", jobRepository)
                .tasklet(firstTask(), transactionManager)
                .listener(firstStepListener)
                .build();
    }

    private Step secondStep() {
        //calling tasklet service from here
        return new StepBuilder("Second Step", jobRepository)
                .tasklet(secondTasklet, transactionManager)
                .build();
    }

    //Task of step 1
    private Tasklet firstTask() {
        /*
        Tasklet: A functional interface that represents a single unit of work for a step.
        execute():
        Executes the work of the task.
        Prints "First Step" to the console.
        Returns RepeatStatus.FINISHED, signaling that the tasklet is complete and the step should not repeat
        */
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("This is first tasklet Step");
                System.out.println("Step exec cont = " + chunkContext.getStepContext().getStepExecutionContext());
                return RepeatStatus.FINISHED;
            }
        };
    }

/*
    private Tasklet secondTask() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Second Step");
                return RepeatStatus.FINISHED;
            }
        };
    }
 */

    //-------------------------------------------2nd Job--------------------------------------------
    //---------------------------------------Chunk Oriented-------------------------------------------
    @Bean
    public Job secondJob() {
        return new JobBuilder("Second Job", jobRepository)
                .start(firstChunkStep())
                .build();
    }

    private Step firstChunkStep() {
        return new StepBuilder("First Chunk Step", jobRepository)
                .<Integer, Long>chunk(3, transactionManager)
                .reader(firstItemReader)
                .processor(firstItemProcessor)
                .writer(firstItemWriter)
                .build();
    }

    //------------------------------------------3rd job-----------------------------------------------
    //------------------------------------Flat File Item Reader--------------------------------------------------
    @Bean
    public Job flatFileJob() {
        return new JobBuilder("Flat File Job", jobRepository)
                .start(flatFileChunkStep())
                .build();
    }

    private Step flatFileChunkStep() {
        return new StepBuilder("Frist Flat File Chunk Step", jobRepository)
                .<EmployeeCsv, EmployeeCsv>chunk(3, transactionManager)
                .reader(flatItemReader())
                .writer(flatItemWriter)
                .build();
    }

    public FlatFileItemReader<EmployeeCsv> flatItemReader() {

        //FlatFileReader: 2 task -> a. setResource
        //                          b. setLineMapper
        FlatFileItemReader<EmployeeCsv> reader = new FlatFileItemReader<>();


        //Source Location of CSV file
        reader.setResource(new FileSystemResource(
                new File("D:\\Rashed\\ERP system\\erp\\ERP-System\\InputFiles\\employee.csv")
        ));


        //Line Mapper(DefaultLineMapper): 2 Task -
        //              a. Set Line Tokenizer(DelimitedLineTokenizer) -  i. set column headers,
        //                                                              ii. set delimiter(, for CSV)
        //              b. Set Bean Mapper(BeanWrapperFieldSetMapper) - i. Map the read data to a POJO class
        reader.setLineMapper(new DefaultLineMapper<EmployeeCsv>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames("EmployeeId","Name","SurName","Email");
                        setDelimiter(",");//"," is set by default, I just showed for reference
                    }
                });

                setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(EmployeeCsv.class);
                    }
                });
            }
        });

        //Skip 1st row as it is the column name
        reader.setLinesToSkip(1);

        //    FlatFileItemReader<EmployeeCsv> reader = new FlatFileItemReader<>();
        //
        //    // Set the file resource with the correct path
        //    reader.setResource(new FileSystemResource("path/to/your/file.csv"));
        //
        //    // Create a DefaultLineMapper
        //    DefaultLineMapper<EmployeeCsv> lineMapper = new DefaultLineMapper<>();
        //
        //    // Create and configure the DelimitedLineTokenizer
        //    DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        //    tokenizer.setNames("EmployeId", "Name", "SurName", "Email");
        //    tokenizer.setDelimiter(",");
        //
        //    // Create and configure the BeanWrapperFieldSetMapper
        //    BeanWrapperFieldSetMapper<EmployeeCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        //    fieldSetMapper.setTargetType(EmployeeCsv.class);
        //
        //    // Set the tokenizer and field set mapper on the line mapper
        //    lineMapper.setLineTokenizer(tokenizer);
        //    lineMapper.setFieldSetMapper(fieldSetMapper);
        //
        //    // Set the line mapper and other configurations on the reader
        //    reader.setLineMapper(lineMapper);
        //    reader.setLinesToSkip(1);

        return reader;
    }

    //------------------------------------------4th job-----------------------------------------------
    //------------------------------------Json File Item Reader--------------------------------------------------
    @Bean
    public Job jsonFileJob() {
        return new JobBuilder("Json File Job", jobRepository)
                .start(jsonFileChunkStep())
                .build();
    }

    private Step jsonFileChunkStep() {
        return new StepBuilder("First Json File Chunk Step", jobRepository)
                .<EmployeeJson, EmployeeJson>chunk(3, transactionManager)
                .reader(jsonItemReader())
                .writer(jsonItemWriter)
                .build();
    }

    public JsonItemReader<EmployeeJson> jsonItemReader() {

        //FlatFileReader: 2 task -> a. setResource
        //                          b. setJsonObjectReader
        JsonItemReader<EmployeeJson> reader = new JsonItemReader<>();


        //Source Location of CSV file
        reader.setResource(new FileSystemResource(
                new File("D:\\Rashed\\ERP system\\erp\\ERP-System\\InputFiles\\employee.json")
        ));

        //Json object reader
        reader.setJsonObjectReader(
                new JacksonJsonObjectReader<>(EmployeeJson.class)
        );

        //Read up to index 8
        reader.setMaxItemCount(8);

        //Start reading from index 2
        reader.setCurrentItemCount(2);

        return reader;
    }

}
