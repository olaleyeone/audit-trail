package com;


import com.github.javafaker.Faker;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

@ExtendWith(MockitoExtension.class)
public abstract class ComponentTest {

    protected final Faker faker = Faker.instance(new Random());
}
