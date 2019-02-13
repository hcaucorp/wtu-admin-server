package com.jvmp.vouchershop.email.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

@Component
@RequiredArgsConstructor
public class DefaultEmailFactory {

    private final TemplateEngine templateEngine;
    private final TemplateEngine stringTemplateEngine;

    SimpleMailMessage createDeliverVouchersEmail(Set<Voucher> vouchers, String email) {
        
        return SimpleMailMessage.builder()
                .from("crypto.vouchers@gmail.com")
                .to(email)
                .subject("Your crypto voucher codes order delivery")
                .body(processBody(vouchers))).build();
    }

    private String processBody(String bodyTemplate, Invoice invoice, String invoiceLink) {
        Context context = new Context();
        return templateEngine.process(bodyTemplate, context);
    }

    private String procesVouchers(String linkStingTemplate, String domain, Set<Voucher> vouchers) {
        Context context = new Context();
        context.setVariable("domain", domain);
        
        return stringTemplateEngine.process(linkStingTemplate, context);
    }

    private String processNoReplyAddress(String noReplyAddressStringTemplate, String domain) {
        Context context = new Context();
        context.setVariable("domain", domain);
        return stringTemplateEngine.process(noReplyAddressStringTemplate, context);
    }
}