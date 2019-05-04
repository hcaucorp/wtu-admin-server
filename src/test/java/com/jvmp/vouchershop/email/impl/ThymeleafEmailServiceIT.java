package com.jvmp.vouchershop.email.impl;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jvmp.vouchershop.Application;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.wallet.WalletService;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Set;

import static com.jvmp.Collections.asSet;
import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @MockBean
    private WalletService walletService;

    @Test
    public void sendVouchers() throws Exception {
        String name = "Tadzio";
        String email = randomEmail();
        when(walletService.findById(any())).thenReturn(Optional.of(randomWallet(TestNet3Params.get())));

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