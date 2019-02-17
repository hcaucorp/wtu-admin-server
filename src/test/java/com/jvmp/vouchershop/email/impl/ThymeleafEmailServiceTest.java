package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import java.util.Set;

import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.utils.RandomUtils.*;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ThymeleafEmailServiceTest {

    @Mock
    private ITemplateEngine templateEngine;

    @Mock
    private JavaMailSender javaMailSender;

    private ThymeleafEmailService thymeleafEmailService;

    @Before
    public void setUp() {
        thymeleafEmailService = new ThymeleafEmailService(templateEngine, javaMailSender);
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn(randomString());
    }

    @Test
    public void sendVouchers() {

        Set<Voucher> vouchers = asSet(
                randomVoucher(),
                randomVoucher()
        );
        String email = randomEmail();
        long orderId = nextLong();

        thymeleafEmailService.sendVouchers(vouchers, new Order()
                .withId(orderId)
                .withCustomer(new Customer()
                        .withEmail(email)));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertNotNull(message);
        assertEquals(singleton(email), asSet(message.getTo()));
    }
}