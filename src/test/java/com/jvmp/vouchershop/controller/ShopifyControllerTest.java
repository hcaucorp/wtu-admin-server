package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class ShopifyControllerTest {

    @Before
    public void setUp() {
    }

    @Test
    public void fullFillmentHook() {
        fail("not implemented");

        // TODO should be accessible without authentication
    }
}