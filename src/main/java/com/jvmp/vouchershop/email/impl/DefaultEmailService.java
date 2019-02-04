package com.jvmp.vouchershop.email.impl;

import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.voucher.Voucher;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.stream.Collectors.joining;

@RequiredArgsConstructor
@Component
public class DefaultEmailService implements EmailService {

    private final JavaMailSender emailSender;

    @Override
    public void sendVouchers(Set<Voucher> vouchers, String email) {
        sendSimpleMessage(
                email,
                "Here is your order",
                vouchers.stream()
                        .map(Voucher::getCode)
                        .collect(joining("\n")));
    }

    private void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}
