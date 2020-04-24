package com.olalayeone.audittrailtest;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.persistence.EntityManager;
import java.util.Random;

@SpringBootTest(classes = TestApplication.class)
public abstract class EntityTest {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected ApplicationContext applicationContext;

    protected final Faker faker = Faker.instance(new Random());
}
