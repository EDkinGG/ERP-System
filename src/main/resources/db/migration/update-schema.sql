ALTER TABLE batch_job_execution_context
DROP
CONSTRAINT job_exec_ctx_fk;

ALTER TABLE batch_job_execution_params
DROP
CONSTRAINT job_exec_params_fk;

ALTER TABLE batch_step_execution
DROP
CONSTRAINT job_exec_step_fk;

ALTER TABLE batch_job_execution
DROP
CONSTRAINT job_inst_exec_fk;

ALTER TABLE batch_step_execution_context
DROP
CONSTRAINT step_exec_ctx_fk;

CREATE SEQUENCE IF NOT EXISTS employee START WITH 1 INCREMENT BY 50;

CREATE TABLE employee
(
    id          BIGINT       NOT NULL,
    employee_id VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    surname     VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(255) NOT NULL,
    CONSTRAINT pk_employee PRIMARY KEY (id)
);

ALTER TABLE employee
    ADD CONSTRAINT uc_employee_employeeid UNIQUE (employee_id);

DROP TABLE batch_job_execution CASCADE;

DROP TABLE batch_job_execution_context CASCADE;

DROP TABLE batch_job_execution_params CASCADE;

DROP TABLE batch_job_instance CASCADE;

DROP TABLE batch_step_execution CASCADE;

DROP TABLE batch_step_execution_context CASCADE;

DROP SEQUENCE batch_job_execution_seq CASCADE;

DROP SEQUENCE batch_job_seq CASCADE;

DROP SEQUENCE batch_step_execution_seq CASCADE;
ALTER TABLE batch_job_execution_context
    DROP CONSTRAINT job_exec_ctx_fk;

ALTER TABLE batch_job_execution_params
    DROP CONSTRAINT job_exec_params_fk;

ALTER TABLE batch_step_execution
    DROP CONSTRAINT job_exec_step_fk;

ALTER TABLE batch_job_execution
    DROP CONSTRAINT job_inst_exec_fk;

ALTER TABLE batch_step_execution_context
    DROP CONSTRAINT step_exec_ctx_fk;

CREATE SEQUENCE IF NOT EXISTS employee START WITH 1 INCREMENT BY 50;

CREATE TABLE employee
(
    id          BIGINT       NOT NULL,
    employee_id VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    surname     VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    phone       VARCHAR(255) NOT NULL,
    CONSTRAINT pk_employee PRIMARY KEY (id)
);

ALTER TABLE employee
    ADD CONSTRAINT uc_employee_employeeid UNIQUE (employee_id);

DROP TABLE batch_job_execution CASCADE;

DROP TABLE batch_job_execution_context CASCADE;

DROP TABLE batch_job_execution_params CASCADE;

DROP TABLE batch_job_instance CASCADE;

DROP TABLE batch_step_execution CASCADE;

DROP TABLE batch_step_execution_context CASCADE;

DROP SEQUENCE batch_job_execution_seq CASCADE;

DROP SEQUENCE batch_job_seq CASCADE;

DROP SEQUENCE batch_step_execution_seq CASCADE;