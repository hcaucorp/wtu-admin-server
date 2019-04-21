package com.jvmp.vouchershop.email.impl;

import com.google.zxing.WriterException;
import com.jvmp.vouchershop.email.EmailService;
import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.qr.QrCode;
import com.jvmp.vouchershop.qr.QrCodeService;
import com.jvmp.vouchershop.shopify.domain.Customer;
import com.jvmp.vouchershop.shopify.domain.Order;
import com.jvmp.vouchershop.system.PropertyNames;
import com.jvmp.vouchershop.voucher.Voucher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class ThymeleafEmailService implements EmailService {

    private final ITemplateEngine templateEngine;
    private final JavaMailSender emailSender;
    private final NotificationService notificationService;
    private final QrCodeService qrCodeService;

    private final String fromEmail;
    private final String fromName;
    private final String emailDeliveryTemplateFileName;

    ThymeleafEmailService(
            ITemplateEngine templateEngine,
            JavaMailSender emailSender,
            NotificationService notificationService,
            QrCodeService qrCodeService,

            @Value(PropertyNames.AWS_SES_FROM_EMAIL) String fromEmail,
            @Value(PropertyNames.AWS_SES_FROM_NAME) String fromName,
            @Value(PropertyNames.THYMELEAF_TEMPLATE_EMAIL_DELIVER_VOUCHERS) String emailDeliveryTemplateFileName
    ) {
        this.templateEngine = templateEngine;
        this.emailSender = emailSender;
        this.notificationService = notificationService;
        this.qrCodeService = qrCodeService;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.emailDeliveryTemplateFileName = emailDeliveryTemplateFileName;
    }

    @Override
    public void sendVouchers(Set<Voucher> vouchers, Order order) {
        Customer customer = order.getCustomer();

        Context ctx = new Context();
        ctx.setVariable("firstName", customer.getFirstName());
        ctx.setVariable("vouchers", vouchers);
        for (Voucher voucher : vouchers) {
            ctx.setVariable(toContentId(voucher), toContentId(voucher));
        }

        try {

            String htmlContent = templateEngine.process(emailDeliveryTemplateFileName, ctx);
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(new InternetAddress(customer.getEmail()));
            helper.setSubject("Your top up voucher code order #" + order.getOrderNumber() + " from wallettopup.co.uk");
            helper.setText(htmlContent, true);

            addQrCodeImagesToEmail(helper, vouchers);

            emailSender.send(message);
        } catch (IOException | WriterException | MessagingException e) {
            String errorMessage = "E-mail delivery failed because of exception: " + e.getMessage();
            notificationService.pushOrderNotification(errorMessage);
            log.error(errorMessage, e);
        }
    }

    private void addQrCodeImagesToEmail(MimeMessageHelper helper, Set<Voucher> vouchers) throws IOException, WriterException, MessagingException {
        // create qr codes "in memory"
        for (Voucher voucher : vouchers) {
            QrCode qrCode = qrCodeService.createQRCode(voucher);
            String contentId = toContentId(voucher);
            helper.addInline(contentId, qrCode.toInputStreamSource(), qrCode.getContentType());
        }
    }

    private String toContentId(Voucher voucher) {
        return voucher.getCode() + ".png";
    }
}