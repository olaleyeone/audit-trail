package com.olalayeone.audittrailtest;

import com.olaleyeone.audittrail.advice.AuditTrailAdvice;
import com.olaleyeone.audittrail.impl.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories({"com.olaleyeone.audittrail.repository"})
@EntityScan({"com.olaleyeone.audittrail.entity", "com.olalayeone.audittrailtest.data.entity"})
public class TestApplication {

    @Bean
    public AuditTrailAdvice auditTrailAdvice() {
        return Mockito.mock(AuditTrailAdvice.class);
    }

    @Bean
    public TaskTransactionContextFactory taskTransactionContextFactory() {
        return new TaskTransactionContextFactory(new TaskContextHolder()) {

            @Override
            public TaskTransactionContext createTaskTransactionContext(TaskTransactionLogger taskTransactionLogger) {
                return Mockito.mock(TaskTransactionContext.class);
            }
        };
    }

    @Bean
    public TaskContextImpl taskContext() {
        return Mockito.mock(TaskContextImpl.class);
    }
}
