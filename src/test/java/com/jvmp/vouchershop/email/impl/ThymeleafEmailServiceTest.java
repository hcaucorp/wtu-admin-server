package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.IContext;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Set;

import static com.jvmp.vouchershop.Collections.asSet;
import static com.jvmp.vouchershop.utils.RandomUtils.*;
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

    @InjectMocks
    private ThymeleafEmailService thymeleafEmailService;

    @Before
    public void setUp() {
        when(templateEngine.process(any(String.class), any(IContext.class))).thenReturn(randomString());
        when(javaMailSender.createMimeMessage()).thenReturn(new M3());
    }

    @Test
    public void sendVouchers() throws MessagingException {

        Set<Voucher> vouchers = asSet(
                randomVoucher(),
                randomVoucher()
        );
        String email = randomEmail();
        long orderNumber = nextLong();

        thymeleafEmailService.sendVouchers(vouchers, new Order()
                .withOrderNumber(orderNumber)
                .withCustomer(new Customer()
                        .withEmail(email)));

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(messageCaptor.capture());

        MimeMessage message = messageCaptor.getValue();

        assertNotNull(message);
        assertEquals(email, message.getRecipients(Message.RecipientType.TO)[0].toString());
    }


    static class M3 extends MimeMessage {
        private Address[] recipients = null;

        M3() {
            super((Session) null);
        }

        @Override
        public Address[] getRecipients(Message.RecipientType type) {
            return recipients;
        }

        public void setRecipient(Message.RecipientType type, Address address) {
            this.recipients = new Address[]{address};
        }

    }
}