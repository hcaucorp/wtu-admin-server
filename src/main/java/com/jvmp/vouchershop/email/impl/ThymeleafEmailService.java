package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.system.PropertyNames;
import com.jvmp.vouchershop.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThymeleafEmailService implements EmailService {

    private final ITemplateEngine templateEngine;
    private final JavaMailSender emailSender;
    private final NotificationService notificationService;

    @Value(PropertyNames.AWS_SES_FROM_EMAIL)
    private String fromEmail;

    @Value(PropertyNames.AWS_SES_FROM_NAME)
    private String fromName;

    @Override
    public void sendVouchers(Set<Voucher> vouchers, Order order) {
        Customer customer = order.getCustomer();

        Context ctx = new Context();
        ctx.setVariable("firstName", customer.getFirstName());
        ctx.setVariable("vouchers", vouchers);

        String htmlContent = templateEngine.process("email-deliver-vouchers.html", ctx);
        MimeMessage message = emailSender.createMimeMessage();

        try {
            message.setFrom(new InternetAddress(fromEmail, fromName));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(customer.getEmail()));
            message.setSubject("Your top up voucher code order #" + order.getOrderNumber()
                    + " from wallettopup.co.uk");
            message.setContent(htmlContent, "text/html");

            emailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            String errorMessage = "E-mail delivery failed because of exception: " + e.getMessage();
            notificationService.pushOrderNotification(errorMessage);
            log.error(errorMessage, e);
        }
    }
}