package com.olalayeone.audittrailtest;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.internal.creation.bytebuddy.MockAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import java.util.Random;

@SpringBootTest(classes = TestApplication.class)
public class EntityTest {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected ApplicationContext applicationContext;

    protected final Faker faker = Faker.instance(new Random());

    @BeforeEach
    public void resetMocks() {
        applicationContext.getBeansOfType(MockAccess.class)
                .values().forEach(Mockito::reset);
    }
}
