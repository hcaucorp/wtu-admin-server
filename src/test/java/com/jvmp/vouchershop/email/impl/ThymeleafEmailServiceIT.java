package com.jvmp.vouchershop.email.impl;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.Set;

import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.utils.RandomUtils.randomEmail;
import static com.jvmp.vouchershop.utils.RandomUtils.randomVoucher;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
public class ThymeleafEmailServiceIT {

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Autowired
    private ThymeleafEmailService thymeleafEmailService;

    @Test
    public void sendVouchers() throws Exception {
        String name = "Tadzio";
        String email = randomEmail();
        long orderNumber = nextLong();
        Set<Voucher> vouchers = asSet(randomVoucher(), randomVoucher());
        thymeleafEmailService.sendVouchers(vouchers, new Order()
                .withOrderNumber(orderNumber)
                .withCustomer(new Customer()
                        .withFirstName(name)
                        .withEmail(email)));
        MimeMessage[] receivedMessages = greenMail.getReceivedMessages();

        assertEquals(1, receivedMessages.length);

        MimeMessage message = receivedMessages[0];
        String body = GreenMailUtil.getBody(message);

        assertEquals("Your top up voucher code order #" + orderNumber + " from wallettopup.co.uk", message.getSubject());
        vouchers.forEach(voucher -> assertTrue("Body: " + body, body.contains(voucher.getCode())));
        assertTrue(body.contains("Dear " + name));
        Address[] recipients = message.getRecipients(Message.RecipientType.TO);
        assertEquals(1, recipients.length);
        assertEquals(email, recipients[0].toString());
    }
}