package com.jvmp.vouchershop.controller;

import com.jvmp.vouchershop.notifications.NotificationService;
import com.jvmp.vouchershop.system.PropertyNames;
import com.jvmp.vouchershop.voucher.Voucher;
import com.jvmp.vouchershop.voucher.VoucherNotFound;
import com.jvmp.vouchershop.voucher.VoucherService;
import com.jvmp.vouchershop.voucher.impl.RedemptionRequest;
import com.jvmp.vouchershop.voucher.impl.RedemptionResponse;
import com.jvmp.vouchershop.voucher.impl.VoucherGenerationDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.time.Duration;
import java.util.List;

@RequestMapping("/api")
@RequiredArgsConstructor
@RestController
@CrossOrigin
@Slf4j
public class VoucherController {

    private final NotificationService notificationService;
    private final VoucherService voucherService;

    @Value(PropertyNames.AWS_SNS_TOPIC_REDEMPTIONS)
    private String redemptionsTopic;

    @GetMapping("/vouchers")
    public List<Voucher> getAllVouchers() {
        return voucherService.findAll();
    }

    @DeleteMapping("/vouchers/{sku}")
    public ResponseEntity<?> deleteVoucherBySku(@PathVariable String sku) {
        voucherService.deleteBySku(sku);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/vouchers")
    public ResponseEntity<?> generateVouchers(@RequestBody @Valid VoucherGenerationDetails details) {
        voucherService.save(voucherService.generateVouchers(details));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/vouchers"))
                .build();
    }

    @PostMapping("/vouchers/redeem")
    public RedemptionResponse redeemVoucher(@RequestBody @Valid RedemptionRequest detail) throws VoucherNotFound, InterruptedException {
        try {
            RedemptionResponse response = voucherService.redeemVoucher(detail);
            notificationService.push("Redeemed " + detail.getVoucherCode(), redemptionsTopic);
            return response;
        } catch (VoucherNotFound e) {
            notificationService.push("Tried to redeem absent voucher: " + detail.getVoucherCode() +
                    " to a wallet: " + detail.getDestinationAddress(), redemptionsTopic);

            // against brute force?
            Thread.sleep(Duration.ofSeconds(RandomUtils.nextLong(10, 20)).toMillis());

            throw e;
        } catch (Exception e) {
            notificationService.push("Redemption attempted but failed with exception: " + e.getMessage(), redemptionsTopic);
            throw e;
        }
    }
}
