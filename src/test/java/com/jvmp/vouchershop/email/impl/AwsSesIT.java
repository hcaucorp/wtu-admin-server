package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.utils.RandomUtils.randomVoucher;
import static org.apache.commons.lang3.RandomUtils.nextLong;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
public class AwsSesIT {

    @Autowired
    private EmailService emailService;

    @Test
    public void sendVouchers() {
        String name = "Tadzio";
        String email = "hubertinio@me.com";

        emailService.sendVouchers(asSet(randomVoucher()), new Order()
                .withOrderNumber(nextLong())
                .withCustomer(new Customer()
                        .withFirstName(name)
                        .withEmail(email)));
    }
}
