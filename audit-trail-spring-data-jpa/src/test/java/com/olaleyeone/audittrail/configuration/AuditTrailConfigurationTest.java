package com.olaleyeone.audittrail.configuration;

import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.impl.EntityDataExtractorImpl;
import com.olaleyeone.audittrail.repository.TaskActivityRepository;
import com.olaleyeone.audittrail.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootTest(classes = AuditTrailConfigurationTest.$Config.class)
class AuditTrailConfigurationTest {

    @Test
    public void test() {

    }

    @SpringBootApplication
    static class $Config extends AuditTrailConfiguration {

        @Override
        public EntityDataExtractor entityDataExtractor(EntityManager entityManager) {
            return new EntityDataExtractorImpl(entityManager) {
                @Override
                public Class<?> getType(Object e) {
                    return e.getClass();
                }
            };
        }

        @Bean
        public TaskActivityRepository taskActivityRepository() {
            return Mockito.mock(TaskActivityRepository.class);
        }

        @Bean
        public TaskRepository taskRepository() {
            return Mockito.mock(TaskRepository.class);
        }
    }
}