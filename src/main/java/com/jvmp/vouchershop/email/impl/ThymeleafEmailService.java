package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ThymeleafEmailService implements EmailService {

    private final ITemplateEngine templateEngine;
    private final JavaMailSender emailSender;

    @Override
    public void sendVouchers(Set<Voucher> vouchers, Order order) {
        Customer customer = order.getCustomer();

        Context ctx = new Context();
        ctx.setVariable("name", customer.getFirstName());
        ctx.setVariable("vouchers", toUnsortedListHTML(vouchers));

        String htmlContent = templateEngine.process("email-deliver-vouchers.html", ctx);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(customer.getEmail());
        message.setSubject("Your top up voucher code order #" + order.getId() + " from wallettopup.co.uk");
        message.setText(htmlContent);
        emailSender.send(message);
    }

    private String toUnsortedListHTML(Set<Voucher> vouchers) {
        return "<ul>"
                +
                vouchers.stream()
                        .map(voucher -> "<li>" + voucher.getCode() + "</li>")
                        .collect(Collectors.joining())
                +
                "</ul>";
    }
}