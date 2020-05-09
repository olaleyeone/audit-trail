package com.olaleyeone.audittrail.configuration;

import com.olaleyeone.audittrail.advice.ActivityAdvice;
import com.olaleyeone.audittrail.advice.EntityManagerAdvice;
import com.olaleyeone.audittrail.api.EntityDataExtractor;
import com.olaleyeone.audittrail.impl.*;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Provider;
import javax.persistence.EntityManager;

@Configuration
public abstract class AuditTrailConfiguration {

    @Bean
    public abstract EntityDataExtractor entityDataExtractor(EntityManager entityManager);

    @Bean
    public TaskContextSaver taskContextSaver(ApplicationContext applicationContext) {
        return applicationContext.getAutowireCapableBeanFactory().createBean(TaskContextSaver.class);
    }

    @Bean
    public TaskContextHolder taskContextHolder() {
        return new TaskContextHolder();
    }

    @Bean
    public ActivityAdvice activityAdvice(TaskContextHolder taskContextHolder) {
        return new ActivityAdvice(taskContextHolder);
    }

    @Bean
    public TaskTransactionContextFactory taskTransactionContextFactory(TaskContextHolder taskContextHolder) {
        return new TaskTransactionContextFactory(taskContextHolder);
    }

    @Bean
    public TaskContextFactory taskContextFactory(AutowireCapableBeanFactory beanFactory) {
        return beanFactory.createBean(TaskContextFactory.class);
    }

    public EntityManagerAdvice entityManagerAdvice(
            EntityDataExtractor entityDataExtractor,
            Provider<TaskTransactionContext> taskTransactionContextProvider) {
        return new EntityManagerAdvice(entityDataExtractor, taskTransactionContextProvider);
    }
}
