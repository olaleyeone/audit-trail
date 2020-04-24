package com.olaleyeone.audittrail.advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Transactional
@SpringBootTest(classes = EntityManagerPointcutTest.AdviceTestApplication.class)
class EntityManagerPointcutTest {

    @Autowired
    private Advice advice;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
    }

    @Test
    void persist() {
        entityManager.persist(1L);
        Mockito.verify(advice, Mockito.times(1)).onPersist(Mockito.any());
    }

    @Test
    void merge() {
        entityManager.merge(1L);
        Mockito.verify(advice, Mockito.times(1)).onMerge(Mockito.any());
    }

    @Test
    void remove() {
        entityManager.remove(1L);
        Mockito.verify(advice, Mockito.times(1)).onRemove(Mockito.any());
    }

    @SpringBootApplication
    static class AdviceTestApplication {

        @Bean
        public EntityManager entityManager() {
            return Mockito.mock(EntityManager.class);
        }

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