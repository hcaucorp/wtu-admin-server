package com.jvmp.vouchershop.email.impl;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.shopify.domain.Order;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.utils.RandomUtils.randomVoucher;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SimpleEmailServiceIT {

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);
    @Autowired
    private SimpleEmailService simpleEmailService;

    @Test
    public void testSend() {
        GreenMailUtil.sendTextEmailTest("to@localhost.com", "from@localhost.com",
                "some subject", "some body"); // --- Place your sending code here instead
        assertEquals("some body", GreenMailUtil.getBody(greenMail.getReceivedMessages()[0]));
    }

    @Test
    @Ignore //TODO find a way to test it (mail server wiremock?)
    public void sendVouchers() {
        simpleEmailService.sendVouchers(asSet(
                randomVoucher(), randomVoucher(), randomVoucher()
                ),
                new Order());
    }
}