package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.Application;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.utils.RandomUtils.randomVoucher;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DefaultEmailServiceIT {

    @Autowired
    private DefaultEmailService defaultEmailService;

    @Test
    @Ignore //TODO find a way to test it (mail server wiremock?)
    public void sendVouchers() {
        defaultEmailService.sendVouchers(asSet(
                randomVoucher(), randomVoucher(), randomVoucher()
        ), "dziukula@gmail.com");
    }
}