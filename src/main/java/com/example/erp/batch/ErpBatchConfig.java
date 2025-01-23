package com.example.erp.batch;

import com.example.erp.chunk_oriented_batch.*;
import com.example.erp.listener.FirstJobListener;
import com.example.erp.listener.FirstStepListener;
import com.example.erp.model.*;
import com.example.erp.service.EmployeeService;
import com.example.erp.service.SecondTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

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

    @Autowired
    private final XmlItemWriter xmlItemWriter;

    @Autowired
    private final JdbcItemWriter jdbcItemWriter;

    @Autowired
    private final ApiItemWriter apiItemWriter;

    @Autowired
    private final DataSource dataSource;

    @Autowired
    private final EmployeeService employeeService;



    public ErpBatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager, SecondTasklet secondTasklet,
                          FirstJobListener firstJobListener, FirstStepListener firstStepListener, FirstItemReader firstItemReader,
                          FirstItemProcessor firstItemProcessor, FirstItemWriter firstItemWriter, FlatItemWriter flatItemWriter, JsonItemWriter jsonItemWriter, XmlItemWriter xmlItemWriter, JdbcItemWriter jdbcItemWriter, ApiItemWriter apiItemWriter, DataSource dataSource, EmployeeService employeeService) {
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
        this.xmlItemWriter = xmlItemWriter;
        this.jdbcItemWriter = jdbcItemWriter;
        this.apiItemWriter = apiItemWriter;
        this.dataSource = dataSource;
        this.employeeService = employeeService;
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
                        setNames("EmployeeId", "Name", "SurName", "Email");
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

    //------------------------------------------5th job-----------------------------------------------
    //------------------------------------Xml File Item Reader--------------------------------------------------
    @Bean
    public Job xmlFileJob() {
        return new JobBuilder("Xml File Job", jobRepository)
                .start(xmlFileChunkStep())
                .build();
    }

    private Step xmlFileChunkStep() {
        return new StepBuilder("First Xml File Chunk Step", jobRepository)
                .<EmployeeXml, EmployeeXml>chunk(3, transactionManager)
                .reader(staxEventItemReader())
                .writer(xmlItemWriter)
                .build();
    }

    public StaxEventItemReader<EmployeeXml> staxEventItemReader() {

        //StaxEventItemReader: 3 task -> a. setResource
        //                               b. setFragmentRootElementName
        //                               c. setUnmarshaller->SetClassesToBeBound
        StaxEventItemReader<EmployeeXml> reader = new StaxEventItemReader<>();


        //Source Location of xml file
        reader.setResource(new FileSystemResource(
                new File("D:\\Rashed\\ERP system\\erp\\ERP-System\\InputFiles\\employee.xml")
        ));

        reader.setFragmentRootElementName("employee");

        // Configure and set the JAXB2 Marshaller
        // Marshaller - Convert Java object to XML
        // UnMarshaller - Convert XML to Java Object
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(EmployeeXml.class);
        reader.setUnmarshaller(marshaller);

//        reader.setUnmarshaller(new Jaxb2Marshaller() {
//            {
//                setClassesToBeBound(EmployeeXml.class);
//            }
//        });
        return reader;
    }


    //------------------------------------------6th job-----------------------------------------------
    //------------------------------------Jdbc Item Reader--------------------------------------------------
    @Bean
    public Job jdbcFileJob() {
        return new JobBuilder("Jdbc Job", jobRepository)
                .start(jdbcChunkStep())
                .build();
    }

    private Step jdbcChunkStep() {
        return new StepBuilder("First Jdbc Chunk Step", jobRepository)
                .<EmployeeJdbc, EmployeeJdbc>chunk(3, transactionManager)
                .reader(jdbcCursorItemReader())
                .writer(jdbcItemWriter)
                .build();
    }

    public JdbcCursorItemReader<EmployeeJdbc> jdbcCursorItemReader()
    {
        JdbcCursorItemReader<EmployeeJdbc> reader = new JdbcCursorItemReader<>();

        reader.setDataSource(dataSource);
        reader.setSql(
                "select employee_id as employeeId,name,surname,email"
                + " from erp_v1.employee_info");

        reader.setRowMapper(new BeanPropertyRowMapper<EmployeeJdbc>(){
            {
                setMappedClass(EmployeeJdbc.class);
            }
        });

        //reader.setCurrentItemCount(2);
        //reader.setMaxItemCount(8);

        return reader;
    }

    //------------------------------------------7th job-----------------------------------------------
    //------------------------------------Rest Api Item Reader--------------------------------------------------
    @Bean
    public Job apiFileJob() {
        return new JobBuilder("Rest Api Job", jobRepository)
                .start(apiChunkStep())
                .build();
    }

    private Step apiChunkStep() {
        return new StepBuilder("First Rest Api Chunk Step", jobRepository)
                .<EmployeeResponse, EmployeeResponse>chunk(3, transactionManager)
                .reader(itemReaderAdapter())
                .writer(apiItemWriter)
                .build();
    }

   public ItemReaderAdapter<EmployeeResponse> itemReaderAdapter() {
        ItemReaderAdapter<EmployeeResponse> readerAdapter = new ItemReaderAdapter<EmployeeResponse>();

        readerAdapter.setTargetObject(employeeService);
        readerAdapter.setTargetMethod("getEmployee");
        readerAdapter.setArguments(new Object[]{1L, "Test"});

        return readerAdapter;
   }

    //------------------------------------------WRITER---------------------------------------------------------
    //--------------------------SOB GULA JDBC THEKE READ KORE WRITE KORBO--------------------------------------
    //------------------------------------------8th job--------------------------------------------------------
    //------------------------------------CSV Writer------------------------------------------------------------
    @Bean
    public Job writeFlatFileJob() {
        return new JobBuilder("Write flat file Job", jobRepository)
                .start(writeFlatFileChunkStep())
                .build();
    }

    private Step writeFlatFileChunkStep() {
        return new StepBuilder("First Write flat Chunk Step", jobRepository)
                .<EmployeeJdbc, EmployeeCsv>chunk(3, transactionManager)
                .reader(jdbcCursorItemReader())
                .writer(flatfileItemWriter())
                .build();
    }

    public FlatFileItemWriter<EmployeeCsv> flatfileItemWriter()
    {
        FlatFileItemWriter<EmployeeCsv> flatfileItemWriter =
                new FlatFileItemWriter<EmployeeCsv>();

        flatfileItemWriter.setResource(new FileSystemResource(
                new File("D:\\Rashed\\ERP system\\erp\\ERP-System\\OutputFiles\\employee.csv")
        ));

        flatfileItemWriter.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("employeeId,name,surname,email");
            }
        });

        flatfileItemWriter.setLineAggregator(new DelimitedLineAggregator<EmployeeCsv>(){
            {
                setDelimiter(",");//default -> ,
                setFieldExtractor( new BeanWrapperFieldExtractor<EmployeeCsv>(){
                    {
                        setNames(new String[]{"employeeId","name","surname","email"});
                    }
                });
            }
        });

        flatfileItemWriter.setFooterCallback( new FlatFileFooterCallback() {
            @Override
            public void writeFooter(Writer writer) throws IOException {
                writer.write("Created @ "+ new Date());
            }
        });

        return flatfileItemWriter;
    }

    //------------------------------------------9th job--------------------------------------------------------
    //-----------------------------------------JSON WRITER-----------------------------------------------------

    @Bean
    public Job writeJsonJob() {
        return new JobBuilder("Write Json file Job", jobRepository)
                .start(writeJsonChunkStep())
                .build();
    }

    private Step writeJsonChunkStep() {
        return new StepBuilder("First Write flat Chunk Step", jobRepository)
                .<EmployeeJdbc, EmployeeJson>chunk(3, transactionManager)
                .reader(jdbcCursorItemReader())
                .writer(jsonFileItemWriter())
                .build();
    }

    private JsonFileItemWriter<EmployeeJson> jsonFileItemWriter() {
        JsonFileItemWriter<EmployeeJson> writer =
                new JsonFileItemWriter<>(new FileSystemResource
                        (new File("D:\\Rashed\\ERP system\\erp\\ERP-System\\OutputFiles\\employee.json")),
                        new JacksonJsonObjectMarshaller<EmployeeJson>()
                );
        return writer;
    }

    //------------------------------------------10th job--------------------------------------------------------
    //-----------------------------------------Xml WRITER-----------------------------------------------------

    @Bean
    public Job writeXmlJob() {
        return new JobBuilder("Write Xml file Job", jobRepository)
                .start(writeXmlChunkStep())
                .build();
    }

    private Step writeXmlChunkStep() {
        return new StepBuilder("First Write Xml Chunk Step", jobRepository)
                .<EmployeeJdbc, EmployeeXml>chunk(3, transactionManager)
                .reader(jdbcCursorItemReader())
                .writer(writeXmlItemWriter())
                .build();
    }

    private StaxEventItemWriter<EmployeeXml> writeXmlItemWriter() {

        // Create the StaxEventItemWriter
        StaxEventItemWriter<EmployeeXml> writer = new StaxEventItemWriter<>();

        // Set the output file resource
        writer.setResource(new FileSystemResource(
                "D:\\Rashed\\ERP system\\erp\\ERP-System\\OutputFiles\\employee.xml"
        ));

        // Set the root tag name for the XML
        writer.setRootTagName("employees");

        // Configure the Jaxb2Marshaller
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(EmployeeXml.class);
        writer.setMarshaller(marshaller);

        return writer;
    }




}
