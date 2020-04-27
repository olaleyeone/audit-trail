package com.olaleyeone.audittrail.advice;

import com.olaleyeone.audittrail.api.Activity;
import com.olaleyeone.audittrail.entity.CodeContext;
import com.olaleyeone.audittrail.impl.CodeContextUtil;
import lombok.NoArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(classes = ActivityPointcutTest.AdviceTestApplication.class)
class ActivityPointcutTest {

    @Autowired
    private Advice advice;

    @Autowired
    private Target target;

    @BeforeEach
    public void setUp() {
        Mockito.reset(advice);
    }

    @Test
    void persist() {
        target.activity(1, "a");
        Mockito.verify(advice, Mockito.times(1)).adviceActivity(Mockito.argThat(argument -> {
            MethodSignature methodSignature = (MethodSignature) argument.getSignature();

            CodeContext entryPoint = CodeContextUtil.getEntryPoint(methodSignature);
            assertNotNull(entryPoint);
            assertEquals(Target.class.getName(), entryPoint.getClassName());
            assertEquals("activity", entryPoint.getMethodName());
            assertEquals("(int, java.lang.String)", entryPoint.getMethodSignature());

            Activity activity = CodeContextUtil.getActivityAnnotation(methodSignature);
            assertNotNull(activity);
            assertEquals("Test", activity.value());
            return true;
        }));
    }

    @Test
    void merge() {
        target.toString();
        Mockito.verify(advice, Mockito.never()).adviceActivity(Mockito.any());
    }

    @SpringBootApplication
    static class AdviceTestApplication {

        @Bean
        public Target target() {
            return new Target();
        }

        @Bean
        public Advice advice() {
            return Mockito.spy(new Advice());
        }
    }

    @Aspect
    private static class Advice implements ActivityPointCut {

        @Around("activityMethod()")
        public Object adviceActivity(ProceedingJoinPoint jp) {
            activityMethod();
            return null;
        }
    }

    @NoArgsConstructor
    private static class Target {

        @Activity("Test")
        void activity(int arg0, String arg1) {
            //intentional
        }
    }
}