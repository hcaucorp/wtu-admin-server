package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.voucher.Voucher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Set;

import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.utils.RandomUtils.randomEmail;
import static com.jvmp.vouchershop.utils.RandomUtils.randomVoucher;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultEmailServiceTest {

    private DefaultEmailService defaultEmailService;

    @Mock
    private JavaMailSender javaMailSender;


    @Before
    public void setUp() {
        defaultEmailService = new DefaultEmailService(javaMailSender);
    }

    @Test
    public void sendVouchers() {
        Set<Voucher> vouchers = asSet(
                randomVoucher(),
                randomVoucher()
        );
        String email = randomEmail();

        defaultEmailService.sendVouchers(vouchers, email);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertNotNull(message);
        assertEquals(singleton(email), asSet(message.getTo()));

        String emailBody = message.getText();
        assertNotNull(emailBody);
        vouchers.stream()
                .map(Voucher::getCode)
                .map(emailBody::contains)
                .forEach(Assert::assertTrue);
    }
}