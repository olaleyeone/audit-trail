package com.olalayeone.audittrailtest;

import com.olaleyeone.audittrail.advice.AuditTrailAdvice;
import com.olaleyeone.audittrail.impl.AuditTrailLogger;
import com.olaleyeone.audittrail.impl.AuditTrailLoggerDelegate;
import com.olaleyeone.audittrail.impl.AuditTrailLoggerFactory;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories({"com.olaleyeone.audittrail.repository"})
@EntityScan({"com.olaleyeone.audittrail.entity", "com.olalayeone.audittrailtest.data.entity"})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @Bean
    public AuditTrailAdvice auditTrailAdvice() {
        return Mockito.mock(AuditTrailAdvice.class);
    }

    @Bean
    public AuditTrailLoggerFactory auditTrailLoggerFactory() {
        return new AuditTrailLoggerFactory() {

            @Override
            public AuditTrailLogger createLogger(AuditTrailLoggerDelegate auditTrailLoggerDelegate) {
                return Mockito.mock(AuditTrailLogger.class);
            }
        };
    }
}
