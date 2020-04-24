package com.olaleyeone.audittrail.advice;

import com.olaleyeone.audittrail.entity.Task;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;

@Transactional
@SpringBootTest(classes = EntityManagerPointcutTest.AdviceTestApplication.class)
class EntityManagerPointcutTest {

    @Autowired
    private Advice advice;

    @Autowired
    private EntityManager entityManager;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setName("abc");
        task.setType("test");
        task.setStartedOn(LocalDateTime.now());
    }

    @Test
    void persist() {
        entityManager.persist(task);
        Mockito.verify(advice, Mockito.times(1)).onPersist(Mockito.any());
    }

    @Test
    void merge() {
        task.setId(1L);
        entityManager.merge(task);
        Mockito.verify(advice, Mockito.times(1)).onMerge(Mockito.any());
    }

    @Test
    void remove() {
        task.setId(1L);
        entityManager.remove(task);
        Mockito.verify(advice, Mockito.times(1)).onRemove(Mockito.any());
    }

    @SpringBootApplication
    @EnableJpaRepositories({"com.olaleyeone.audittrail.repository"})
    @EntityScan({"com.olaleyeone.audittrail.entity"})
    static class AdviceTestApplication {

        @Bean
        public Advice advice() {
            return Mockito.spy(new Advice());
        }
    }

    @Aspect
    private static class Advice implements EntityManagerPointcut {

        @Around("persist()")
        public Object onPersist(ProceedingJoinPoint jp) {
            persist();
            return null;
        }

        @Around("merge()")
        public Object onMerge(ProceedingJoinPoint jp) {
            merge();
            return null;
        }

        @Around("remove()")
        public Object onRemove(ProceedingJoinPoint jp) {
            remove();
            return null;
        }
    }
}